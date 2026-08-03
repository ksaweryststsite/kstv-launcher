package pl.ksawery.ktvlauncher.ui.home

import pl.ksawery.ktvlauncher.model.LaunchableApp
import pl.ksawery.ktvlauncher.model.WeatherSnapshot

data class HomeUiState(
    val isLoading: Boolean = true,
    val apps: List<LaunchableApp> = emptyList(),
    val favorites: List<LaunchableApp> = emptyList(),
    val recent: List<LaunchableApp> = emptyList(),
    val weather: WeatherSnapshot? = null,
    val wallpaperUri: String? = null,
)

