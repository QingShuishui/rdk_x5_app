package top.minepixel.rdk.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import top.minepixel.rdk.data.model.CleaningMode
import top.minepixel.rdk.data.model.CleaningTask
import top.minepixel.rdk.data.model.TaskStatus
import top.minepixel.rdk.data.repository.RobotRepository
import java.util.UUID
import javax.inject.Inject

private const val TAG = "TasksViewModel"

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val robotRepository: RobotRepository
) : ViewModel() {
    
    // 清洁任务列表
    val tasks: StateFlow<List<CleaningTask>> = robotRepository
        .getCleaningTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * 创建新的清洁任务
     */
    fun createTask(task: CleaningTask) {
        viewModelScope.launch {
            robotRepository.createCleaningTask(task)
                .onSuccess { createdTask ->
                    Log.d(TAG, "任务创建成功: ${createdTask.id}")
                }
                .onFailure { e ->
                    Log.e(TAG, "任务创建失败", e)
                }
        }
    }
    
    /**
     * 添加任务
     */
    fun addTask(name: String, startTime: Long, room: String, mode: CleaningMode) {
        val task = CleaningTask(
            id = UUID.randomUUID().toString(),
            name = name,
            rooms = listOf(room),
            startTime = startTime,
            status = TaskStatus.PENDING,
            mode = mode,
            avoidItems = true
        )
        createTask(task)
    }
    
    /**
     * 删除任务
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            robotRepository.deleteCleaningTask(taskId)
                .onSuccess {
                    Log.d(TAG, "任务删除成功: $taskId")
                }
                .onFailure { e ->
                    Log.e(TAG, "任务删除失败: $taskId", e)
                }
        }
    }
    
    /**
     * 切换任务启用状态
     */
    fun toggleTaskEnabled(taskId: String) {
        val task = tasks.value.find { it.id == taskId } ?: return
        
        // 简单起见，这里只是切换任务状态
        val newStatus = if (task.status == TaskStatus.PENDING) {
            TaskStatus.CANCELLED
        } else {
            TaskStatus.PENDING
        }
        
        updateTaskStatus(taskId, newStatus)
    }
    
    /**
     * 更新任务状态
     */
    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            robotRepository.updateTaskStatus(taskId, status)
                .onSuccess { success ->
                    Log.d(TAG, "任务状态更新成功: $taskId -> $status")
                }
                .onFailure { e ->
                    Log.e(TAG, "任务状态更新失败: $taskId -> $status", e)
                }
        }
    }
} 