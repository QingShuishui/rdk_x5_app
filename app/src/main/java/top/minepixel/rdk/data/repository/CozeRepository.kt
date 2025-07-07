package top.minepixel.rdk.data.repository

import kotlinx.coroutines.flow.Flow
import top.minepixel.rdk.data.model.*
import java.io.File

/**
 * 扣子API数据仓库接口
 */
interface CozeRepository {
    
    /**
     * 发起对话
     */
    suspend fun createChat(
        botId: String,
        userId: String,
        message: String,
        conversationHistory: List<CozeMessage>? = null
    ): Result<String>
    
    /**
     * 发起流式对话
     */
    suspend fun createChatStream(
        botId: String,
        userId: String,
        message: String,
        conversationHistory: List<CozeMessage>? = null
    ): Flow<CozeResponse>
    
    /**
     * 提交工具执行结果
     */
    suspend fun submitToolOutputs(
        conversationId: String,
        chatId: String,
        toolOutputs: List<ToolOutput>
    ): Result<String>
    
    /**
     * 提交工具执行结果 - 流式
     */
    suspend fun submitToolOutputsStream(
        conversationId: String,
        chatId: String,
        toolOutputs: List<ToolOutput>
    ): Flow<CozeResponse>
    
    /**
     * 获取对话详情
     */
    suspend fun getChatDetails(
        conversationId: String,
        chatId: String
    ): Result<String>
    
    /**
     * 获取对话消息
     */
    suspend fun getChatMessages(
        conversationId: String,
        chatId: String
    ): Result<List<CozeResponse>>

    /**
     * 上传音频文件
     */
    suspend fun uploadAudioFile(audioFile: File): Result<FileUploadResponse>

    /**
     * 发起语音对话
     */
    suspend fun createVoiceChatStream(
        botId: String,
        userId: String,
        audioFile: File,
        conversationHistory: List<CozeMessage>? = null
    ): Flow<CozeResponse>
}
