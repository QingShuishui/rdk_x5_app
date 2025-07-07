package top.minepixel.rdk.data.manager

import android.content.Context
import android.util.Base64
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import top.minepixel.rdk.data.api.BaiduSpeechApiService
import top.minepixel.rdk.data.api.BaiduTokenApiService
import top.minepixel.rdk.data.model.BaiduSpeechRequest
import top.minepixel.rdk.data.model.BaiduSpeechResponse
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class BaiduSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val baiduTokenApiService: BaiduTokenApiService,
    private val baiduSpeechApiService: BaiduSpeechApiService,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    
    companion object {
        private const val TAG = "BaiduSpeechManager"
        private const val TOKEN_CACHE_KEY = "baidu_access_token"
        private const val TOKEN_EXPIRE_KEY = "baidu_token_expire"
    }
    
    // Access Token缓存
    private var cachedAccessToken: String? = null
    private var tokenExpireTime: Long = 0
    
    // 识别状态
    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()
    
    // 识别结果
    private val _recognitionResult = MutableStateFlow<String?>(null)
    val recognitionResult: StateFlow<String?> = _recognitionResult.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 获取Access Token
     */
    private suspend fun getAccessToken(): String? {
        return withContext(ioDispatcher) {
            try {
                // 检查缓存的token是否有效
                val currentTime = System.currentTimeMillis()
                if (cachedAccessToken != null && currentTime < tokenExpireTime) {
                    Log.d(TAG, "使用缓存的Access Token")
                    return@withContext cachedAccessToken
                }
                
                Log.d(TAG, "获取新的Access Token")
                val response = baiduTokenApiService.getAccessToken()
                
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null && tokenResponse.error == null) {
                        cachedAccessToken = tokenResponse.accessToken
                        tokenExpireTime = currentTime + (tokenResponse.expiresIn - 300) * 1000L // 提前5分钟过期
                        
                        Log.d(TAG, "Access Token获取成功，有效期: ${tokenResponse.expiresIn}秒")
                        return@withContext cachedAccessToken
                    } else {
                        Log.e(TAG, "Token响应错误: ${tokenResponse?.error} - ${tokenResponse?.errorDescription}")
                        return@withContext null
                    }
                } else {
                    Log.e(TAG, "获取Access Token失败: ${response.code()} ${response.message()}")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取Access Token异常", e)
                return@withContext null
            }
        }
    }
    
    /**
     * 识别语音文件
     */
    suspend fun recognizeSpeech(audioFile: File): Result<String> {
        return withContext(ioDispatcher) {
            try {
                _isRecognizing.value = true
                _error.value = null
                
                Log.d(TAG, "开始语音识别，文件: ${audioFile.absolutePath}")
                
                // 获取Access Token
                val accessToken = getAccessToken()
                if (accessToken == null) {
                    _error.value = "获取Access Token失败"
                    return@withContext Result.failure(Exception("获取Access Token失败"))
                }
                
                // 读取音频文件
                if (!audioFile.exists()) {
                    _error.value = "音频文件不存在"
                    return@withContext Result.failure(Exception("音频文件不存在"))
                }
                
                val audioBytes = audioFile.readBytes()
                val audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
                
                Log.d(TAG, "音频文件大小: ${audioBytes.size} 字节")
                
                // 构建请求
                val audioFormat = getAudioFormat(audioFile.name)
                Log.d(TAG, "音频文件: ${audioFile.name}, 识别格式: $audioFormat")

                val request = BaiduSpeechRequest(
                    format = audioFormat,
                    rate = 16000,
                    channel = 1,
                    cuid = getDeviceId(),
                    token = accessToken,
                    devPid = 1537, // 普通话模型
                    speech = audioBase64,
                    len = audioBytes.size
                )
                
                // 发送识别请求
                Log.d(TAG, "发送语音识别请求到: ${BaiduSpeechApiService.SPEECH_BASE_URL}server_api")
                val response = baiduSpeechApiService.recognizeSpeech(request)
                
                if (response.isSuccessful) {
                    val speechResponse = response.body()
                    if (speechResponse != null) {
                        Log.d(TAG, "收到百度API响应: errNo=${speechResponse.errNo}, errMsg=${speechResponse.errMsg}")

                        if (speechResponse.errNo == 0) {
                            val result = speechResponse.result?.firstOrNull() ?: ""
                            if (result.isNotBlank()) {
                                Log.d(TAG, "语音识别成功: $result")
                                _recognitionResult.value = result
                                return@withContext Result.success(result)
                            } else {
                                val errorMsg = "识别结果为空"
                                Log.w(TAG, errorMsg)
                                _error.value = errorMsg
                                return@withContext Result.failure(Exception(errorMsg))
                            }
                        } else {
                            val errorMsg = when (speechResponse.errNo) {
                                3300 -> "输入参数不正确"
                                3301 -> "音频质量过差"
                                3302 -> "鉴权失败"
                                3303 -> "语音服务器后端问题"
                                3304 -> "用户的请求QPS超限"
                                3305 -> "用户的日pv（日请求量）超限"
                                3307 -> "语音过长"
                                3308 -> "音频无效"
                                3309 -> "音频文件过大"
                                3310 -> "音频文件下载失败"
                                3311 -> "音频时长过短"
                                3312 -> "音频格式不支持"
                                else -> "识别失败: ${speechResponse.errMsg} (${speechResponse.errNo})"
                            }
                            Log.e(TAG, errorMsg)
                            _error.value = errorMsg
                            return@withContext Result.failure(Exception(errorMsg))
                        }
                    } else {
                        _error.value = "响应为空"
                        return@withContext Result.failure(Exception("响应为空"))
                    }
                } else {
                    val errorMsg = "请求失败: ${response.code()} ${response.message()}"
                    Log.e(TAG, errorMsg)
                    _error.value = errorMsg
                    return@withContext Result.failure(Exception(errorMsg))
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "语音识别异常", e)
                _error.value = "识别异常: ${e.message}"
                return@withContext Result.failure(e)
            } finally {
                _isRecognizing.value = false
            }
        }
    }
    
    /**
     * 根据文件名获取音频格式
     */
    private fun getAudioFormat(fileName: String): String {
        return when {
            fileName.endsWith(".pcm", true) -> "pcm"
            fileName.endsWith(".wav", true) -> "wav"
            fileName.endsWith(".amr", true) -> "amr"
            fileName.endsWith(".m4a", true) -> "m4a"
            fileName.endsWith(".mp4", true) -> "m4a" // MP4容器通常包含AAC音频，使用m4a格式
            fileName.endsWith(".aac", true) -> "m4a" // AAC编码使用m4a格式
            fileName.endsWith(".3gp", true) -> "amr"
            else -> {
                Log.w(TAG, "未知音频格式: $fileName，使用默认格式 m4a")
                "m4a" // 默认使用m4a格式，因为我们录制的是AAC编码
            }
        }
    }
    
    /**
     * 获取设备唯一标识
     */
    private fun getDeviceId(): String {
        return try {
            val androidId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
            androidId ?: UUID.randomUUID().toString()
        } catch (e: Exception) {
            UUID.randomUUID().toString()
        }
    }
    
    /**
     * 清除识别结果
     */
    fun clearResult() {
        _recognitionResult.value = null
        _error.value = null
    }
}
