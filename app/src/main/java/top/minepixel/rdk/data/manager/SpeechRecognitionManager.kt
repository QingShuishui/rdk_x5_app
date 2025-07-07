package top.minepixel.rdk.data.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "SpeechRecognitionManager"
    }
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionIntent: Intent? = null
    
    // 识别状态
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    // 识别结果
    private val _recognitionResult = MutableStateFlow<String?>(null)
    val recognitionResult: StateFlow<String?> = _recognitionResult.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        initializeSpeechRecognizer()
    }
    
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            
            recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString()) // 中文识别
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "准备开始语音识别")
                    _isListening.value = true
                }
                
                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "开始说话")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // 音量变化，可以用来显示音量指示器
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // 接收到音频数据
                }
                
                override fun onEndOfSpeech() {
                    Log.d(TAG, "说话结束")
                    _isListening.value = false
                }
                
                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "音频错误"
                        SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                        SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                        SpeechRecognizer.ERROR_NO_MATCH -> "没有匹配的结果"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙碌"
                        SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音超时"
                        else -> "未知错误: $error"
                    }
                    Log.e(TAG, "语音识别错误: $errorMessage")
                    _error.value = errorMessage
                    _isListening.value = false
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        Log.d(TAG, "语音识别结果: $recognizedText")
                        _recognitionResult.value = recognizedText
                    }
                    _isListening.value = false
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val partialText = matches[0]
                        Log.d(TAG, "部分识别结果: $partialText")
                        // 可以用来实时显示识别进度
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // 其他事件
                }
            })
            
            Log.d(TAG, "语音识别器初始化成功")
        } else {
            Log.e(TAG, "设备不支持语音识别")
            _error.value = "设备不支持语音识别"
        }
    }
    
    /**
     * 开始语音识别
     */
    fun startListening() {
        try {
            _recognitionResult.value = null
            _error.value = null
            speechRecognizer?.startListening(recognitionIntent)
            Log.d(TAG, "开始语音识别")
        } catch (e: Exception) {
            Log.e(TAG, "启动语音识别失败", e)
            _error.value = "启动语音识别失败: ${e.message}"
        }
    }
    
    /**
     * 停止语音识别
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
            Log.d(TAG, "停止语音识别")
        } catch (e: Exception) {
            Log.e(TAG, "停止语音识别失败", e)
        }
    }
    
    /**
     * 取消语音识别
     */
    fun cancel() {
        try {
            speechRecognizer?.cancel()
            _isListening.value = false
            Log.d(TAG, "取消语音识别")
        } catch (e: Exception) {
            Log.e(TAG, "取消语音识别失败", e)
        }
    }
    
    /**
     * 清除识别结果
     */
    fun clearResult() {
        _recognitionResult.value = null
        _error.value = null
    }
    
    /**
     * 释放资源
     */
    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
