package top.minepixel.rdk.data.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.minepixel.rdk.data.manager.ByteDanceTtsManager
import top.minepixel.rdk.data.model.TtsConstants
import top.minepixel.rdk.data.model.VoiceAssistantState
import top.minepixel.rdk.data.model.VoiceMessage
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceAssistantManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val byteDanceTtsManager: ByteDanceTtsManager
) {
    
    companion object {
        private const val TAG = "VoiceAssistantManager"
        private const val AUDIO_FILE_NAME = "voice_recording.m4a" // 使用M4A格式支持百度识别
    }
    
    // 状态管理
    private val _state = MutableStateFlow(VoiceAssistantState.IDLE)
    val state: StateFlow<VoiceAssistantState> = _state.asStateFlow()
    
    // 消息历史
    private val _messages = MutableStateFlow<List<VoiceMessage>>(emptyList())
    val messages: StateFlow<List<VoiceMessage>> = _messages.asStateFlow()
    
    // 当前正在播放的文本
    private val _currentSpeakingText = MutableStateFlow("")
    val currentSpeakingText: StateFlow<String> = _currentSpeakingText.asStateFlow()
    
    // 录音相关
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null

    // 音频管理器
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    // TTS相关
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    // 协程作用域
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // TTS设置
    private val sharedPreferences by lazy {
        context.getSharedPreferences("tts_settings", Context.MODE_PRIVATE)
    }

    init {
        initializeTextToSpeech()
        setupAudioFile()
    }
    
    /**
     * 初始化TTS
     */
    private fun initializeTextToSpeech() {
        Log.d(TAG, "开始初始化TTS")
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TTS引擎初始化成功")

                // 尝试设置中文语言
                val chineseResult = textToSpeech?.setLanguage(Locale.CHINESE)
                val simplifiedChineseResult = textToSpeech?.setLanguage(Locale.SIMPLIFIED_CHINESE)

                isTtsInitialized = when {
                    chineseResult == TextToSpeech.LANG_AVAILABLE ||
                    chineseResult == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                    chineseResult == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                        Log.d(TAG, "中文语言设置成功")
                        true
                    }
                    simplifiedChineseResult == TextToSpeech.LANG_AVAILABLE ||
                    simplifiedChineseResult == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                    simplifiedChineseResult == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                        Log.d(TAG, "简体中文语言设置成功")
                        true
                    }
                    else -> {
                        Log.w(TAG, "中文语言不支持，使用默认语言")
                        textToSpeech?.setLanguage(Locale.getDefault())
                        true // 即使不支持中文也允许使用
                    }
                }

                // 设置TTS参数
                textToSpeech?.setSpeechRate(1.0f) // 正常语速
                textToSpeech?.setPitch(1.0f) // 正常音调

                Log.d(TAG, "TTS初始化${if (isTtsInitialized) "成功" else "失败"}")
            } else {
                Log.e(TAG, "TTS引擎初始化失败，状态码: $status")
                isTtsInitialized = false
            }
        }
    }
    
    /**
     * 设置音频文件
     */
    private fun setupAudioFile() {
        audioFile = File(context.cacheDir, AUDIO_FILE_NAME)
    }
    
    /**
     * 开始录音
     */
    fun startRecording(): Boolean {
        Log.d(TAG, "=== startRecording() 被调用 ===")
        return try {
            // 允许从IDLE或LISTENING状态开始录音（LISTENING可能是之前设置的）
            if (_state.value != VoiceAssistantState.IDLE && _state.value != VoiceAssistantState.LISTENING) {
                Log.w(TAG, "当前状态不允许开始录音: ${_state.value}")
                return false
            }

            Log.d(TAG, "状态检查通过，开始初始化录音")

            // 最终权限检查
            Log.d(TAG, "执行最终权限检查...")
            if (!hasRecordAudioPermission()) {
                Log.e(TAG, "最终权限检查失败，无录音权限")
                _state.value = VoiceAssistantState.IDLE
                return false
            }
            Log.d(TAG, "最终权限检查通过")

            // 设置为LISTENING状态
            _state.value = VoiceAssistantState.LISTENING
            Log.d(TAG, "状态设置为LISTENING")

            stopMediaPlayer()

            // 确保音频文件存在
            audioFile?.let { file ->
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "删除旧的音频文件")
                }

                // 确保父目录存在
                file.parentFile?.let { parentDir ->
                    if (!parentDir.exists()) {
                        parentDir.mkdirs()
                        Log.d(TAG, "创建父目录: ${parentDir.absolutePath}")
                    }
                }

                file.createNewFile()
                Log.d(TAG, "创建新的音频文件: ${file.absolutePath}")
            } ?: run {
                Log.e(TAG, "音频文件为null")
                return false
            }

            // 检查录音权限
            val hasRecordPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            val hasModifyAudioPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            ) == PackageManager.PERMISSION_GRANTED

            Log.d(TAG, "权限检查: RECORD_AUDIO=$hasRecordPermission, MODIFY_AUDIO_SETTINGS=$hasModifyAudioPermission")

            if (!hasRecordPermission) {
                Log.e(TAG, "没有录音权限 (RECORD_AUDIO)")
                _state.value = VoiceAssistantState.ERROR
                return false
            }

            if (!hasModifyAudioPermission) {
                Log.w(TAG, "没有音频设置权限 (MODIFY_AUDIO_SETTINGS)，但继续尝试录音")
            }

            Log.d(TAG, "权限检查通过，开始检查音频设备...")

            // 检查音频设备状态
            try {
                val isMicrophoneAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
                Log.d(TAG, "麦克风硬件可用: $isMicrophoneAvailable")

                if (!isMicrophoneAvailable) {
                    Log.e(TAG, "设备没有麦克风硬件")
                    _state.value = VoiceAssistantState.ERROR
                    return false
                }

                // 检查音频管理器状态
                val audioMode = audioManager.mode
                val isMicrophoneMute = audioManager.isMicrophoneMute
                Log.d(TAG, "音频模式: $audioMode, 麦克风静音: $isMicrophoneMute")

                if (isMicrophoneMute) {
                    Log.w(TAG, "麦克风被静音，尝试取消静音")
                    // 注意：在某些Android版本中，应用可能无法取消系统级静音
                }

            } catch (e: Exception) {
                Log.w(TAG, "音频设备检查失败: ${e.message}", e)
            }

            Log.d(TAG, "开始初始化MediaRecorder")

            // 启用真实的MediaRecorder录音，使用更兼容的配置
            try {
                Log.d(TAG, "创建MediaRecorder实例...")
                mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }

                Log.d(TAG, "配置MediaRecorder...")
                mediaRecorder?.apply {
                    // 设置音频源
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    Log.d(TAG, "音频源设置完成: MIC")

                    // 设置输出格式
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    Log.d(TAG, "输出格式设置完成: MPEG_4")

                    // 设置音频编码器
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    Log.d(TAG, "音频编码器设置完成: AAC")

                    // 设置输出文件
                    setOutputFile(audioFile?.absolutePath)
                    Log.d(TAG, "输出文件设置完成: ${audioFile?.absolutePath}")

                    // 设置音频参数 - 优化为百度API推荐参数
                    try {
                        setAudioSamplingRate(16000) // 16kHz采样率
                        Log.d(TAG, "采样率设置完成: 16000Hz")
                    } catch (e: Exception) {
                        Log.w(TAG, "设置采样率失败，使用默认值: ${e.message}")
                    }

                    try {
                        setAudioEncodingBitRate(64000) // 64kbps比特率
                        Log.d(TAG, "比特率设置完成: 64000bps")
                    } catch (e: Exception) {
                        Log.w(TAG, "设置比特率失败，使用默认值: ${e.message}")
                    }

                    try {
                        setAudioChannels(1) // 单声道
                        Log.d(TAG, "声道设置完成: 单声道")
                    } catch (e: Exception) {
                        Log.w(TAG, "设置声道失败，使用默认值: ${e.message}")
                    }

                    Log.d(TAG, "准备MediaRecorder...")
                    prepare()
                    Log.d(TAG, "MediaRecorder准备完成")

                    Log.d(TAG, "启动MediaRecorder...")
                    start()
                    Log.d(TAG, "MediaRecorder启动成功")
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "权限错误: ${e.message}", e)
                throw Exception("录音权限被拒绝，请在设置中授予麦克风权限")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "MediaRecorder状态错误: ${e.message}", e)
                // 尝试使用更简单的配置
                trySimpleRecorderConfig()
            } catch (e: RuntimeException) {
                Log.e(TAG, "MediaRecorder运行时错误: ${e.message}", e)
                // 尝试使用更简单的配置
                trySimpleRecorderConfig()
            } catch (e: Exception) {
                Log.e(TAG, "MediaRecorder配置失败: ${e.message}", e)
                throw Exception("录音初始化失败: ${e.message}")
            }

            // 状态已经在前面设置为LISTENING了，这里不需要重复设置
            Log.d(TAG, "录音启动成功，状态已设置为LISTENING")
            true
        } catch (e: Exception) {
            Log.e(TAG, "开始录音失败: ${e.message}", e)

            // 清理资源
            try {
                mediaRecorder?.release()
                mediaRecorder = null
            } catch (cleanupException: Exception) {
                Log.e(TAG, "清理MediaRecorder失败", cleanupException)
            }

            _state.value = VoiceAssistantState.IDLE // 错误时回到IDLE状态，而不是ERROR状态

            // 根据异常类型提供更具体的错误信息
            when {
                e.message?.contains("permission", true) == true -> {
                    Log.e(TAG, "录音权限被拒绝")
                }
                e.message?.contains("busy", true) == true -> {
                    Log.e(TAG, "录音设备被占用")
                }
                else -> {
                    Log.e(TAG, "录音配置错误: ${e.message}")
                }
            }

            false
        }
    }
    
    /**
     * 停止录音
     */
    fun stopRecording(): File? {
        Log.d(TAG, "stopRecording() 被调用")
        return try {
            // 启用真实的MediaRecorder停止操作
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            _state.value = VoiceAssistantState.PROCESSING
            Log.d(TAG, "录音停止，状态已更改为PROCESSING")

            // 返回录制的音频文件
            audioFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    Log.d(TAG, "返回录制的音频文件: ${file.absolutePath}, 大小: ${file.length()} 字节")
                    file
                } else {
                    Log.w(TAG, "音频文件不存在或为空")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "停止录音失败", e)
            _state.value = VoiceAssistantState.ERROR
            null
        }
    }
    
    /**
     * 播放录音
     */
    fun playRecording() {
        try {
            stopMediaPlayer()
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile?.absolutePath)
                prepare()
                setOnCompletionListener {
                    Log.d(TAG, "录音播放完成")
                }
                start()
            }
            Log.d(TAG, "开始播放录音")
        } catch (e: Exception) {
            Log.e(TAG, "播放录音失败", e)
        }
    }
    
    /**
     * 停止播放
     */
    private fun stopMediaPlayer() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }
    
    /**
     * 文本转语音 - 使用字节跳动TTS
     */
    fun speakText(text: String) {
        Log.d(TAG, "speakText() 被调用: $text")

        if (text.isBlank()) {
            Log.w(TAG, "文本为空，跳过TTS")
            _state.value = VoiceAssistantState.IDLE
            return
        }

        Log.d(TAG, "使用字节跳动TTS开始播放")
        _state.value = VoiceAssistantState.SPEAKING
        _currentSpeakingText.value = text

        // 使用协程调用字节跳动TTS
        managerScope.launch {
            try {
                // 获取用户设置
                val voiceType = sharedPreferences.getString("voice_type", TtsConstants.VOICE_TYPE_FEMALE)
                    ?: TtsConstants.VOICE_TYPE_FEMALE
                val speedRatio = sharedPreferences.getFloat("speed_ratio", 1.0f)
                val loudnessRatio = sharedPreferences.getFloat("loudness_ratio", 1.0f)

                val result = byteDanceTtsManager.speakText(
                    text = text,
                    voiceType = voiceType,
                    speedRatio = speedRatio,
                    loudnessRatio = loudnessRatio
                )

                result.onSuccess {
                    Log.d(TAG, "字节跳动TTS合成成功")
                    // 状态会在ByteDanceTtsManager中管理
                }.onFailure { e ->
                    Log.e(TAG, "字节跳动TTS合成失败", e)
                    _state.value = VoiceAssistantState.IDLE
                    _currentSpeakingText.value = ""
                }

            } catch (e: Exception) {
                Log.e(TAG, "字节跳动TTS调用异常", e)
                _state.value = VoiceAssistantState.IDLE
                _currentSpeakingText.value = ""
            }
        }

        // 监听字节跳动TTS播放状态
        managerScope.launch {
            byteDanceTtsManager.isPlaying.collect { isPlaying ->
                if (!isPlaying && _state.value == VoiceAssistantState.SPEAKING) {
                    Log.d(TAG, "字节跳动TTS播放完成")
                    _state.value = VoiceAssistantState.IDLE
                    _currentSpeakingText.value = ""
                }
            }
        }
    }

    /**
     * 文本转语音 - 使用Android原生TTS（备用方案）
     */
    fun speakTextWithNativeTts(text: String) {
        Log.d(TAG, "speakTextWithNativeTts() 被调用: $text")

        if (text.isBlank()) {
            Log.w(TAG, "文本为空，跳过TTS")
            _state.value = VoiceAssistantState.IDLE
            return
        }

        if (!isTtsInitialized) {
            Log.w(TAG, "TTS未初始化，尝试重新初始化")
            initializeTextToSpeech()
            // 延迟一段时间后重试
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isTtsInitialized) {
                    speakTextWithNativeTts(text)
                } else {
                    Log.e(TAG, "TTS初始化失败，无法播放语音")
                    _state.value = VoiceAssistantState.IDLE
                }
            }, 1000)
            return
        }

        Log.d(TAG, "原生TTS已初始化，开始播放")
        _state.value = VoiceAssistantState.SPEAKING
        _currentSpeakingText.value = text

        try {
            val utteranceId = "tts_${System.currentTimeMillis()}"

            // 设置TTS参数
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                putFloat("rate", 1.0f) // 正常语速
                putFloat("pitch", 1.0f) // 正常音调
            }

            // 设置TTS完成监听
            textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "原生TTS开始播放: $utteranceId")
                    _state.value = VoiceAssistantState.SPEAKING
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "原生TTS播放完成: $utteranceId")
                    _state.value = VoiceAssistantState.IDLE
                    _currentSpeakingText.value = ""
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "原生TTS播放错误: $utteranceId")
                    _state.value = VoiceAssistantState.IDLE
                    _currentSpeakingText.value = ""
                }
            })

            // 开始TTS播放
            val result = textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                params,
                utteranceId
            )

            if (result == TextToSpeech.ERROR) {
                Log.e(TAG, "原生TTS播放失败")
                _state.value = VoiceAssistantState.IDLE
                _currentSpeakingText.value = ""
            } else {
                Log.d(TAG, "原生TTS播放请求成功")

                // 设置一个备用定时器，防止TTS卡住
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (_state.value == VoiceAssistantState.SPEAKING) {
                        Log.d(TAG, "原生TTS播放超时，强制设置为IDLE")
                        _state.value = VoiceAssistantState.IDLE
                        _currentSpeakingText.value = ""
                    }
                }, 15000) // 15秒超时
            }

        } catch (e: Exception) {
            Log.e(TAG, "原生TTS播放异常", e)
            _state.value = VoiceAssistantState.IDLE
            _currentSpeakingText.value = ""
        }
    }
    
    /**
     * 停止TTS
     */
    fun stopSpeaking() {
        // 停止字节跳动TTS
        byteDanceTtsManager.stopPlaying()

        // 停止原生TTS
        textToSpeech?.stop()

        _state.value = VoiceAssistantState.IDLE
        _currentSpeakingText.value = ""
    }
    
    /**
     * 添加消息到历史
     */
    fun addMessage(message: VoiceMessage) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(message)
        _messages.value = currentMessages
    }
    
    /**
     * 清除消息历史
     */
    fun clearMessages() {
        _messages.value = emptyList()
    }
    
    /**
     * 设置状态
     */
    fun setState(newState: VoiceAssistantState) {
        _state.value = newState
    }

    /**
     * 尝试使用简单的录音配置
     */
    private fun trySimpleRecorderConfig() {
        Log.w(TAG, "尝试使用简单的录音配置...")
        try {
            // 清理之前的实例
            mediaRecorder?.release()

            // 创建新的MediaRecorder实例
            mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                // 使用最基本的配置
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)

                Log.d(TAG, "使用简单配置: THREE_GPP + AMR_NB")

                prepare()
                start()

                Log.d(TAG, "简单配置录音启动成功")
            }
        } catch (e: Exception) {
            Log.e(TAG, "简单配置也失败: ${e.message}", e)
            throw Exception("录音设备完全不可用: ${e.message}")
        }
    }

    /**
     * 检查是否有录音权限
     */
    fun hasRecordAudioPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "录音权限检查结果: $hasPermission")
        return hasPermission
    }

    /**
     * 检查是否有音频设置权限
     */
    fun hasModifyAudioPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "音频设置权限检查结果: $hasPermission")
        return hasPermission
    }

    /**
     * 释放资源
     */
    fun release() {
        stopMediaPlayer()
        mediaRecorder?.release()
        textToSpeech?.shutdown()
    }
}
