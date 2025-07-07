package top.minepixel.rdk.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 字节跳动TTS请求
 */
@JsonClass(generateAdapter = true)
data class ByteDanceTtsRequest(
    val app: TtsAppConfig,
    val user: TtsUserConfig,
    val audio: TtsAudioConfig,
    val request: TtsRequestConfig
)

/**
 * TTS应用配置
 */
@JsonClass(generateAdapter = true)
data class TtsAppConfig(
    val appid: String,
    val token: String,
    val cluster: String = "volcano_tts"
)

/**
 * TTS用户配置
 */
@JsonClass(generateAdapter = true)
data class TtsUserConfig(
    val uid: String
)

/**
 * TTS音频配置
 */
@JsonClass(generateAdapter = true)
data class TtsAudioConfig(
    @Json(name = "voice_type")
    val voiceType: String = "zh_female_roumeinvyou_emo_v2_mars_bigtts",
    val encoding: String = "mp3",
    @Json(name = "speed_ratio")
    val speedRatio: Float = 1.0f,
    val rate: Int = 24000,
    val bitrate: Int = 160,
    @Json(name = "explicit_language")
    val explicitLanguage: String = "zh",
    @Json(name = "loudness_ratio")
    val loudnessRatio: Float = 1.0f,
    val emotion: String? = null,
    @Json(name = "enable_emotion")
    val enableEmotion: Boolean = false,
    @Json(name = "emotion_scale")
    val emotionScale: Float = 4.0f
)

/**
 * TTS请求配置
 */
@JsonClass(generateAdapter = true)
data class TtsRequestConfig(
    val reqid: String,
    val text: String,
    val operation: String = "query",
    @Json(name = "text_type")
    val textType: String? = null,
    @Json(name = "silence_duration")
    val silenceDuration: Float? = null,
    @Json(name = "with_timestamp")
    val withTimestamp: Int? = null,
    @Json(name = "extra_param")
    val extraParam: String? = null
)

/**
 * 字节跳动TTS响应
 */
@JsonClass(generateAdapter = true)
data class ByteDanceTtsResponse(
    val reqid: String,
    val code: Int,
    val message: String,
    val operation: String? = null,
    val sequence: Int? = null,
    val data: String? = null, // base64编码的音频数据
    val addition: TtsAdditionInfo? = null
)

/**
 * TTS附加信息
 */
@JsonClass(generateAdapter = true)
data class TtsAdditionInfo(
    val duration: String? = null
)

/**
 * TTS错误码枚举
 */
enum class TtsErrorCode(val code: Int, val description: String) {
    SUCCESS(3000, "请求正确"),
    INVALID_REQUEST(3001, "无效的请求"),
    CONCURRENT_LIMIT(3003, "并发超限"),
    SERVER_BUSY(3005, "后端服务忙"),
    SERVICE_INTERRUPTED(3006, "服务中断"),
    TEXT_TOO_LONG(3010, "文本长度超限"),
    INVALID_TEXT(3011, "无效文本"),
    PROCESSING_TIMEOUT(3030, "处理超时"),
    PROCESSING_ERROR(3031, "处理错误"),
    AUDIO_TIMEOUT(3032, "等待获取音频超时"),
    CONNECTION_ERROR(3040, "后端链路连接错误"),
    VOICE_NOT_FOUND(3050, "音色不存在");

    companion object {
        fun fromCode(code: Int): TtsErrorCode? {
            return values().find { it.code == code }
        }
    }
}

/**
 * TTS配置常量
 */
object TtsConstants {
    const val APP_ID = "2031301685"
    const val ACCESS_TOKEN = "0pNUqHNy9KkQUripWYQSWL7g6PrXQSU7"
    const val SECRET_KEY = "1LlQe1og6YqchcAd9yHLZav0uS7ah5DD"
    
    // 音色类型
    const val VOICE_TYPE_MALE = "zh_male_M392_conversation_wvae_bigtts"
    const val VOICE_TYPE_FEMALE = "zh_female_roumeinvyou_emo_v2_mars_bigtts"
    
    // 编码格式
    const val ENCODING_MP3 = "mp3"
    const val ENCODING_WAV = "wav"
    const val ENCODING_PCM = "pcm"
    const val ENCODING_OGG_OPUS = "ogg_opus"
    
    // 采样率
    const val RATE_8K = 8000
    const val RATE_16K = 16000
    const val RATE_24K = 24000
    
    // 语速范围
    const val MIN_SPEED_RATIO = 0.8f
    const val MAX_SPEED_RATIO = 2.0f
    
    // 音量范围
    const val MIN_LOUDNESS_RATIO = 0.5f
    const val MAX_LOUDNESS_RATIO = 2.0f
    
    // 情绪值范围
    const val MIN_EMOTION_SCALE = 1.0f
    const val MAX_EMOTION_SCALE = 5.0f
}
