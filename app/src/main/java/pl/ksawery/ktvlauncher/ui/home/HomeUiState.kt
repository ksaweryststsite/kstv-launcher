package pl.ksawery.ktvlauncher.ui.home

import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.model.WeatherSnapshot
import pl.ksawery.ktvlauncher.model.WatchNextItem
import pl.ksawery.ktvlauncher.model.WatchNextStatus

data class HomeUiState(
    val isLoading: Boolean = true,
    val apps: List<LaunchableApp> = emptyList(),
    val favorites: List<LaunchableApp> = emptyList(),
    val dockShortcuts: List<LaunchableApp> = emptyList(),
    val recent: List<LaunchableApp> = emptyList(),
    val watchNext: List<WatchNextItem> = emptyList(),
    val watchNextStatus: WatchNextStatus = WatchNextStatus.Loading,
    val continueWatchingEnabled: Boolean = true,
    val weather: WeatherSnapshot? = null,
    val wallpaperUri: String? = null,
)
