package top.minepixel.rdk.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import top.minepixel.rdk.data.manager.VoiceAssistantManager
import top.minepixel.rdk.data.model.*
import java.io.File
import javax.inject.Inject

private const val TAG = "VoiceAssistantTestVM"

@HiltViewModel
class VoiceAssistantTestViewModel @Inject constructor(
    private val voiceAssistantManager: VoiceAssistantManager
) : ViewModel() {
    
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
    
    /**
     * 开始录音
     */
    fun startRecording() {
        viewModelScope.launch {
            Log.d(TAG, "开始录音")
            voiceAssistantManager.setState(VoiceAssistantState.LISTENING)
        }
    }
    
    /**
     * 停止录音并处理
     */
    fun stopRecordingAndProcess() {
        viewModelScope.launch {
            Log.d(TAG, "停止录音并处理")
            
            // 直接模拟整个流程
            voiceAssistantManager.setState(VoiceAssistantState.PROCESSING)
            _isProcessing.value = true
            
            // 添加用户语音消息
            val userMessage = VoiceMessage(
                content = "[语音消息] 开始清洁",
                isFromUser = true,
                type = MessageType.AUDIO
            )
            voiceAssistantManager.addMessage(userMessage)
            
            // 模拟处理延迟
            kotlinx.coroutines.delay(2000)
            
            // 添加AI回复
            val assistantMessage = VoiceMessage(
                content = "好的，我来帮您启动扫地机器人开始清洁。",
                isFromUser = false
            )
            voiceAssistantManager.addMessage(assistantMessage)
            
            // 完成处理
            _isProcessing.value = false
            voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            
            Log.d(TAG, "语音处理完成")
        }
    }
    
    /**
     * 发送文本消息
     */
    fun sendTextMessage(text: String) {
        viewModelScope.launch {
            Log.d(TAG, "发送文本消息: $text")
            
            _isProcessing.value = true
            voiceAssistantManager.setState(VoiceAssistantState.PROCESSING)
            
            // 添加用户消息
            val userMessage = VoiceMessage(
                content = text,
                isFromUser = true
            )
            voiceAssistantManager.addMessage(userMessage)
            
            // 模拟处理延迟
            kotlinx.coroutines.delay(1500)
            
            // 生成回复
            val reply = when {
                text.contains("清洁") || text.contains("扫地") -> "好的，我来帮您启动扫地机器人开始清洁。"
                text.contains("停止") -> "好的，我来帮您停止机器人工作。"
                text.contains("回到") || text.contains("充电") -> "好的，我来让机器人返回充电座。"
                text.contains("找") || text.contains("定位") -> "好的，我来让机器人发出声音帮助您定位。"
                else -> "我收到了您的指令：$text，正在为您处理。"
            }
            
            // 添加AI回复
            val assistantMessage = VoiceMessage(
                content = reply,
                isFromUser = false
            )
            voiceAssistantManager.addMessage(assistantMessage)
            
            // 完成处理
            _isProcessing.value = false
            voiceAssistantManager.setState(VoiceAssistantState.IDLE)
            
            Log.d(TAG, "文本处理完成")
        }
    }
    
    /**
     * 清除对话历史
     */
    fun clearConversation() {
        voiceAssistantManager.clearMessages()
        _errorMessage.value = null
    }
    
    /**
     * 停止语音播放
     */
    fun stopSpeaking() {
        Log.d(TAG, "停止语音播放")
        voiceAssistantManager.stopSpeaking()
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
