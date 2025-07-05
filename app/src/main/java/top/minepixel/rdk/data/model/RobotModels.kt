package top.minepixel.rdk.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 机器人状态信息
 */
@JsonClass(generateAdapter = true)
data class RobotStatus(
    val id: String,
    val name: String,
    val isOnline: Boolean = false,
    val battery: Int = 0,
    val mode: RobotMode = RobotMode.IDLE,
    val position: RobotPosition? = null,
    val environment: EnvironmentInfo? = null,
    val cleaningProgress: Int = 0,
    val errorCode: Int? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * 机器人位置信息
 */
@JsonClass(generateAdapter = true)
data class RobotPosition(
    val x: Double,
    val y: Double,
    val orientation: Double, // 方向角度，0-360度
    val roomId: String? = null
)

/**
 * 环境信息（温湿度等）
 */
@JsonClass(generateAdapter = true)
data class EnvironmentInfo(
    val temperature: Float, // 摄氏度
    val humidity: Float, // 百分比
    val dustLevel: Int = 0 // 灰尘等级 0-100
)

/**
 * 识别到的物品信息
 */
@JsonClass(generateAdapter = true)
data class DetectedItem(
    val id: String,
    val type: ItemType,
    val confidence: Float, // 0-1
    val position: RobotPosition,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 清洁任务定义
 */
@JsonClass(generateAdapter = true)
data class CleaningTask(
    val id: String,
    val name: String,
    val rooms: List<String>, // 房间ID列表
    val mode: CleaningMode,
    val startTime: Long? = null, // 可以是定时任务
    val status: TaskStatus = TaskStatus.PENDING,
    val avoidItems: Boolean = true // 是否避开检测到的贵重物品
)

/**
 * 机器人工作模式
 */
enum class RobotMode {
    @Json(name = "idle")
    IDLE,
    
    @Json(name = "cleaning")
    CLEANING,
    
    @Json(name = "charging")
    CHARGING,
    
    @Json(name = "returning")
    RETURNING_TO_DOCK,
    
    @Json(name = "error")
    ERROR
}

/**
 * 清洁模式
 */
enum class CleaningMode {
    @Json(name = "standard")
    STANDARD,
    
    @Json(name = "deep")
    DEEP,
    
    @Json(name = "quick")
    QUICK,
    
    @Json(name = "edge")
    EDGE,
    
    @Json(name = "spot")
    SPOT
}

/**
 * 任务状态
 */
enum class TaskStatus {
    @Json(name = "pending")
    PENDING,
    
    @Json(name = "in_progress")
    IN_PROGRESS,
    
    @Json(name = "completed")
    COMPLETED,
    
    @Json(name = "failed")
    FAILED,
    
    @Json(name = "cancelled")
    CANCELLED
}

/**
 * 物品类型
 */
enum class ItemType {
    @Json(name = "jewelry")
    JEWELRY,
    
    @Json(name = "earphone")
    EARPHONE,
    
    @Json(name = "key")
    KEY,
    
    @Json(name = "wallet")
    WALLET,
    
    @Json(name = "other")
    OTHER
} 