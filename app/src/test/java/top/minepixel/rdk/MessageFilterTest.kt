package top.minepixel.rdk

import org.junit.Test
import org.junit.Assert.*

/**
 * 消息过滤测试
 */
class MessageFilterTest {
    
    /**
     * 模拟cleanResponseContent方法的核心逻辑
     */
    private fun testCleanResponseContent(content: String): String {
        var cleaned = content.trim()
        
        // 1. 检查是否是JSON格式的系统消息或函数调用信息，直接过滤掉
        if (cleaned.startsWith("{") || cleaned.contains("{\"name\":")) {
            return ""
        }
        
        // 2. 检查是否包含函数调用相关的关键词
        val functionCallKeywords = listOf(
            "plugin_id", "api_id", "function_call", "tool_call",
            "arguments", "plugin_name", "\"name\":", "\"arguments\":",
            "ts-1-w", "plugin_type", "api_name"
        )
        
        if (functionCallKeywords.any { cleaned.contains(it, ignoreCase = true) }) {
            return ""
        }
        
        // 3. 检查是否包含特定的函数调用模式
        if (cleaned.matches(Regex(".*\"name\"\\s*:\\s*\".*\".*")) ||
            cleaned.matches(Regex(".*\"arguments\"\\s*:\\s*\\{.*")) ||
            cleaned.contains("7523587886646755380") ||
            cleaned.contains("7523587966820745266")) {
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
                return ""
            }
        }
        
        // 5. 移除常见的乱码字符
        cleaned = cleaned.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")
        
        // 6. 移除特殊控制字符
        cleaned = cleaned.replace(Regex("[\u0000-\u001F\u007F-\u009F]"), "")
        
        // 7. 最终检查：如果清理后内容太短或只包含标点，可能是无效内容
        if (cleaned.length < 5 || cleaned.matches(Regex("[\\s\\p{Punct}]*"))) {
            return ""
        }
        
        return cleaned.trim()
    }
    
    @Test
    fun testNormalQuestionShouldNotBeFiltered() {
        // 测试正常的对话回复不应该被过滤
        val message = "不太明白您的意思呢。咱们聊点清洁相关的话题吧，您是有清洁需求或者清洁问题要问我吗？"
        val result = testCleanResponseContent(message)
        
        assertEquals("正常的对话回复不应该被过滤", message, result)
    }
    
    @Test
    fun testRecommendationQuestionShouldBeFiltered() {
        // 测试推荐问题应该被过滤
        val messages = listOf(
            "您想了解清洁技巧吗？",
            "需要我为您推荐工具吗？",
            "我可以为您提供建议吗？",
            "如何清洁呢？",
            "什么方法好呢？"
        )
        
        messages.forEach { message ->
            val result = testCleanResponseContent(message)
            assertEquals("推荐问题应该被过滤: $message", "", result)
        }
    }
    
    @Test
    fun testLongQuestionShouldNotBeFiltered() {
        // 测试长的问句不应该被过滤（即使包含关键词）
        val message = "关于清洁机器人的使用，我想详细了解一下它的工作原理和清洁效果，您能为我详细介绍一下吗？"
        val result = testCleanResponseContent(message)
        
        assertEquals("长的问句不应该被过滤", message, result)
    }
    
    @Test
    fun testFunctionCallShouldBeFiltered() {
        // 测试函数调用应该被过滤
        val messages = listOf(
            "{\"name\": \"start_cleaning\", \"arguments\": {}}",
            "plugin_id: 12345",
            "function_call detected",
            "tool_call in progress"
        )
        
        messages.forEach { message ->
            val result = testCleanResponseContent(message)
            assertEquals("函数调用应该被过滤: $message", "", result)
        }
    }
    
    @Test
    fun testNormalResponseShouldNotBeFiltered() {
        // 测试正常回复不应该被过滤
        val messages = listOf(
            "好的，我来帮您启动扫地机器人开始清洁。",
            "机器人已经开始工作了，请稍等。",
            "清洁任务已完成，机器人正在返回充电座。",
            "抱歉，我没有理解您的意思，请重新说一遍。"
        )
        
        messages.forEach { message ->
            val result = testCleanResponseContent(message)
            assertEquals("正常回复不应该被过滤: $message", message, result)
        }
    }
    
    @Test
    fun testShortContentShouldBeFiltered() {
        // 测试过短的内容应该被过滤
        val messages = listOf(
            "？",
            "。。。",
            "   ",
            "！！"
        )
        
        messages.forEach { message ->
            val result = testCleanResponseContent(message)
            assertEquals("过短的内容应该被过滤: $message", "", result)
        }
    }
}
