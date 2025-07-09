package top.minepixel.rdk.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import top.minepixel.rdk.data.model.DetectedItem
import top.minepixel.rdk.data.model.RobotCommand
import top.minepixel.rdk.data.model.RobotStatus
import top.minepixel.rdk.data.repository.RobotRepository
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val robotRepository: RobotRepository
) : ViewModel() {
    
    // 机器人状态
    val robotStatus: StateFlow<RobotStatus> = robotRepository
        .getRobotStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RobotStatus(
                id = "",
                name = "光净精灵",
                isOnline = false,
                battery = 0
            )
        )
    
    // 检测到的物品
    val detectedItems: StateFlow<List<DetectedItem>> = robotRepository
        .getDetectedItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        // 如果可能，自动连接到机器人
        viewModelScope.launch {
            connectToRobot("demo_robot_1")
        }
    }
    
    /**
     * 发送命令到机器人
     */
    fun sendCommand(command: RobotCommand) {
        viewModelScope.launch {
            robotRepository.sendCommand(command)
                .onSuccess {
                    Log.d(TAG, "命令发送成功: ${command.action}")
                }
                .onFailure { e ->
                    Log.e(TAG, "命令发送失败: ${command.action}", e)
                }
        }
    }
    
    /**
     * 连接到机器人
     */
    fun connectToRobot(robotId: String) {
        viewModelScope.launch {
            robotRepository.connectToRobot(robotId)
                .onSuccess {
                    Log.d(TAG, "连接到机器人成功: $robotId")
                }
                .onFailure { e ->
                    Log.e(TAG, "连接到机器人失败: $robotId", e)
                }
        }
    }
    
    /**
     * 断开与机器人的连接
     */
    fun disconnectFromRobot() {
        viewModelScope.launch {
            robotRepository.disconnectFromRobot()
                .onSuccess {
                    Log.d(TAG, "断开与机器人的连接成功")
                }
                .onFailure { e ->
                    Log.e(TAG, "断开与机器人的连接失败", e)
                }
        }
    }
} 