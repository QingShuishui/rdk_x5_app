package top.minepixel.rdk.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 百度语音识别请求
 */
@JsonClass(generateAdapter = true)
data class BaiduSpeechRequest(
    val format: String = "m4a", // 音频格式：pcm/wav/amr/m4a
    val rate: Int = 16000, // 采样率：16000、8000
    val channel: Int = 1, // 声道数：固定值1
    val cuid: String, // 用户唯一标识
    val token: String, // access_token
    @Json(name = "dev_pid")
    val devPid: Int = 1537, // 识别模型：1537普通话
    val speech: String, // base64编码的音频数据
    val len: Int // 原始音频字节数
)

/**
 * 百度语音识别响应
 */
@JsonClass(generateAdapter = true)
data class BaiduSpeechResponse(
    @Json(name = "err_no")
    val errNo: Int,
    @Json(name = "err_msg")
    val errMsg: String,
    val sn: String,
    @Json(name = "corpus_no")
    val corpusNo: String? = null,
    val result: List<String>? = null
)

/**
 * 百度Access Token请求
 */
@JsonClass(generateAdapter = true)
data class BaiduTokenRequest(
    @Json(name = "grant_type")
    val grantType: String = "client_credentials",
    @Json(name = "client_id")
    val clientId: String, // API Key
    @Json(name = "client_secret")
    val clientSecret: String // Secret Key
)

/**
 * 百度Access Token响应
 */
@JsonClass(generateAdapter = true)
data class BaiduTokenResponse(
    @Json(name = "access_token")
    val accessToken: String,
    @Json(name = "expires_in")
    val expiresIn: Int,
    @Json(name = "refresh_token")
    val refreshToken: String? = null,
    val scope: String? = null,
    @Json(name = "session_key")
    val sessionKey: String? = null,
    @Json(name = "session_secret")
    val sessionSecret: String? = null,
    val error: String? = null,
    @Json(name = "error_description")
    val errorDescription: String? = null
)
