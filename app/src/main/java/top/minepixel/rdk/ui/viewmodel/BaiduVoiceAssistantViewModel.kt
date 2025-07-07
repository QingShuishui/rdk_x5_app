package top.minepixel.rdk.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.minepixel.rdk.data.manager.BaiduSpeechManager
import top.minepixel.rdk.data.manager.VoiceAssistantManager
import top.minepixel.rdk.data.model.*
import top.minepixel.rdk.data.repository.CozeRepository
import java.io.File
import javax.inject.Inject

private const val TAG = "BaiduVoiceAssistantVM"

@HiltViewModel
class BaiduVoiceAssistantViewModel @Inject constructor(
    private val voiceAssistantManager: VoiceAssistantManager,
    private val baiduSpeechManager: BaiduSpeechManager,
    private val cozeRepository: CozeRepository
) : ViewModel() {
    
    companion object {
        private const val DEFAULT_BOT_ID = "7523579176386576419"
        private const val DEFAULT_USER_ID = "user_001"
    }
    
    // 语音助手状态
    val assistantState: StateFlow<VoiceAssistantState> = voiceAssistantManager.state
    
    // 消息历史
    val messages: StateFlow<List<VoiceMessage>> = voiceAssistantManager.messages
    
    // 当前正在播放的文本
    val currentSpeakingText: StateFlow<String> = voiceAssistantManager.currentSpeakingText
    
    // 百度语音识别状态
    val isRecognizing: StateFlow<Boolean> = baiduSpeechManager.isRecognizing
    
    // 错误状态
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 是否正在处理
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    init {
        // 监听百度语音识别结果
        viewModelScope.launch {
            baiduSpeechManager.recognitionResult.collect { result ->
                result?.let { recognizedText ->
                    Log.d(TAG, "收到百度语音识别结果: $recognizedText")
                    processRecognizedText(recognizedText)
                    baiduSpeechManager.clearResult()
                }
            }
        }
        
        // 监听百度语音识别错误
        viewModelScope.launch {
            baiduSpeechManager.error.collect { error ->
                error?.let {
                    Log.e(TAG, "百度语音识别错误: $it")
                    _errorMessage.value = "语音识别错误: $it"
                    voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                }
            }
        }
    }
    
    /**
     * 开始录音
     */
    fun startRecording() {
        viewModelScope.launch {
            Log.d(TAG, "开始录音")
            val success = voiceAssistantManager.startRecording()
            if (!success) {
                _errorMessage.value = "开始录音失败"
            }
        }
    }
    
    /**
     * 停止录音并处理
     */
    fun stopRecordingAndProcess() {
        viewModelScope.launch {
            Log.d(TAG, "停止录音并处理")
            val audioFile = voiceAssistantManager.stopRecording()
            
            if (audioFile != null && audioFile.exists()) {
                // 使用百度语音识别
                recognizeWithBaidu(audioFile)
            } else {
                _errorMessage.value = "录音文件无效"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            }
        }
    }
    
    /**
     * 使用百度语音识别
     */
    private fun recognizeWithBaidu(audioFile: File) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始百度语音识别")
                voiceAssistantManager.setState(VoiceAssistantState.PROCESSING)
                
                // 注意：不在这里添加消息，等识别完成后统一添加
                
                // 调用百度语音识别
                val result = baiduSpeechManager.recognizeSpeech(audioFile)
                
                result.onSuccess { recognizedText ->
                    Log.d(TAG, "百度语音识别成功: $recognizedText")
                    if (recognizedText.isNotBlank()) {
                        processRecognizedText(recognizedText)
                    } else {
                        _errorMessage.value = "未识别到有效语音内容"
                        voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "百度语音识别失败", e)
                    _errorMessage.value = "语音识别失败: ${e.message}"
                    voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "语音识别异常", e)
                _errorMessage.value = "语音识别异常: ${e.message}"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            }
        }
    }
    
    /**
     * 处理识别到的文本
     */
    private fun processRecognizedText(recognizedText: String) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                _errorMessage.value = null

                Log.d(TAG, "处理识别文本: $recognizedText")

                // 添加语音消息到消息历史，统一格式
                val voiceMessage = VoiceMessage(
                    content = "语音消息：$recognizedText",
                    isFromUser = true,
                    type = MessageType.AUDIO
                )
                voiceAssistantManager.addMessage(voiceMessage)

                // 异步发送给Coze API获取智能回复，避免阻塞
                launch {
                    sendToCozeAPI(recognizedText)
                }

            } catch (e: Exception) {
                Log.e(TAG, "处理识别文本失败", e)
                _errorMessage.value = "处理失败: ${e.message}"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                _isProcessing.value = false
            }
        }
    }

    /**
     * 发送给Coze API获取回复
     */
    private fun sendToCozeAPI(userInput: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "发送给Coze API: $userInput")

                // 在主线程设置状态
                withContext(Dispatchers.Main) {
                    voiceAssistantManager.setState(VoiceAssistantState.PROCESSING)
                }

                // 在IO线程调用Coze API
                cozeRepository.createChatStream(
                    botId = DEFAULT_BOT_ID,
                    userId = DEFAULT_USER_ID,
                    message = userInput,
                    conversationHistory = emptyList()
                ).collect { response ->
                    // 在主线程处理响应
                    withContext(Dispatchers.Main) {
                        handleCozeResponse(response)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Coze API调用失败", e)
                // 在主线程处理错误
                withContext(Dispatchers.Main) {
                    // 如果API失败，使用本地回复
                    val fallbackReply = generateReply(userInput)
                    val assistantMessage = VoiceMessage(
                        content = fallbackReply,
                        isFromUser = false
                    )
                    voiceAssistantManager.addMessage(assistantMessage)
                    voiceAssistantManager.speakText(fallbackReply)
                    voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                    _isProcessing.value = false
                }
            }
        }
    }

    /**
     * 处理Coze API响应
     */
    private fun handleCozeResponse(response: CozeResponse) {
        try {
            Log.d(TAG, "收到Coze响应: ${response.event}")

            when (response.event) {
                "conversation.message.completed" -> {
                    response.data?.content?.let { content ->
                        if (response.data?.type == "answer" && content.isNotBlank()) {
                            Log.d(TAG, "✅ 收到AI回复: $content")

                            // 添加AI回复到消息历史
                            val assistantMessage = VoiceMessage(
                                content = content,
                                isFromUser = false
                            )
                            voiceAssistantManager.addMessage(assistantMessage)

                            // 播放TTS回复
                            voiceAssistantManager.speakText(content)
                            voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                            _isProcessing.value = false
                        }
                    }
                }
                "conversation.chat.completed" -> {
                    Log.d(TAG, "🏁 对话完成")
                    voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                    _isProcessing.value = false
                }
                "error" -> {
                    Log.e(TAG, "❌ Coze API错误")
                    _errorMessage.value = "AI回复失败"
                    voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                    _isProcessing.value = false
                }
                else -> {
                    Log.d(TAG, "📋 其他事件: ${response.event}")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "处理Coze响应失败", e)
            voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            _isProcessing.value = false
        }
    }

    /**
     * 生成智能回复（备用方案）
     */
    private fun generateReply(userInput: String): String {
        return when {
            userInput.contains("清洁") || userInput.contains("扫地") || userInput.contains("打扫") -> {
                "好的，我来帮您启动扫地机器人开始清洁。"
            }
            userInput.contains("停止") || userInput.contains("暂停") -> {
                "好的，我来帮您停止机器人工作。"
            }
            userInput.contains("回到") || userInput.contains("充电") || userInput.contains("回家") -> {
                "好的，我来让机器人返回充电座。"
            }
            userInput.contains("找") || userInput.contains("定位") || userInput.contains("在哪") -> {
                "好的，我来让机器人发出声音帮助您定位。"
            }
            userInput.contains("你好") || userInput.contains("hello") -> {
                "您好！我是您的智能语音助手，可以帮您控制扫地机器人。"
            }
            userInput.contains("谢谢") || userInput.contains("感谢") -> {
                "不客气，很高兴为您服务！"
            }
            userInput.contains("天气") -> {
                "抱歉，我主要负责控制扫地机器人，天气信息请咨询其他助手。"
            }
            userInput.contains("时间") -> {
                "当前时间是${java.text.SimpleDateFormat("HH:mm", java.util.Locale.CHINA).format(java.util.Date())}"
            }
            else -> {
                "我收到了您的指令：$userInput。正在为您处理中..."
            }
        }
    }
    
    /**
     * 发送文本消息
     */
    fun sendTextMessage(text: String) {
        processRecognizedText(text)
    }
    
    /**
     * 停止语音播放
     */
    fun stopSpeaking() {
        Log.d(TAG, "停止语音播放")
        voiceAssistantManager.stopSpeaking()
    }
    
    /**
     * 清除对话历史
     */
    fun clearConversation() {
        voiceAssistantManager.clearMessages()
        baiduSpeechManager.clearResult()
        _errorMessage.value = null
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
        baiduSpeechManager.clearResult()
    }
    
    override fun onCleared() {
        super.onCleared()
        voiceAssistantManager.release()
    }
}
