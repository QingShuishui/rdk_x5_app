package top.minepixel.rdk.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 机器人控制命令
 */
@JsonClass(generateAdapter = true)
data class RobotCommand(
    val action: CommandAction,
    val params: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 指令类型
 */
enum class CommandAction {
    @Json(name = "start_cleaning")
    START_CLEANING,
    
    @Json(name = "stop_cleaning")
    STOP_CLEANING,
    
    @Json(name = "pause_cleaning")
    PAUSE_CLEANING,
    
    @Json(name = "resume_cleaning")
    RESUME_CLEANING,
    
    @Json(name = "return_to_dock")
    RETURN_TO_DOCK,
    
    @Json(name = "set_mode")
    SET_MODE,
    
    @Json(name = "clean_spot")
    CLEAN_SPOT,
    
    @Json(name = "set_fan_speed")
    SET_FAN_SPEED,
    
    @Json(name = "move")
    MOVE,
    
    @Json(name = "locate")
    LOCATE,   // 让机器人发出声音便于找到它
    
    @Json(name = "voice_control")
    VOICE_CONTROL,  // 语音控制指令，会通过扣子平台解析
} 