package top.minepixel.rdk.data.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import top.minepixel.rdk.data.model.CozeChatRequest
import top.minepixel.rdk.data.model.FileUploadResponse
import top.minepixel.rdk.data.model.SubmitToolOutputsRequest

/**
 * 扣子API服务接口
 */
interface CozeApiService {
    
    companion object {
        const val BASE_URL = "https://api.coze.cn/"
        const val API_TOKEN = "pat_GzjXb1EE2xAXOCZdYthtedvbRBpWdNZadw7fMyXNWTOxojAAu3y38ThT0jHlkMWu"
    }
    
    /**
     * 发起对话
     */
    @POST("v3/chat")
    @Headers("Content-Type: application/json")
    suspend fun createChat(
        @Header("Authorization") authorization: String = "Bearer $API_TOKEN",
        @Body request: CozeChatRequest
    ): Response<ResponseBody>
    
    /**
     * 发起对话 - 流式响应
     */
    @POST("v3/chat")
    @Headers("Content-Type: application/json")
    @Streaming
    suspend fun createChatStream(
        @Header("Authorization") authorization: String = "Bearer $API_TOKEN",
        @Body request: CozeChatRequest
    ): Response<ResponseBody>
    
    /**
     * 提交工具执行结果
     */
    @POST("v3/chat/submit_tool_outputs")
    @Headers("Content-Type: application/json")
    suspend fun submitToolOutputs(
        @Header("Authorization") authorization: String = "Bearer $API_TOKEN",
        @Query("conversation_id") conversationId: String,
        @Query("chat_id") chatId: String,
        @Body request: SubmitToolOutputsRequest
    ): Response<ResponseBody>
    
    /**
     * 提交工具执行结果 - 流式响应
     */
    @POST("v3/chat/submit_tool_outputs")
    @Headers("Content-Type: application/json")
    @Streaming
    suspend fun submitToolOutputsStream(
        @Header("Authorization") authorization: String = "Bearer $API_TOKEN",
        @Query("conversation_id") conversationId: String,
        @Query("chat_id") chatId: String,
        @Body request: SubmitToolOutputsRequest
    ): Response<ResponseBody>
    
    /**
     * 查看对话详情
     */
    @GET("v3/chat/retrieve")
    suspend fun getChatDetails(
        @Header("Authorization") authorization: String = "Bearer $API_TOKEN",
        @Query("conversation_id") conversationId: String,
        @Query("chat_id") chatId: String
    ): Response<ResponseBody>
    
    /**
     * 查看对话消息详情
     */
    @GET("v3/chat/message/list")
    suspend fun getChatMessages(
        @Header("Authorization") authorization: String = "Bearer $API_TOKEN",
        @Query("conversation_id") conversationId: String,
        @Query("chat_id") chatId: String
    ): Response<ResponseBody>

    /**
     * 上传文件
     */
    @Multipart
    @POST("v1/files/upload")
    suspend fun uploadFile(
        @Header("Authorization") authorization: String = "Bearer $API_TOKEN",
        @Part file: MultipartBody.Part,
        @Part("purpose") purpose: okhttp3.RequestBody
    ): Response<FileUploadResponse>
}
