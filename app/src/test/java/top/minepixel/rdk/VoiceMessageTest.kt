package top.minepixel.rdk

import org.junit.Test
import org.junit.Assert.*
import top.minepixel.rdk.data.model.VoiceMessage
import top.minepixel.rdk.data.model.MessageType

/**
 * 语音消息测试
 */
class VoiceMessageTest {
    
    @Test
    fun testVoiceMessageCreation() {
        val voiceMessage = VoiceMessage(
            id = "voice_123",
            content = "语音消息：开始清洁",
            isFromUser = true,
            type = MessageType.AUDIO,
            timestamp = System.currentTimeMillis()
        )
        
        assertEquals("voice_123", voiceMessage.id)
        assertEquals("语音消息：开始清洁", voiceMessage.content)
        assertTrue(voiceMessage.isFromUser)
        assertEquals(MessageType.AUDIO, voiceMessage.type)
    }
    
    @Test
    fun testTextMessageCreation() {
        val textMessage = VoiceMessage(
            id = "text_456",
            content = "好的，我来帮您启动扫地机器人开始清洁。",
            isFromUser = false,
            type = MessageType.TEXT,
            timestamp = System.currentTimeMillis()
        )
        
        assertEquals("text_456", textMessage.id)
        assertEquals("好的，我来帮您启动扫地机器人开始清洁。", textMessage.content)
        assertFalse(textMessage.isFromUser)
        assertEquals(MessageType.TEXT, textMessage.type)
    }
    
    @Test
    fun testDefaultValues() {
        val message = VoiceMessage(
            content = "测试消息",
            isFromUser = true
        )
        
        assertNotNull(message.id)
        assertEquals("测试消息", message.content)
        assertTrue(message.isFromUser)
        assertEquals(MessageType.TEXT, message.type)
        assertTrue(message.timestamp > 0)
        assertNull(message.audioPath)
    }
}
