package top.minepixel.rdk.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import top.minepixel.rdk.data.manager.BaiduSpeechManager
import top.minepixel.rdk.data.manager.VoiceAssistantManager
import top.minepixel.rdk.data.model.*
import top.minepixel.rdk.data.repository.CozeRepository
import java.io.File
import javax.inject.Inject

private const val TAG = "RealVoiceAssistantVM"

@HiltViewModel
class RealVoiceAssistantViewModel @Inject constructor(
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
    
    // 错误状态
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 是否正在处理
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // 对话ID和聊天ID（用于扣子API）
    private val _conversationId = MutableStateFlow<String?>(null)
    private val _chatId = MutableStateFlow<String?>(null)

    init {
        // 监听百度语音识别结果
        viewModelScope.launch {
            baiduSpeechManager.recognitionResult.collect { result ->
                result?.let { recognizedText ->
                    Log.d(TAG, "收到百度语音识别结果: $recognizedText")
                    processRecognizedText(recognizedText)
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
                    _isProcessing.value = false
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
            voiceAssistantManager.setState(VoiceAssistantState.LISTENING)
            val success = voiceAssistantManager.startRecording()
            if (!success) {
                Log.e(TAG, "录音启动失败")
                _errorMessage.value = "开始录音失败"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            }
        }
    }

    /**
     * 停止录音并处理
     */
    fun stopRecordingAndProcess() {
        viewModelScope.launch {
            Log.d(TAG, "停止录音并开始处理")
            val audioFile = voiceAssistantManager.stopRecording()
            if (audioFile != null) {
                // 使用百度语音识别API识别音频
                recognizeAudioWithBaidu(audioFile)
            } else {
                _errorMessage.value = "录音失败"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            }
        }
    }
    
    /**
     * 使用百度语音识别API识别音频
     */
    private fun recognizeAudioWithBaidu(audioFile: File) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始百度语音识别")
                voiceAssistantManager.setState(VoiceAssistantState.PROCESSING)
                _isProcessing.value = true

                // 添加语音消息到历史
                val voiceMessage = VoiceMessage(
                    content = "[语音消息]",
                    isFromUser = true,
                    type = MessageType.AUDIO,
                    audioPath = audioFile.absolutePath
                )
                voiceAssistantManager.addMessage(voiceMessage)

                // 调用百度语音识别
                val result = baiduSpeechManager.recognizeSpeech(audioFile)

                result.onSuccess { recognizedText ->
                    Log.d(TAG, "百度语音识别成功: $recognizedText")
                    // 处理识别结果
                    processRecognizedText(recognizedText)
                }.onFailure { e ->
                    Log.e(TAG, "百度语音识别失败", e)
                    _errorMessage.value = "语音识别失败: ${e.message}"
                    voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                    _isProcessing.value = false
                }

            } catch (e: Exception) {
                Log.e(TAG, "百度语音识别异常", e)
                _errorMessage.value = "语音识别异常: ${e.message}"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                _isProcessing.value = false
            }
        }
    }

    /**
     * 处理识别到的文本
     */
    private fun processRecognizedText(recognizedText: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "处理识别文本: $recognizedText")

                // 添加识别结果消息到历史（不加前缀，UI会自动显示）
                val recognizedMessage = VoiceMessage(
                    content = recognizedText,
                    isFromUser = true
                )
                voiceAssistantManager.addMessage(recognizedMessage)

                // 发送给扣子API获取回复
                sendToCozeAPI(recognizedText)

            } catch (e: Exception) {
                Log.e(TAG, "处理识别文本失败", e)
                _errorMessage.value = "处理失败: ${e.message}"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * 发送给扣子API获取回复
     */
    private fun sendToCozeAPI(userInput: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "发送给扣子API: $userInput")

                // 获取对话历史
                val conversationHistory = buildConversationHistory()

                // 调用扣子API流式对话
                cozeRepository.createChatStream(
                    botId = DEFAULT_BOT_ID,
                    userId = DEFAULT_USER_ID,
                    message = userInput,
                    conversationHistory = conversationHistory
                ).collect { response ->
                    handleCozeResponse(response)
                }

            } catch (e: Exception) {
                Log.e(TAG, "扣子API调用失败", e)
                _errorMessage.value = "AI回复失败: ${e.message}"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                _isProcessing.value = false
            }
        }
    }

    /**
     * 处理扣子API响应
     */
    private fun handleCozeResponse(response: CozeResponse) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "收到扣子API响应: ${response.event}")

                when (response.event) {
                    "conversation.chat.created" -> {
                        // 对话创建成功
                        _conversationId.value = response.data?.conversationId
                        _chatId.value = response.data?.id
                        Log.d(TAG, "对话创建成功: conversationId=${response.data?.conversationId}, chatId=${response.data?.id}")
                    }

                    "conversation.message.delta" -> {
                        // 接收到消息增量
                        response.data?.delta?.content?.let { content ->
                            Log.d(TAG, "接收到消息增量: $content")
                            // 这里可以实时更新UI显示正在输入的内容
                        }
                    }

                    "conversation.message.completed" -> {
                        // 消息完成
                        response.data?.content?.let { rawContent ->
                            Log.d(TAG, "收到原始消息: ${rawContent.take(100)}")
                            Log.d(TAG, "消息类型: ${response.data?.type}")

                            // 只处理answer类型的消息，过滤掉function_call等类型
                            when (response.data?.type) {
                                "answer" -> {
                                    // 智能体回复
                                    if (response.data?.contentType == "text") {
                                        // 清理和过滤内容，移除函数调用信息
                                        val cleanedContent = cleanResponseContent(rawContent)

                                        if (cleanedContent.isNotBlank()) {
                                            Log.d(TAG, "清理后的消息: $cleanedContent")

                                            // 添加AI回复到消息历史
                                            val assistantMessage = VoiceMessage(
                                                content = cleanedContent,
                                                isFromUser = false
                                            )
                                            voiceAssistantManager.addMessage(assistantMessage)

                                            // 根据回复内容执行相应操作
                                            executeActionBasedOnResponse(cleanedContent)

                                            // 播放TTS回复
                                            voiceAssistantManager.speakText(cleanedContent)
                                        } else {
                                            Log.d(TAG, "内容被过滤，继续等待有效回复")
                                        }
                                    }
                                }
                                "function_call" -> {
                                    // 函数调用 - 静默处理，不显示给用户
                                    Log.d(TAG, "收到函数调用，静默处理")
                                }
                                "tool_response" -> {
                                    // 工具响应 - 静默处理，不显示给用户
                                    Log.d(TAG, "收到工具响应，静默处理")
                                }
                                "follow_up" -> {
                                    // 推荐问题 - 忽略不处理
                                    Log.d(TAG, "收到推荐问题，已忽略")
                                }
                                "verbose" -> {
                                    // 详细信息 - 忽略不处理
                                    Log.d(TAG, "收到详细信息，已忽略")
                                }
                                else -> {
                                    Log.d(TAG, "收到其他类型消息: ${response.data?.type}，已忽略")
                                }
                            }
                        }
                    }

                    "conversation.chat.completed" -> {
                        // 对话完成
                        Log.d(TAG, "对话完成")
                        _isProcessing.value = false
                        voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                    }

                    "conversation.chat.failed" -> {
                        // 对话失败
                        Log.e(TAG, "对话失败")
                        _errorMessage.value = "AI对话失败"
                        _isProcessing.value = false
                        voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                    }

                    else -> {
                        Log.d(TAG, "其他事件: ${response.event}")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "处理扣子API响应失败", e)
                _errorMessage.value = "处理AI回复失败: ${e.message}"
                _isProcessing.value = false
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            }
        }
    }
    
    /**
     * 根据AI回复执行相应操作
     */
    private fun executeActionBasedOnResponse(response: String) {
        Log.d(TAG, "根据AI回复执行操作: $response")

        // 这里可以根据AI的回复内容执行具体的机器人控制操作
        // 例如：
        when {
            response.contains("开始清洁") || response.contains("启动") -> {
                Log.d(TAG, "执行开始清洁操作")
                // TODO: 调用机器人开始清洁的API
            }
            response.contains("停止") || response.contains("暂停") -> {
                Log.d(TAG, "执行停止操作")
                // TODO: 调用机器人停止的API
            }
            response.contains("回到基站") || response.contains("充电") -> {
                Log.d(TAG, "执行回到基站操作")
                // TODO: 调用机器人回到基站的API
            }
            response.contains("找机器人") || response.contains("定位") -> {
                Log.d(TAG, "执行找机器人操作")
                // TODO: 调用机器人发声定位的API
            }
            else -> {
                Log.d(TAG, "无需执行特定操作")
            }
        }
    }

    /**
     * 构建对话历史
     */
    private fun buildConversationHistory(): List<CozeMessage> {
        return messages.value.mapNotNull { message ->
            // 只包含文本消息，排除语音消息和系统消息
            if (message.type == MessageType.TEXT || message.type == null) {
                CozeMessage(
                    role = if (message.isFromUser) "user" else "assistant",
                    content = message.content,
                    contentType = "text"
                )
            } else {
                null
            }
        }
    }

    /**
     * 发送文本消息
     */
    fun sendTextMessage(text: String) {
        sendToCozeAPI(text)
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
        _conversationId.value = null
        _chatId.value = null
        _errorMessage.value = null
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 清理响应内容，移除乱码和不需要的推荐内容以及函数调用信息
     */
    private fun cleanResponseContent(content: String): String {
        var cleaned = content.trim()

        try {
            Log.d(TAG, "开始清理内容，原始长度: ${content.length}")

            // 1. 检查是否是JSON格式的系统消息或函数调用信息，直接过滤掉
            if (cleaned.startsWith("{") || cleaned.contains("{\"name\":")) {
                Log.d(TAG, "检测到JSON格式内容，已过滤")
                return ""
            }

            // 2. 检查是否包含函数调用相关的关键词
            val functionCallKeywords = listOf(
                "plugin_id", "api_id", "function_call", "tool_call",
                "arguments", "plugin_name", "\"name\":", "\"arguments\":",
                "ts-1-w", "plugin_type", "api_name"
            )

            if (functionCallKeywords.any { cleaned.contains(it, ignoreCase = true) }) {
                Log.d(TAG, "检测到函数调用相关内容，已过滤")
                return ""
            }

            // 3. 检查是否包含特定的函数调用模式
            if (cleaned.matches(Regex(".*\"name\"\\s*:\\s*\".*\".*")) ||
                cleaned.matches(Regex(".*\"arguments\"\\s*:\\s*\\{.*")) ||
                cleaned.contains("7523587886646755380") ||
                cleaned.contains("7523587966820745266")) {
                Log.d(TAG, "检测到函数调用模式，已过滤")
                return ""
            }

            // 4. 检查是否是推荐问题（更精确的判断）
            if (cleaned.endsWith("？") || cleaned.endsWith("?")) {
                // 只过滤明显的推荐问题，不过滤正常的对话回复
                if (cleaned.length < 30 && (
                    cleaned.startsWith("您想了解") ||
                    cleaned.startsWith("需要我为您") ||
                    cleaned.startsWith("我可以为您") ||
                    cleaned.contains("推荐") ||
                    cleaned.contains("建议") ||
                    cleaned.matches(Regex(".*[如怎什么][何样么].*[吗呢？?]$"))
                )) {
                    Log.d(TAG, "检测到推荐问题，已过滤: ${cleaned.take(20)}")
                    return ""
                }
            }

            // 4. 移除常见的乱码字符
            cleaned = cleaned.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")

            // 5. 移除特殊控制字符
            cleaned = cleaned.replace(Regex("[\u0000-\u001F\u007F-\u009F]"), "")

            // 6. 移除推荐问题相关的内容
            cleaned = cleaned.replace(Regex("推荐问题[:：].*"), "")
            cleaned = cleaned.replace(Regex("相关推荐[:：].*"), "")
            cleaned = cleaned.replace(Regex("您还可以问[:：].*"), "")
            cleaned = cleaned.replace(Regex("建议问题[:：].*"), "")
            cleaned = cleaned.replace(Regex("您可以.*问我.*"), "")

            // 7. 移除多余的换行和空格
            cleaned = cleaned.replace(Regex("\\n{3,}"), "\n\n")
            cleaned = cleaned.replace(Regex("\\s{3,}"), " ")

            // 8. 移除开头和结尾的空白字符
            cleaned = cleaned.trim()

            // 9. 如果内容过长，截取前300个字符（语音播放考虑）
            if (cleaned.length > 300) {
                cleaned = cleaned.substring(0, 300) + "..."
                Log.d(TAG, "内容过长，已截取前300字符")
            }

            // 10. 最终检查：如果清理后内容太短或只包含标点，可能是无效内容
            if (cleaned.length < 5 || cleaned.matches(Regex("[\\s\\p{Punct}]*"))) {
                Log.d(TAG, "清理后内容无效，已过滤")
                return ""
            }

            Log.d(TAG, "内容清理完成，原长度: ${content.length}, 清理后长度: ${cleaned.length}")

        } catch (e: Exception) {
            Log.e(TAG, "清理内容时出错: ${e.message}", e)
            // 如果清理失败，返回空字符串，避免显示错误内容
            return ""
        }

        return cleaned
    }

    override fun onCleared() {
        super.onCleared()
        voiceAssistantManager.release()
    }
}
