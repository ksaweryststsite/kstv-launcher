package pl.ksawery.ktvlauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.ksawery.ktvlauncher.data.AppRepository
import pl.ksawery.ktvlauncher.data.LauncherPreferences
import pl.ksawery.ktvlauncher.data.MediaPlaybackRepository
import pl.ksawery.ktvlauncher.data.WatchNextRepository
import pl.ksawery.ktvlauncher.data.WeatherRepository
import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.model.HomeFocusMode
import pl.ksawery.ktvlauncher.model.LauncherThemeMode
import pl.ksawery.ktvlauncher.model.ShelfMode
import pl.ksawery.ktvlauncher.model.UiScale
import pl.ksawery.ktvlauncher.model.WatchNextStatus

class HomeViewModel(
    private val appRepository: AppRepository,
    private val weatherRepository: WeatherRepository,
    private val watchNextRepository: WatchNextRepository,
    private val mediaPlaybackRepository: MediaPlaybackRepository,
    private val preferences: LauncherPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            launcherTheme = preferences.launcherTheme(),
            wallpaperUri = preferences.wallpaperUri(preferences.launcherTheme()),
            continueWatchingEnabled = preferences.continueWatchingEnabled(),
            shelfMode = preferences.shelfMode(),
            homeFocusMode = preferences.homeFocusMode(),
            dockBackgroundEnabled = preferences.dockBackgroundEnabled(),
            uiScale = preferences.uiScale(),
            themeOneName = preferences.themeName(LauncherThemeMode.Theme1),
            themeTwoName = preferences.themeName(LauncherThemeMode.Theme2),
            themeThreeName = preferences.themeName(LauncherThemeMode.Theme3),
            mediaWidgetEnabled = preferences.mediaWidgetEnabled(),
            watchNextSourcePackage = preferences.watchNextSourcePackage(),
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var allWatchNextItems = emptyList<pl.ksawery.ktvlauncher.model.WatchNextItem>()
    private var mediaPollingJob: Job? = null

    init {
        refreshApps()
        refreshWeather()
        refreshWatchNext()
        startMediaPlaybackMonitoring()
    }

    fun refreshApps() {
        viewModelScope.launch {
            runCatching { appRepository.getLaunchableApps() }
                .onSuccess(::applyApps)
                .onFailure {
                    _uiState.update { state -> state.copy(isLoading = false) }
                }
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            runCatching { weatherRepository.currentForBienczyce() }
                .onSuccess { weather -> _uiState.update { it.copy(weather = weather) } }
        }
    }

    fun refreshWatchNext() {
        if (!preferences.continueWatchingEnabled()) {
            _uiState.update {
                it.copy(
                    continueWatchingEnabled = false,
                    watchNext = emptyList(),
                    watchNextStatus = WatchNextStatus.Empty,
                )
            }
            return
        }

        _uiState.update {
            it.copy(continueWatchingEnabled = true, watchNextStatus = WatchNextStatus.Loading)
        }
        viewModelScope.launch {
            runCatching { watchNextRepository.load() }
                .onSuccess { items ->
                    allWatchNextItems = items
                    applyWatchNext()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(watchNext = emptyList(), watchNextStatus = WatchNextStatus.Unavailable)
                    }
                }
        }
    }

    fun recordLaunch(app: LaunchableApp) {
        preferences.recordLaunch(app.componentName)
        _uiState.update { state -> state.copy(recent = selectRecent(state.apps)) }
    }

    fun setWallpaperUri(uri: String) {
        val theme = _uiState.value.launcherTheme
        preferences.setWallpaperUri(theme, uri)
        _uiState.update { it.copy(wallpaperUri = uri) }
    }

    fun resetWallpaper() {
        val theme = _uiState.value.launcherTheme
        preferences.resetWallpaper(theme)
        _uiState.update { it.copy(wallpaperUri = null) }
    }

    fun toggleFavorite(app: LaunchableApp) {
        val components = preferences.favoriteComponents().toMutableList()
        if (app.componentName in components) {
            components.remove(app.componentName)
        } else if (components.size < MAX_FAVORITES) {
            components += app.componentName
        }
        preferences.setFavoriteComponents(components)
        applyApps(_uiState.value.apps)
    }

    fun moveFavorite(app: LaunchableApp, direction: Int) {
        val components = preferences.favoriteComponents().toMutableList()
        val currentIndex = components.indexOf(app.componentName)
        if (currentIndex < 0) return
        val newIndex = (currentIndex + direction).coerceIn(0, components.lastIndex)
        if (currentIndex != newIndex) {
            val component = components.removeAt(currentIndex)
            components.add(newIndex, component)
            preferences.setFavoriteComponents(components)
            applyApps(_uiState.value.apps)
        }
    }

    fun setDockShortcut(slot: Int, app: LaunchableApp) {
        val components = preferences.dockComponents().toMutableList()
        while (components.size <= slot) {
            components += app.componentName
        }
        components[slot] = app.componentName
        preferences.setDockComponents(components)
        applyApps(_uiState.value.apps)
    }

    fun setFeaturedApp(slot: Int, app: LaunchableApp) {
        val components = preferences.featuredComponents().toMutableList()
        while (components.size <= slot) {
            components += app.componentName
        }
        components[slot] = app.componentName
        preferences.setFeaturedComponents(components)
        applyApps(_uiState.value.apps)
    }

    fun cycleShelfMode() {
        val mode = when (_uiState.value.shelfMode) {
            ShelfMode.WatchNext -> ShelfMode.AppShortcuts
            ShelfMode.AppShortcuts -> ShelfMode.Hidden
            ShelfMode.Hidden -> ShelfMode.WatchNext
        }
        preferences.setShelfMode(mode)
        _uiState.update { it.copy(shelfMode = mode) }
    }

    fun cycleHomeFocusMode() {
        val modes = HomeFocusMode.entries
        val current = modes.indexOf(_uiState.value.homeFocusMode)
        val mode = modes[(current + 1) % modes.size]
        preferences.setHomeFocusMode(mode)
        _uiState.update { it.copy(homeFocusMode = mode) }
    }

    fun toggleDockBackground() {
        val enabled = !_uiState.value.dockBackgroundEnabled
        preferences.setDockBackgroundEnabled(enabled)
        _uiState.update { it.copy(dockBackgroundEnabled = enabled) }
    }

    fun cycleUiScale() {
        val values = UiScale.entries
        val current = values.indexOf(_uiState.value.uiScale)
        val scale = values[(current + 1) % values.size]
        preferences.setUiScale(scale)
        _uiState.update { it.copy(uiScale = scale) }
    }

    fun cycleLauncherTheme() {
        val values = LauncherThemeMode.entries
        val current = values.indexOf(_uiState.value.launcherTheme)
        val theme = values[(current + 1) % values.size]
        preferences.setLauncherTheme(theme)
        _uiState.update {
            it.copy(
                launcherTheme = theme,
                wallpaperUri = preferences.wallpaperUri(theme),
            )
        }
    }

    fun setThemeNames(themeOneName: String, themeTwoName: String, themeThreeName: String) {
        preferences.setThemeName(LauncherThemeMode.Theme1, themeOneName)
        preferences.setThemeName(LauncherThemeMode.Theme2, themeTwoName)
        preferences.setThemeName(LauncherThemeMode.Theme3, themeThreeName)
        _uiState.update {
            it.copy(
                themeOneName = preferences.themeName(LauncherThemeMode.Theme1),
                themeTwoName = preferences.themeName(LauncherThemeMode.Theme2),
                themeThreeName = preferences.themeName(LauncherThemeMode.Theme3),
            )
        }
    }

    fun toggleMediaWidget() {
        val enabled = !_uiState.value.mediaWidgetEnabled
        preferences.setMediaWidgetEnabled(enabled)
        _uiState.update {
            it.copy(
                mediaWidgetEnabled = enabled,
                mediaPlayback = if (enabled) it.mediaPlayback else null,
            )
        }
        startMediaPlaybackMonitoring()
    }

    fun refreshMediaPlayback() {
        if (!_uiState.value.mediaWidgetEnabled) {
            _uiState.update { it.copy(mediaPlayback = null) }
            return
        }
        _uiState.update {
            it.copy(mediaPlayback = mediaPlaybackRepository.currentPlayback())
        }
    }

    private fun startMediaPlaybackMonitoring() {
        mediaPollingJob?.cancel()
        if (!_uiState.value.mediaWidgetEnabled) {
            _uiState.update { it.copy(mediaPlayback = null) }
            return
        }
        mediaPollingJob = viewModelScope.launch {
            while (isActive) {
                refreshMediaPlayback()
                delay(MEDIA_POLLING_INTERVAL_MS)
            }
        }
    }

    override fun onCleared() {
        mediaPollingJob?.cancel()
        super.onCleared()
    }

    fun cycleWatchNextSource() {
        val packages = _uiState.value.watchNextSources
            .map { it.componentName.packageName }
            .distinct()
        val options = listOf<String?>(null) + packages
        val current = options.indexOf(_uiState.value.watchNextSourcePackage).coerceAtLeast(0)
        val selected = options[(current + 1) % options.size]
        preferences.setWatchNextSourcePackage(selected)
        _uiState.update { it.copy(watchNextSourcePackage = selected) }
        applyWatchNext()
    }

    fun moveApp(app: LaunchableApp, direction: Int) {
        val ordered = _uiState.value.apps.toMutableList()
        val current = ordered.indexOf(app)
        if (current < 0) return
        val target = (current + direction).coerceIn(0, ordered.lastIndex)
        if (target == current) return
        ordered.add(target, ordered.removeAt(current))
        preferences.setAppOrderComponents(ordered.map { it.componentName })
        applyApps(ordered)
    }

    fun reloadProfile() {
        _uiState.update {
            it.copy(
                shelfMode = preferences.shelfMode(),
                homeFocusMode = preferences.homeFocusMode(),
                continueWatchingEnabled = preferences.continueWatchingEnabled(),
                dockBackgroundEnabled = preferences.dockBackgroundEnabled(),
                uiScale = preferences.uiScale(),
                launcherTheme = preferences.launcherTheme(),
                wallpaperUri = preferences.wallpaperUri(preferences.launcherTheme()),
                themeOneName = preferences.themeName(LauncherThemeMode.Theme1),
                themeTwoName = preferences.themeName(LauncherThemeMode.Theme2),
                themeThreeName = preferences.themeName(LauncherThemeMode.Theme3),
                mediaWidgetEnabled = preferences.mediaWidgetEnabled(),
                mediaPlayback = null,
                watchNextSourcePackage = preferences.watchNextSourcePackage(),
            )
        }
        applyApps(_uiState.value.apps)
        refreshWatchNext()
        startMediaPlaybackMonitoring()
    }

    fun setContinueWatchingEnabled(enabled: Boolean) {
        preferences.setContinueWatchingEnabled(enabled)
        _uiState.update { it.copy(continueWatchingEnabled = enabled) }
        refreshWatchNext()
    }

    private fun applyApps(apps: List<LaunchableApp>) {
        val orderedApps = orderApps(apps)
        val appsByComponent = orderedApps.associateBy { it.componentName }

        var favoriteComponents = preferences.favoriteComponents()
        if (favoriteComponents.isEmpty()) {
            favoriteComponents = selectDefaultFavorites(orderedApps).map { it.componentName }
            preferences.setFavoriteComponents(favoriteComponents)
        }
        val favorites = favoriteComponents.mapNotNull(appsByComponent::get).take(MAX_FAVORITES)

        var dockComponents = preferences.dockComponents()
        if (dockComponents.isEmpty()) {
            dockComponents = favorites.take(MAX_DOCK_SHORTCUTS).map { it.componentName }
            preferences.setDockComponents(dockComponents)
        }

        var featuredComponents = preferences.featuredComponents()
        if (featuredComponents.isEmpty()) {
            featuredComponents = favorites
                .filter { app ->
                    FEATURED_LABELS.any { label -> app.label.contains(label, ignoreCase = true) }
                }
                .take(MAX_FEATURED_APPS)
                .map { it.componentName }
            preferences.setFeaturedComponents(featuredComponents)
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                apps = orderedApps,
                recentlyAdded = orderedApps.sortedByDescending { app -> app.firstInstallTime }.take(5),
                favorites = favorites,
                dockShortcuts = dockComponents.mapNotNull(appsByComponent::get)
                    .take(MAX_DOCK_SHORTCUTS),
                featuredApps = featuredComponents.mapNotNull(appsByComponent::get)
                    .take(MAX_FEATURED_APPS),
                shelfMode = preferences.shelfMode(),
                recent = selectRecent(orderedApps),
            )
        }
        if (allWatchNextItems.isNotEmpty()) applyWatchNext()
    }

    private fun orderApps(apps: List<LaunchableApp>): List<LaunchableApp> {
        val byComponent = apps.associateBy { it.componentName }
        val saved = preferences.appOrderComponents()
        if (saved.isEmpty()) {
            return apps.sortedByDescending { it.firstInstallTime }.also {
                preferences.setAppOrderComponents(it.map(LaunchableApp::componentName))
            }
        }
        val savedSet = saved.toSet()
        val newlyInstalled = apps
            .filterNot { it.componentName in savedSet }
            .sortedByDescending { it.firstInstallTime }
        val persisted = saved.mapNotNull(byComponent::get)
        val remaining = apps.filterNot { app ->
            app in newlyInstalled || app.componentName in savedSet
        }
        return newlyInstalled + persisted + remaining
    }

    private fun applyWatchNext() {
        val packages = allWatchNextItems.mapNotNull { it.packageName }.toSet()
        val sources = _uiState.value.apps
            .filter { it.componentName.packageName in packages }
            .distinctBy { it.componentName.packageName }
        var selected = preferences.watchNextSourcePackage()
        if (selected != null && selected !in packages) {
            selected = null
            preferences.setWatchNextSourcePackage(null)
        }
        val visible = allWatchNextItems
            .filter { selected == null || it.packageName == selected }
            .take(MAX_WATCH_NEXT_VISIBLE)
        _uiState.update {
            it.copy(
                watchNext = visible,
                watchNextSources = sources,
                watchNextSourcePackage = selected,
                watchNextStatus = if (visible.isEmpty()) WatchNextStatus.Empty else WatchNextStatus.Ready,
            )
        }
    }

    private fun selectRecent(apps: List<LaunchableApp>): List<LaunchableApp> {
        val appsByComponent = apps.associateBy { it.componentName }
        return preferences.recentComponents().mapNotNull(appsByComponent::get).take(2)
    }

    private fun selectDefaultFavorites(apps: List<LaunchableApp>): List<LaunchableApp> {
        val selected = buildList {
            FAVORITE_LABELS.forEach { preferredLabel ->
                apps.firstOrNull { app ->
                    app.label.contains(preferredLabel, ignoreCase = true) && app !in this
                }?.let(::add)
            }
        }.toMutableList()

        if (selected.size < MAX_FAVORITES) {
            apps.filterNot { app ->
                app in selected || UTILITY_LABELS.any {
                    utility -> app.label.contains(utility, ignoreCase = true)
                }
            }.take(MAX_FAVORITES - selected.size).forEach(selected::add)
        }
        return selected.take(MAX_FAVORITES)
    }

    private companion object {
        const val MAX_FAVORITES = 8
        const val MAX_DOCK_SHORTCUTS = 3
        const val MAX_FEATURED_APPS = 2
        const val MAX_WATCH_NEXT_VISIBLE = 4
        const val MEDIA_POLLING_INTERVAL_MS = 1_000L
        val FEATURED_LABELS = listOf("Netflix", "Prime Video")
        val FAVORITE_LABELS = listOf(
            "Netflix", "Spotify", "YouTube", "Prime Video",
            "Disney+", "Twitch", "cda.pl", "HBO Max",
        )
        val UTILITY_LABELS = listOf(
            "AIDA", "AirReceiver", "AirScreen", "Aptoide", "Browser",
            "Button", "Downloader", "File Manager", "DTV Viewer",
        )
    }
}

class HomeViewModelFactory(
    private val appRepository: AppRepository,
    private val weatherRepository: WeatherRepository,
    private val watchNextRepository: WatchNextRepository,
    private val mediaPlaybackRepository: MediaPlaybackRepository,
    private val preferences: LauncherPreferences,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java))
        return HomeViewModel(
            appRepository,
            weatherRepository,
            watchNextRepository,
            mediaPlaybackRepository,
            preferences,
        ) as T
    }
}
