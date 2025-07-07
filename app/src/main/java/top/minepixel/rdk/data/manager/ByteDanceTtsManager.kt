package top.minepixel.rdk.data.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import top.minepixel.rdk.data.api.ByteDanceTtsApiService
import top.minepixel.rdk.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 字节跳动TTS管理器
 */
@Singleton
class ByteDanceTtsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val byteDanceTtsApiService: ByteDanceTtsApiService,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    
    companion object {
        private const val TAG = "ByteDanceTtsManager"
        private const val TTS_CACHE_DIR = "tts_cache"
    }
    
    // TTS状态
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentText = MutableStateFlow("")
    val currentText: StateFlow<String> = _currentText.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 音频播放器
    private var mediaPlayer: MediaPlayer? = null
    
    // 音频管理器
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    // TTS缓存目录
    private val ttsCacheDir: File by lazy {
        File(context.cacheDir, TTS_CACHE_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * 合成并播放语音
     */
    suspend fun speakText(
        text: String,
        voiceType: String = TtsConstants.VOICE_TYPE_MALE,
        speedRatio: Float = 1.0f,
        loudnessRatio: Float = 1.0f
    ): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                Log.d(TAG, "开始TTS合成: $text")
                _currentText.value = text
                _error.value = null
                
                // 检查缓存
                val cacheKey = generateCacheKey(text, voiceType, speedRatio, loudnessRatio)
                val cachedFile = File(ttsCacheDir, "$cacheKey.mp3")
                
                val audioFile = if (cachedFile.exists()) {
                    Log.d(TAG, "使用缓存的TTS文件: ${cachedFile.absolutePath}")
                    cachedFile
                } else {
                    // 调用API合成语音
                    synthesizeSpeech(text, voiceType, speedRatio, loudnessRatio, cacheKey)
                }
                
                if (audioFile != null) {
                    // 播放音频
                    playAudioFile(audioFile)
                    Result.success(Unit)
                } else {
                    val error = "TTS合成失败"
                    _error.value = error
                    Result.failure(Exception(error))
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "TTS处理异常", e)
                _error.value = "TTS处理异常: ${e.message}"
                _currentText.value = ""
                Result.failure(e)
            }
        }
    }
    
    /**
     * 调用API合成语音
     */
    private suspend fun synthesizeSpeech(
        text: String,
        voiceType: String,
        speedRatio: Float,
        loudnessRatio: Float,
        cacheKey: String
    ): File? {
        return try {
            val request = ByteDanceTtsRequest(
                app = TtsAppConfig(
                    appid = TtsConstants.APP_ID,
                    token = TtsConstants.ACCESS_TOKEN,
                    cluster = "volcano_tts"
                ),
                user = TtsUserConfig(
                    uid = "user_${System.currentTimeMillis()}"
                ),
                audio = TtsAudioConfig(
                    voiceType = voiceType,
                    encoding = TtsConstants.ENCODING_MP3,
                    speedRatio = speedRatio.coerceIn(TtsConstants.MIN_SPEED_RATIO, TtsConstants.MAX_SPEED_RATIO),
                    rate = TtsConstants.RATE_24K,
                    bitrate = 160,
                    explicitLanguage = "zh",
                    loudnessRatio = loudnessRatio.coerceIn(TtsConstants.MIN_LOUDNESS_RATIO, TtsConstants.MAX_LOUDNESS_RATIO)
                ),
                request = TtsRequestConfig(
                    reqid = UUID.randomUUID().toString(),
                    text = text,
                    operation = "query"
                )
            )
            
            Log.d(TAG, "发送TTS请求: ${request.request.reqid}")
            val response = byteDanceTtsApiService.synthesizeSpeech(request = request)
            
            if (response.isSuccessful) {
                val ttsResponse = response.body()
                if (ttsResponse != null && ttsResponse.code == TtsErrorCode.SUCCESS.code) {
                    val audioData = ttsResponse.data
                    if (!audioData.isNullOrEmpty()) {
                        // 解码base64音频数据并保存到文件
                        val audioBytes = Base64.decode(audioData, Base64.DEFAULT)
                        val audioFile = File(ttsCacheDir, "$cacheKey.mp3")
                        
                        FileOutputStream(audioFile).use { fos ->
                            fos.write(audioBytes)
                        }
                        
                        Log.d(TAG, "TTS合成成功，音频文件: ${audioFile.absolutePath}, 大小: ${audioBytes.size} 字节")
                        audioFile
                    } else {
                        Log.e(TAG, "TTS响应中没有音频数据")
                        null
                    }
                } else {
                    val errorCode = TtsErrorCode.fromCode(ttsResponse?.code ?: -1)
                    val errorMsg = errorCode?.description ?: ttsResponse?.message ?: "未知错误"
                    Log.e(TAG, "TTS合成失败: $errorMsg (${ttsResponse?.code})")
                    _error.value = "TTS合成失败: $errorMsg"
                    null
                }
            } else {
                val errorMsg = "TTS API调用失败: ${response.code()} ${response.message()}"
                Log.e(TAG, errorMsg)
                _error.value = errorMsg
                null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "TTS API调用异常", e)
            _error.value = "TTS API调用异常: ${e.message}"
            null
        }
    }
    
    /**
     * 播放音频文件
     */
    private suspend fun playAudioFile(audioFile: File) {
        withContext(ioDispatcher) {
            try {
                stopPlaying()
                
                _isPlaying.value = true
                
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_ASSISTANT)
                            .build()
                    )
                    
                    setDataSource(audioFile.absolutePath)
                    
                    setOnPreparedListener {
                        Log.d(TAG, "MediaPlayer准备完成，开始播放")
                        start()
                    }
                    
                    setOnCompletionListener {
                        Log.d(TAG, "TTS播放完成")
                        _isPlaying.value = false
                        _currentText.value = ""
                    }
                    
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer播放错误: what=$what, extra=$extra")
                        _isPlaying.value = false
                        _currentText.value = ""
                        _error.value = "音频播放错误"
                        true
                    }
                    
                    prepareAsync()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "播放音频文件失败", e)
                _isPlaying.value = false
                _currentText.value = ""
                _error.value = "播放音频失败: ${e.message}"
            }
        }
    }
    
    /**
     * 停止播放
     */
    fun stopPlaying() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            _isPlaying.value = false
            _currentText.value = ""
        } catch (e: Exception) {
            Log.e(TAG, "停止播放失败", e)
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 生成缓存键
     */
    private fun generateCacheKey(
        text: String,
        voiceType: String,
        speedRatio: Float,
        loudnessRatio: Float
    ): String {
        val content = "$text-$voiceType-$speedRatio-$loudnessRatio"
        return content.hashCode().toString()
    }
    
    /**
     * 清理缓存
     */
    fun clearCache() {
        try {
            ttsCacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".mp3")) {
                    file.delete()
                }
            }
            Log.d(TAG, "TTS缓存清理完成")
        } catch (e: Exception) {
            Log.e(TAG, "清理TTS缓存失败", e)
        }
    }
}
