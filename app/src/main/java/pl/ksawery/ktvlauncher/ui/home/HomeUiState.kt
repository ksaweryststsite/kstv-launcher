package pl.ksawery.ktvlauncher.ui.home

import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.model.ShelfMode
import pl.ksawery.ktvlauncher.model.UiScale
import pl.ksawery.ktvlauncher.model.WeatherSnapshot
import pl.ksawery.ktvlauncher.model.WatchNextItem
import pl.ksawery.ktvlauncher.model.WatchNextStatus

data class HomeUiState(
    val isLoading: Boolean = true,
    val apps: List<LaunchableApp> = emptyList(),
    val recentlyAdded: List<LaunchableApp> = emptyList(),
    val favorites: List<LaunchableApp> = emptyList(),
    val dockShortcuts: List<LaunchableApp> = emptyList(),
    val featuredApps: List<LaunchableApp> = emptyList(),
    val shelfMode: ShelfMode = ShelfMode.WatchNext,
    val recent: List<LaunchableApp> = emptyList(),
    val watchNext: List<WatchNextItem> = emptyList(),
    val watchNextStatus: WatchNextStatus = WatchNextStatus.Loading,
    val watchNextSources: List<LaunchableApp> = emptyList(),
    val watchNextSourcePackage: String? = null,
    val continueWatchingEnabled: Boolean = true,
    val dockBackgroundEnabled: Boolean = true,
    val uiScale: UiScale = UiScale.Auto,
    val weather: WeatherSnapshot? = null,
    val wallpaperUri: String? = null,
)
