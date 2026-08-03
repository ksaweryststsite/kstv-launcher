package pl.ksawery.ktvlauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.ksawery.ktvlauncher.data.AppRepository

class HomeViewModel(
    private val repository: AppRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshApps()
    }

    fun refreshApps() {
        viewModelScope.launch {
            _uiState.value = runCatching { repository.getLaunchableApps() }
                .fold(
                    onSuccess = HomeUiState::Ready,
                    onFailure = HomeUiState::Error,
                )
        }
    }
}

class HomeViewModelFactory(
    private val repository: AppRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java))
        return HomeViewModel(repository) as T
    }
}

