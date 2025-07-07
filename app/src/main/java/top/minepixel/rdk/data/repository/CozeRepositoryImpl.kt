package top.minepixel.rdk.data.repository

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import top.minepixel.rdk.data.api.CozeApiService
import top.minepixel.rdk.data.model.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CozeRepositoryImpl @Inject constructor(
    private val cozeApiService: CozeApiService,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) : CozeRepository {
    
    companion object {
        private const val TAG = "CozeRepository"
        private const val DEFAULT_BOT_ID = "7523579176386576419" // 您的Bot ID
    }
    
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val cozeResponseAdapter = moshi.adapter(CozeResponse::class.java)
    
    override suspend fun createChat(
        botId: String,
        userId: String,
        message: String,
        conversationHistory: List<CozeMessage>?
    ): Result<String> {
        return withContext(ioDispatcher) {
            try {
                val messages = mutableListOf<CozeMessage>()
                
                // 添加历史消息
                conversationHistory?.let { messages.addAll(it) }
                
                // 添加当前用户消息
                messages.add(CozeMessage(
                    role = "user",
                    content = message,
                    contentType = "text"
                ))
                
                val request = CozeChatRequest(
                    botId = botId,
                    userId = userId,
                    stream = false,
                    autoSaveHistory = true,
                    additionalMessages = messages
                )
                
                val response = cozeApiService.createChat(request = request)
                
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string() ?: ""
                    Log.d(TAG, "对话创建成功: $responseBody")
                    Result.success(responseBody)
                } else {
                    val error = "对话创建失败: ${response.code()} ${response.message()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "对话创建异常", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun createChatStream(
        botId: String,
        userId: String,
        message: String,
        conversationHistory: List<CozeMessage>?
    ): Flow<CozeResponse> = flow {
        try {
            val messages = mutableListOf<CozeMessage>()
            
            // 添加历史消息
            conversationHistory?.let { messages.addAll(it) }
            
            // 添加当前用户消息
            messages.add(CozeMessage(
                role = "user",
                content = message,
                contentType = "text"
            ))
            
            val request = CozeChatRequest(
                botId = botId,
                userId = userId,
                stream = true,
                autoSaveHistory = true,
                additionalMessages = messages
            )
            
            val response = cozeApiService.createChatStream(request = request)
            
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    parseStreamResponse(responseBody) { cozeResponse ->
                        emit(cozeResponse)
                    }
                }
            } else {
                Log.e(TAG, "流式对话失败: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "流式对话异常", e)
        }
    }
    
    override suspend fun submitToolOutputs(
        conversationId: String,
        chatId: String,
        toolOutputs: List<ToolOutput>
    ): Result<String> {
        return withContext(ioDispatcher) {
            try {
                val request = SubmitToolOutputsRequest(
                    toolOutputs = toolOutputs,
                    stream = false
                )
                
                val response = cozeApiService.submitToolOutputs(
                    conversationId = conversationId,
                    chatId = chatId,
                    request = request
                )
                
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string() ?: ""
                    Log.d(TAG, "工具结果提交成功: $responseBody")
                    Result.success(responseBody)
                } else {
                    val error = "工具结果提交失败: ${response.code()} ${response.message()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "工具结果提交异常", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun submitToolOutputsStream(
        conversationId: String,
        chatId: String,
        toolOutputs: List<ToolOutput>
    ): Flow<CozeResponse> = flow {
        try {
            val request = SubmitToolOutputsRequest(
                toolOutputs = toolOutputs,
                stream = true
            )
            
            val response = cozeApiService.submitToolOutputsStream(
                conversationId = conversationId,
                chatId = chatId,
                request = request
            )
            
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    parseStreamResponse(responseBody) { cozeResponse ->
                        emit(cozeResponse)
                    }
                }
            } else {
                Log.e(TAG, "流式工具结果提交失败: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "流式工具结果提交异常", e)
        }
    }
    
    override suspend fun getChatDetails(
        conversationId: String,
        chatId: String
    ): Result<String> {
        return withContext(ioDispatcher) {
            try {
                val response = cozeApiService.getChatDetails(
                    conversationId = conversationId,
                    chatId = chatId
                )
                
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string() ?: ""
                    Log.d(TAG, "获取对话详情成功: $responseBody")
                    Result.success(responseBody)
                } else {
                    val error = "获取对话详情失败: ${response.code()} ${response.message()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取对话详情异常", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getChatMessages(
        conversationId: String,
        chatId: String
    ): Result<List<CozeResponse>> {
        return withContext(ioDispatcher) {
            try {
                val response = cozeApiService.getChatMessages(
                    conversationId = conversationId,
                    chatId = chatId
                )
                
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string() ?: ""
                    Log.d(TAG, "获取对话消息成功: $responseBody")
                    // 这里需要解析JSON数组，暂时返回空列表
                    Result.success(emptyList<CozeResponse>())
                } else {
                    val error = "获取对话消息失败: ${response.code()} ${response.message()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取对话消息异常", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun uploadAudioFile(audioFile: File): Result<FileUploadResponse> {
        return withContext(ioDispatcher) {
            try {
                val requestFile = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
                val purposeBody = "assistants".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = cozeApiService.uploadFile(
                    file = filePart,
                    purpose = purposeBody
                )

                if (response.isSuccessful) {
                    response.body()?.let { uploadResponse ->
                        Log.d(TAG, "文件上传成功: ${uploadResponse.id}")
                        Result.success(uploadResponse)
                    } ?: Result.failure(Exception("上传响应为空"))
                } else {
                    val error = "文件上传失败: ${response.code()} ${response.message()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "文件上传异常", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun createVoiceChatStream(
        botId: String,
        userId: String,
        audioFile: File,
        conversationHistory: List<CozeMessage>?
    ): Flow<CozeResponse> = flow {
        try {
            // 1. 先上传音频文件
            val uploadResult = uploadAudioFile(audioFile)

            uploadResult.onSuccess { uploadResponse ->
                // 2. 创建包含音频的消息
                val messages = mutableListOf<CozeMessage>()

                // 添加历史消息
                conversationHistory?.let { messages.addAll(it) }

                // 添加音频消息
                messages.add(CozeMessage(
                    role = "user",
                    content = "", // 音频消息content可以为空
                    contentType = "audio",
                    fileId = uploadResponse.id
                ))

                // 3. 发起对话
                val request = CozeChatRequest(
                    botId = botId,
                    userId = userId,
                    stream = true,
                    autoSaveHistory = true,
                    additionalMessages = messages
                )

                val response = cozeApiService.createChatStream(request = request)

                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        parseStreamResponse(responseBody) { cozeResponse ->
                            emit(cozeResponse)
                        }
                    }
                } else {
                    Log.e(TAG, "语音对话失败: ${response.code()} ${response.message()}")
                }
            }.onFailure { e ->
                Log.e(TAG, "音频上传失败", e)
            }

        } catch (e: Exception) {
            Log.e(TAG, "语音对话异常", e)
        }
    }

    /**
     * 解析流式响应
     */
    private suspend fun parseStreamResponse(
        responseBody: ResponseBody,
        onResponse: suspend (CozeResponse) -> Unit
    ) {
        val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))

        try {
            var currentEvent: String? = null
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                line?.let { currentLine ->
                    val trimmedLine = currentLine.trim()

                    when {
                        trimmedLine.startsWith("event:") -> {
                            // 解析事件类型
                            currentEvent = trimmedLine.substring(6).trim()
                        }

                        trimmedLine.startsWith("data:") -> {
                            // 解析数据
                            val jsonData = trimmedLine.substring(5).trim()
                            if (jsonData.isNotEmpty() && jsonData != "[DONE]") {
                                try {
                                    // 解析数据部分
                                    val dataAdapter = moshi.adapter(CozeResponseData::class.java)
                                    val responseData = dataAdapter.fromJson(jsonData)

                                    // 构建完整的响应对象
                                    val cozeResponse = CozeResponse(
                                        event = currentEvent,
                                        data = responseData
                                    )

                                    onResponse(cozeResponse)
                                    Log.d(TAG, "解析流式响应成功: event=$currentEvent, data=$jsonData")
                                } catch (e: Exception) {
                                    Log.w(TAG, "解析流式响应失败: event=$currentEvent, data=$jsonData", e)
                                }
                            }
                        }

                        trimmedLine.isEmpty() -> {
                            // 空行，重置当前事件
                            currentEvent = null
                        }
                    }
                }
            }
        } finally {
            reader.close()
        }
    }
}
