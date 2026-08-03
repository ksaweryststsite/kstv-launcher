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
import pl.ksawery.ktvlauncher.data.WeatherRepository
import pl.ksawery.ktvlauncher.model.LaunchableApp

class HomeViewModel(
    private val appRepository: AppRepository,
    private val weatherRepository: WeatherRepository,
    private val preferences: LauncherPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(wallpaperUri = preferences.wallpaperUri()),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshApps()
        refreshWeather()
    }

    fun refreshApps() {
        viewModelScope.launch {
            runCatching { appRepository.getLaunchableApps() }
                .onSuccess { apps ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            apps = apps,
                            favorites = selectFavorites(apps),
                            recent = selectRecent(apps),
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state -> state.copy(isLoading = false) }
                }
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            runCatching { weatherRepository.currentForBienczyce() }
                .onSuccess { weather ->
                    _uiState.update { it.copy(weather = weather) }
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

    private fun selectRecent(apps: List<LaunchableApp>): List<LaunchableApp> {
        val appsByComponent = apps.associateBy { it.componentName }
        return preferences.recentComponents().mapNotNull(appsByComponent::get).take(2)
    }

    private fun selectFavorites(apps: List<LaunchableApp>): List<LaunchableApp> {
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
        val FAVORITE_LABELS = listOf(
            "Netflix",
            "Spotify",
            "YouTube",
            "Prime Video",
            "Disney+",
            "Twitch",
            "cda.pl",
            "HBO Max",
        )
        val UTILITY_LABELS = listOf(
            "AIDA",
            "AirReceiver",
            "AirScreen",
            "Aptoide",
            "Browser",
            "Button",
            "Downloader",
            "File Manager",
            "DTV Viewer",
        )
    }
}

class HomeViewModelFactory(
    private val appRepository: AppRepository,
    private val weatherRepository: WeatherRepository,
    private val preferences: LauncherPreferences,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java))
        return HomeViewModel(appRepository, weatherRepository, preferences) as T
    }
}
