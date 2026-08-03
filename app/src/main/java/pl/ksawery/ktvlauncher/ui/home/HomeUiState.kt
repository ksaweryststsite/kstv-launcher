package pl.ksawery.ktvlauncher.ui.home

import pl.ksawery.ktvlauncher.model.LaunchableApp

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(val apps: List<LaunchableApp>) : HomeUiState
    data class Error(val cause: Throwable) : HomeUiState
}

