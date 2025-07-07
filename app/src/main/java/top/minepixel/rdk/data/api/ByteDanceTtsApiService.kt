package top.minepixel.rdk.data.api

import retrofit2.Response
import retrofit2.http.*
import top.minepixel.rdk.data.model.ByteDanceTtsRequest
import top.minepixel.rdk.data.model.ByteDanceTtsResponse
import top.minepixel.rdk.data.model.TtsConstants

/**
 * 字节跳动TTS API服务接口
 */
interface ByteDanceTtsApiService {
    
    companion object {
        const val BASE_URL = "https://openspeech.bytedance.com/"
        const val TTS_ENDPOINT = "api/v1/tts"
        
        // 认证格式：Bearer;token（注意使用分号分隔）
        private const val AUTHORIZATION_FORMAT = "Bearer;${TtsConstants.ACCESS_TOKEN}"
    }
    
    /**
     * 文本转语音 - HTTP方式
     * 
     * @param request TTS请求参数
     * @return TTS响应，包含base64编码的音频数据
     */
    @POST(TTS_ENDPOINT)
    @Headers("Content-Type: application/json")
    suspend fun synthesizeSpeech(
        @Header("Authorization") authorization: String = AUTHORIZATION_FORMAT,
        @Body request: ByteDanceTtsRequest
    ): Response<ByteDanceTtsResponse>
}
