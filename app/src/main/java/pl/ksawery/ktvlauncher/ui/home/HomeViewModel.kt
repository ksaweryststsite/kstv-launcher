package pl.ksawery.ktvlauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.ksawery.ktvlauncher.data.AppRepository
import pl.ksawery.ktvlauncher.data.LauncherPreferences
import pl.ksawery.ktvlauncher.data.WatchNextRepository
import pl.ksawery.ktvlauncher.data.WeatherRepository
import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.model.WatchNextStatus

class HomeViewModel(
    private val appRepository: AppRepository,
    private val weatherRepository: WeatherRepository,
    private val watchNextRepository: WatchNextRepository,
    private val preferences: LauncherPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            wallpaperUri = preferences.wallpaperUri(),
            continueWatchingEnabled = preferences.continueWatchingEnabled(),
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshApps()
        refreshWeather()
        refreshWatchNext()
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
                    _uiState.update {
                        it.copy(
                            watchNext = items,
                            watchNextStatus = if (items.isEmpty()) {
                                WatchNextStatus.Empty
                            } else {
                                WatchNextStatus.Ready
                            },
                        )
                    }
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
        preferences.setWallpaperUri(uri)
        _uiState.update { it.copy(wallpaperUri = uri) }
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

    fun setContinueWatchingEnabled(enabled: Boolean) {
        preferences.setContinueWatchingEnabled(enabled)
        _uiState.update { it.copy(continueWatchingEnabled = enabled) }
        refreshWatchNext()
    }

    private fun applyApps(apps: List<LaunchableApp>) {
        val appsByComponent = apps.associateBy { it.componentName }

        var favoriteComponents = preferences.favoriteComponents()
        if (favoriteComponents.isEmpty()) {
            favoriteComponents = selectDefaultFavorites(apps).map { it.componentName }
            preferences.setFavoriteComponents(favoriteComponents)
        }
        val favorites = favoriteComponents.mapNotNull(appsByComponent::get).take(MAX_FAVORITES)

        var dockComponents = preferences.dockComponents()
        if (dockComponents.isEmpty()) {
            dockComponents = favorites.take(MAX_DOCK_SHORTCUTS).map { it.componentName }
            preferences.setDockComponents(dockComponents)
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                apps = apps,
                favorites = favorites,
                dockShortcuts = dockComponents.mapNotNull(appsByComponent::get)
                    .take(MAX_DOCK_SHORTCUTS),
                recent = selectRecent(apps),
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
    private val preferences: LauncherPreferences,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java))
        return HomeViewModel(
            appRepository,
            weatherRepository,
            watchNextRepository,
            preferences,
        ) as T
    }
}
