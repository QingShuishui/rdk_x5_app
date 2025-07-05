package top.minepixel.rdk.data.repository

import kotlinx.coroutines.flow.Flow
import top.minepixel.rdk.data.model.*

/**
 * 机器人数据仓库接口
 */
interface RobotRepository {
    /**
     * 获取机器人实时状态
     */
    fun getRobotStatus(): Flow<RobotStatus>
    
    /**
     * 获取最新检测到的物品列表
     */
    fun getDetectedItems(): Flow<List<DetectedItem>>
    
    /**
     * 获取清洁任务列表
     */
    fun getCleaningTasks(): Flow<List<CleaningTask>>
    
    /**
     * 创建新的清洁任务
     */
    suspend fun createCleaningTask(task: CleaningTask): Result<CleaningTask>
    
    /**
     * 删除清洁任务
     */
    suspend fun deleteCleaningTask(taskId: String): Result<Boolean>
    
    /**
     * 更新任务状态
     */
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Boolean>
    
    /**
     * 发送控制命令到机器人
     */
    suspend fun sendCommand(command: RobotCommand): Result<Boolean>
    
    /**
     * 连接到机器人
     */
    suspend fun connectToRobot(robotId: String): Result<Boolean>
    
    /**
     * 断开与机器人的连接
     */
    suspend fun disconnectFromRobot(): Result<Boolean>
    
    /**
     * 获取历史清洁记录
     */
    suspend fun getCleaningHistory(limit: Int = 20): Result<List<CleaningTask>>
} 