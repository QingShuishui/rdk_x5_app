package top.minepixel.rdk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    fun updateMessage(message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(message = message)
        }
    }
}

data class MainUiState(
    val message: String = "欢迎使用RDK项目",
    val isLoading: Boolean = false
) 