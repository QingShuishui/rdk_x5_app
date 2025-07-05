package top.minepixel.rdk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import top.minepixel.rdk.data.model.DetectedItem
import top.minepixel.rdk.data.repository.RobotRepository
import javax.inject.Inject

@HiltViewModel
class DetectedItemsViewModel @Inject constructor(
    private val robotRepository: RobotRepository
) : ViewModel() {
    
    // 检测到的物品
    val detectedItems: StateFlow<List<DetectedItem>> = robotRepository
        .getDetectedItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
} 