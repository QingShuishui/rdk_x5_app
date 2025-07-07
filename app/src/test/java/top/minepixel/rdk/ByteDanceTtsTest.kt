package top.minepixel.rdk

import org.junit.Test
import org.junit.Assert.*
import top.minepixel.rdk.data.model.*

/**
 * 字节跳动TTS集成测试
 */
class ByteDanceTtsTest {
    
    @Test
    fun testTtsRequestCreation() {
        val request = ByteDanceTtsRequest(
            app = TtsAppConfig(
                appid = TtsConstants.APP_ID,
                token = TtsConstants.ACCESS_TOKEN,
                cluster = "volcano_tts"
            ),
            user = TtsUserConfig(
                uid = "test_user"
            ),
            audio = TtsAudioConfig(
                voiceType = TtsConstants.VOICE_TYPE_MALE,
                encoding = TtsConstants.ENCODING_MP3,
                speedRatio = 1.0f,
                rate = TtsConstants.RATE_24K,
                bitrate = 160,
                explicitLanguage = "zh",
                loudnessRatio = 1.0f
            ),
            request = TtsRequestConfig(
                reqid = "test_request_id",
                text = "测试文本",
                operation = "query"
            )
        )
        
        assertEquals(TtsConstants.APP_ID, request.app.appid)
        assertEquals(TtsConstants.ACCESS_TOKEN, request.app.token)
        assertEquals("volcano_tts", request.app.cluster)
        assertEquals("test_user", request.user.uid)
        assertEquals(TtsConstants.VOICE_TYPE_MALE, request.audio.voiceType)
        assertEquals(TtsConstants.ENCODING_MP3, request.audio.encoding)
        assertEquals(1.0f, request.audio.speedRatio)
        assertEquals(TtsConstants.RATE_24K, request.audio.rate)
        assertEquals("zh", request.audio.explicitLanguage)
        assertEquals("test_request_id", request.request.reqid)
        assertEquals("测试文本", request.request.text)
        assertEquals("query", request.request.operation)
    }
    
    @Test
    fun testTtsErrorCodeMapping() {
        assertEquals(TtsErrorCode.SUCCESS, TtsErrorCode.fromCode(3000))
        assertEquals(TtsErrorCode.INVALID_REQUEST, TtsErrorCode.fromCode(3001))
        assertEquals(TtsErrorCode.CONCURRENT_LIMIT, TtsErrorCode.fromCode(3003))
        assertEquals(TtsErrorCode.SERVER_BUSY, TtsErrorCode.fromCode(3005))
        assertEquals(TtsErrorCode.VOICE_NOT_FOUND, TtsErrorCode.fromCode(3050))
        assertNull(TtsErrorCode.fromCode(9999))
    }
    
    @Test
    fun testTtsConstants() {
        assertEquals("2031301685", TtsConstants.APP_ID)
        assertEquals("0pNUqHNy9KkQUripWYQSWL7g6PrXQSU7", TtsConstants.ACCESS_TOKEN)
        assertEquals("1LlQe1og6YqchcAd9yHLZav0uS7ah5DD", TtsConstants.SECRET_KEY)
        
        assertEquals("zh_male_M392_conversation_wvae_bigtts", TtsConstants.VOICE_TYPE_MALE)
        assertEquals("zh_female_F001_conversation_wvae_bigtts", TtsConstants.VOICE_TYPE_FEMALE)
        
        assertEquals("mp3", TtsConstants.ENCODING_MP3)
        assertEquals("wav", TtsConstants.ENCODING_WAV)
        assertEquals("pcm", TtsConstants.ENCODING_PCM)
        assertEquals("ogg_opus", TtsConstants.ENCODING_OGG_OPUS)
        
        assertEquals(8000, TtsConstants.RATE_8K)
        assertEquals(16000, TtsConstants.RATE_16K)
        assertEquals(24000, TtsConstants.RATE_24K)
        
        assertEquals(0.8f, TtsConstants.MIN_SPEED_RATIO)
        assertEquals(2.0f, TtsConstants.MAX_SPEED_RATIO)
        
        assertEquals(0.5f, TtsConstants.MIN_LOUDNESS_RATIO)
        assertEquals(2.0f, TtsConstants.MAX_LOUDNESS_RATIO)
        
        assertEquals(1.0f, TtsConstants.MIN_EMOTION_SCALE)
        assertEquals(5.0f, TtsConstants.MAX_EMOTION_SCALE)
    }
    
    @Test
    fun testTtsResponseParsing() {
        val response = ByteDanceTtsResponse(
            reqid = "test_request_id",
            code = 3000,
            message = "Success",
            operation = "query",
            sequence = -1,
            data = "base64_encoded_audio_data",
            addition = TtsAdditionInfo(duration = "1960")
        )
        
        assertEquals("test_request_id", response.reqid)
        assertEquals(3000, response.code)
        assertEquals("Success", response.message)
        assertEquals("query", response.operation)
        assertEquals(-1, response.sequence)
        assertEquals("base64_encoded_audio_data", response.data)
        assertEquals("1960", response.addition?.duration)
    }
    
    @Test
    fun testSpeedRatioValidation() {
        // 测试语速范围验证
        val validSpeed = 1.5f.coerceIn(TtsConstants.MIN_SPEED_RATIO, TtsConstants.MAX_SPEED_RATIO)
        assertEquals(1.5f, validSpeed)
        
        val tooSlowSpeed = 0.5f.coerceIn(TtsConstants.MIN_SPEED_RATIO, TtsConstants.MAX_SPEED_RATIO)
        assertEquals(TtsConstants.MIN_SPEED_RATIO, tooSlowSpeed)
        
        val tooFastSpeed = 3.0f.coerceIn(TtsConstants.MIN_SPEED_RATIO, TtsConstants.MAX_SPEED_RATIO)
        assertEquals(TtsConstants.MAX_SPEED_RATIO, tooFastSpeed)
    }
    
    @Test
    fun testLoudnessRatioValidation() {
        // 测试音量范围验证
        val validLoudness = 1.2f.coerceIn(TtsConstants.MIN_LOUDNESS_RATIO, TtsConstants.MAX_LOUDNESS_RATIO)
        assertEquals(1.2f, validLoudness)
        
        val tooQuietLoudness = 0.3f.coerceIn(TtsConstants.MIN_LOUDNESS_RATIO, TtsConstants.MAX_LOUDNESS_RATIO)
        assertEquals(TtsConstants.MIN_LOUDNESS_RATIO, tooQuietLoudness)
        
        val tooLoudLoudness = 3.0f.coerceIn(TtsConstants.MIN_LOUDNESS_RATIO, TtsConstants.MAX_LOUDNESS_RATIO)
        assertEquals(TtsConstants.MAX_LOUDNESS_RATIO, tooLoudLoudness)
    }
}
