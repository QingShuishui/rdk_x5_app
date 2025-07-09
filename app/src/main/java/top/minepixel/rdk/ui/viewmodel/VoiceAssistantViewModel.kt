package top.minepixel.rdk.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import top.minepixel.rdk.data.manager.VoiceAssistantManager
import top.minepixel.rdk.data.model.*
import top.minepixel.rdk.data.repository.CozeRepository
import java.io.File
import javax.inject.Inject

private const val TAG = "VoiceAssistantViewModel"

@HiltViewModel
class VoiceAssistantViewModel @Inject constructor(
    private val voiceAssistantManager: VoiceAssistantManager,
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
    
    // 当前播放的文本
    val currentSpeakingText: StateFlow<String> = voiceAssistantManager.currentSpeakingText
    
    // 对话状态
    private val _conversationId = MutableStateFlow<String?>(null)
    val conversationId: StateFlow<String?> = _conversationId.asStateFlow()
    
    private val _chatId = MutableStateFlow<String?>(null)
    val chatId: StateFlow<String?> = _chatId.asStateFlow()
    
    // 错误状态
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 是否正在处理
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    /**
     * 开始录音
     */
    fun startRecording() {
        viewModelScope.launch {
            Log.d(TAG, "ViewModel: 开始录音请求")
            val success = voiceAssistantManager.startRecording()
            if (!success) {
                Log.e(TAG, "ViewModel: 录音启动失败")
                _errorMessage.value = "开始录音失败"
            } else {
                Log.d(TAG, "ViewModel: 录音启动成功")
            }
        }
    }
    
    /**
     * 停止录音并处理
     */
    fun stopRecordingAndProcess() {
        viewModelScope.launch {
            val audioFile = voiceAssistantManager.stopRecording()
            if (audioFile != null) {
                // 直接发送音频文件到扣子API
                processVoiceInput(audioFile)
            } else {
                _errorMessage.value = "录音失败"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            }
        }
    }

    /**
     * 处理语音输入（直接发送音频文件）
     */
    fun processVoiceInput(audioFile: File) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始处理语音输入: ${audioFile.absolutePath}")
                _isProcessing.value = true
                _errorMessage.value = null

                // 模拟处理过程
                Log.d(TAG, "开始模拟处理延迟...")
                kotlinx.coroutines.delay(2000)

                // 模拟语音识别结果
                val recognizedText = "开始清洁" // 模拟识别到的文本
                Log.d(TAG, "模拟识别结果: $recognizedText")

                // 只添加识别结果到消息历史（避免重复消息）
                val userMessage = VoiceMessage(
                    content = recognizedText,
                    isFromUser = true,
                    type = MessageType.TEXT  // 识别后的文本消息
                )
                voiceAssistantManager.addMessage(userMessage)

                // 模拟AI回复
                val assistantMessage = VoiceMessage(
                    content = "好的，我来帮您启动扫地机器人开始清洁。",
                    isFromUser = false
                )
                voiceAssistantManager.addMessage(assistantMessage)

                Log.d(TAG, "开始播放TTS回复")
                // 播放TTS
                voiceAssistantManager.speakText("好的，我来帮您启动扫地机器人开始清洁。")

                // 如果TTS有问题，3秒后强制设置为IDLE状态
                kotlinx.coroutines.delay(3000)
                if (voiceAssistantManager.state.value == VoiceAssistantState.SPEAKING) {
                    Log.d(TAG, "TTS可能有问题，强制设置为IDLE")
                    voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                }

                // 暂时注释掉真实的API调用
                /*
                // 获取对话历史
                val conversationHistory = buildConversationHistory()

                // 调用扣子API语音对话
                cozeRepository.createVoiceChatStream(
                    botId = DEFAULT_BOT_ID,
                    userId = DEFAULT_USER_ID,
                    audioFile = audioFile,
                    conversationHistory = conversationHistory
                ).collect { response ->
                    handleCozeResponse(response)
                }
                */

            } catch (e: Exception) {
                Log.e(TAG, "处理语音输入失败", e)
                _errorMessage.value = "语音处理失败: ${e.message}"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            } finally {
                _isProcessing.value = false
                Log.d(TAG, "语音处理完成，isProcessing设置为false")
            }
        }
    }
    
    /**
     * 处理用户输入（文本或语音识别结果）
     */
    fun processUserInput(userInput: String) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                _errorMessage.value = null
                
                // 添加用户消息到历史
                val userMessage = VoiceMessage(
                    content = userInput,
                    isFromUser = true
                )
                voiceAssistantManager.addMessage(userMessage)
                
                // 获取对话历史
                val conversationHistory = buildConversationHistory()
                
                // 调用Coze API
                cozeRepository.createChatStream(
                    botId = DEFAULT_BOT_ID,
                    userId = DEFAULT_USER_ID,
                    message = userInput,
                    conversationHistory = conversationHistory
                ).collect { response ->
                    handleCozeResponse(response)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "处理用户输入失败", e)
                _errorMessage.value = "处理失败: ${e.message}"
                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * 处理Coze响应
     */
    private suspend fun handleCozeResponse(response: CozeResponse) {
        // 更新对话ID
        if (_conversationId.value == null) {
            _conversationId.value = response.data?.conversationId
        }
        if (_chatId.value == null) {
            _chatId.value = response.data?.chatId
        }

        when (response.event) {
            "conversation.chat.created" -> {
                // 对话创建成功
                Log.d(TAG, "对话创建成功")
            }

            "conversation.message.delta" -> {
                // 接收到消息增量 - 暂时忽略，避免乱码累积
                response.data?.content?.let { content ->
                    // 清理增量内容
                    val cleanDelta = content.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")
                    if (cleanDelta.isNotBlank() && cleanDelta.length < 100) {
                        Log.d(TAG, "接收到消息增量: ${cleanDelta.take(50)}")
                    }
                }
            }

            "conversation.message.completed" -> {
                // 消息完成
                response.data?.content?.let { rawContent ->
                    when (response.data?.type) {
                        "answer" -> {
                            // 智能体回复
                            if (response.data?.contentType == "text") {
                                // 清理内容
                                val cleanedContent = cleanResponseContent(rawContent)

                                if (cleanedContent.isNotBlank()) {
                                    val assistantMessage = VoiceMessage(
                                        content = cleanedContent,
                                        isFromUser = false
                                    )
                                    voiceAssistantManager.addMessage(assistantMessage)

                                    // 播放TTS
                                    voiceAssistantManager.speakText(cleanedContent)
                                }
                            }
                        }
                        "function_call" -> {
                            // 函数调用 - 静默处理，不显示给用户
                            Log.d(TAG, "收到函数调用，静默处理")
                            handleFunctionCall(response)
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
                            // 详细信息
                            Log.d(TAG, "收到详细信息")
                            if (rawContent.contains("generate_answer_finish")) {
                                // 生成结束
                                voiceAssistantManager.setState(VoiceAssistantState.IDLE)
                            }
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
                Log.d(TAG, "未知事件类型: ${response.event}")
            }
        }
    }
    
    /**
     * 处理函数调用
     */
    private suspend fun handleFunctionCall(response: CozeResponse) {
        try {
            // 解析函数调用内容
            // 这里需要根据实际的函数调用格式来解析
            // 暂时模拟一个工具执行结果
            
            val toolOutput = ToolOutput(
                toolCallId = response.data?.id ?: "",
                output = "机器人控制命令执行成功"
            )
            
            // 提交工具执行结果
            _conversationId.value?.let { convId ->
                _chatId.value?.let { chatId ->
                    cozeRepository.submitToolOutputsStream(
                        conversationId = convId,
                        chatId = chatId,
                        toolOutputs = listOf(toolOutput)
                    ).collect { toolResponse ->
                        handleCozeResponse(toolResponse)
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "处理函数调用失败", e)
            _errorMessage.value = "函数调用处理失败: ${e.message}"
        }
    }
    
    /**
     * 构建对话历史
     */
    private fun buildConversationHistory(): List<CozeMessage> {
        return messages.value.map { message ->
            CozeMessage(
                role = if (message.isFromUser) "user" else "assistant",
                content = message.content,
                contentType = "text"
            )
        }
    }
    
    /**
     * 发送文本消息
     */
    fun sendTextMessage(text: String) {
        processUserInput(text)
    }
    
    /**
     * 停止语音播放
     */
    fun stopSpeaking() {
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
     * 清理响应内容，移除乱码和不需要的推荐内容
     */
    private fun cleanResponseContent(content: String): String {
        var cleaned = content.trim()

        try {
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
                    Log.d(TAG, "检测到推荐问题，已过滤")
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
