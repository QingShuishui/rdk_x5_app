package top.minepixel.rdk.data.api

import retrofit2.Response
import retrofit2.http.*
import top.minepixel.rdk.data.model.BaiduSpeechRequest
import top.minepixel.rdk.data.model.BaiduSpeechResponse
import top.minepixel.rdk.data.model.BaiduTokenResponse

/**
 * 百度Token API服务
 */
interface BaiduTokenApiService {

    companion object {
        const val API_KEY = "mr8qfvSRKDmLTNnJIvYcOh9M"
        const val SECRET_KEY = "a4Fy42lfwsINcctuBPkZKjFXv22VMHnd"
    }

    /**
     * 获取Access Token
     */
    @POST("oauth/2.0/token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String = API_KEY,
        @Field("client_secret") clientSecret: String = SECRET_KEY
    ): Response<BaiduTokenResponse>
}

/**
 * 百度语音识别API服务
 */
interface BaiduSpeechApiService {

    companion object {
        const val SPEECH_BASE_URL = "https://vop.baidu.com/"
        const val TOKEN_BASE_URL = "https://aip.baidubce.com/"
    }

    /**
     * 语音识别 - JSON方式
     */
    @POST("server_api")
    @Headers("Content-Type: application/json")
    suspend fun recognizeSpeech(
        @Body request: BaiduSpeechRequest
    ): Response<BaiduSpeechResponse>

    /**
     * 语音识别 - RAW方式
     */
    @POST("server_api")
    suspend fun recognizeSpeechRaw(
        @Header("Content-Type") contentType: String, // audio/pcm;rate=16000
        @Query("cuid") cuid: String,
        @Query("token") token: String,
        @Query("dev_pid") devPid: Int = 1537,
        @Body audioData: okhttp3.RequestBody
    ): Response<BaiduSpeechResponse>
}
