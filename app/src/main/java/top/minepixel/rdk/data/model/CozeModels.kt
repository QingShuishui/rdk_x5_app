package top.minepixel.rdk.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 扣子对话请求
 */
@JsonClass(generateAdapter = true)
data class CozeChatRequest(
    @Json(name = "bot_id")
    val botId: String,
    @Json(name = "user_id")
    val userId: String,
    val stream: Boolean = true,
    @Json(name = "auto_save_history")
    val autoSaveHistory: Boolean = true,
    @Json(name = "additional_messages")
    val additionalMessages: List<CozeMessage>? = null
)

/**
 * 扣子消息
 */
@JsonClass(generateAdapter = true)
data class CozeMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    @Json(name = "content_type")
    val contentType: String = "text", // "text", "audio", "image"
    @Json(name = "file_id")
    val fileId: String? = null // 音频文件ID
)

/**
 * 扣子流式响应
 */
@JsonClass(generateAdapter = true)
data class CozeResponse(
    val event: String? = null,
    val data: CozeResponseData? = null
)

/**
 * 扣子响应数据
 */
@JsonClass(generateAdapter = true)
data class CozeResponseData(
    val id: String? = null,
    @Json(name = "conversation_id")
    val conversationId: String? = null,
    @Json(name = "bot_id")
    val botId: String? = null,
    @Json(name = "chat_id")
    val chatId: String? = null,
    val role: String? = null,
    val type: String? = null,
    val content: String? = null,
    @Json(name = "content_type")
    val contentType: String? = null,
    @Json(name = "created_at")
    val createdAt: Long? = null,
    @Json(name = "updated_at")
    val updatedAt: Long? = null,
    val status: String? = null,
    @Json(name = "last_error")
    val lastError: CozeError? = null,
    val usage: CozeUsage? = null,
    @Json(name = "section_id")
    val sectionId: String? = null,
    val delta: CozeDelta? = null
)

/**
 * 扣子消息增量
 */
@JsonClass(generateAdapter = true)
data class CozeDelta(
    val content: String? = null
)

/**
 * 扣子错误信息
 */
@JsonClass(generateAdapter = true)
data class CozeError(
    val code: Int = 0,
    val msg: String = ""
)

/**
 * 扣子使用统计
 */
@JsonClass(generateAdapter = true)
data class CozeUsage(
    @Json(name = "token_count")
    val tokenCount: Int = 0,
    @Json(name = "output_count")
    val outputCount: Int = 0,
    @Json(name = "input_count")
    val inputCount: Int = 0
)

/**
 * 工具调用
 */
@JsonClass(generateAdapter = true)
data class ToolCall(
    val id: String,
    val type: String,
    val function: ToolFunction
)

/**
 * 工具函数
 */
@JsonClass(generateAdapter = true)
data class ToolFunction(
    val name: String,
    val arguments: String
)

/**
 * 工具输出
 */
@JsonClass(generateAdapter = true)
data class ToolOutput(
    @Json(name = "tool_call_id")
    val toolCallId: String,
    val output: String
)

/**
 * 提交工具结果请求
 */
@JsonClass(generateAdapter = true)
data class SubmitToolOutputsRequest(
    @Json(name = "tool_outputs")
    val toolOutputs: List<ToolOutput>,
    val stream: Boolean = true
)

/**
 * 语音助手状态
 */
enum class VoiceAssistantState {
    IDLE,           // 空闲
    LISTENING,      // 监听中
    PROCESSING,     // 处理中
    SPEAKING,       // 说话中
    ERROR           // 错误状态
}

/**
 * 语音助手消息
 */
@JsonClass(generateAdapter = true)
data class VoiceMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val audioPath: String? = null
)

/**
 * 文件上传响应
 */
@JsonClass(generateAdapter = true)
data class FileUploadResponse(
    val id: String,
    val bytes: Long,
    @Json(name = "created_at")
    val createdAt: Long,
    val filename: String,
    val purpose: String
)

/**
 * 消息类型
 */
enum class MessageType {
    TEXT,           // 文本消息
    AUDIO,          // 语音消息
    CARD,           // 卡片消息
    FUNCTION_CALL,  // 函数调用
    TOOL_RESPONSE   // 工具响应
}
