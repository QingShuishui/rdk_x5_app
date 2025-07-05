package top.minepixel.rdk.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 用户信息
 */
@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val username: String,
    val email: String? = null,
    val avatar: String? = null,
    val nickname: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 登录请求
 */
@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean = true
)

/**
 * 登录响应
 */
@JsonClass(generateAdapter = true)
data class LoginResponse(
    val success: Boolean,
    val user: User? = null,
    val token: String? = null,
    val message: String? = null
)

/**
 * 用户会话状态
 */
@JsonClass(generateAdapter = true)
data class UserSession(
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val token: String? = null,
    val loginTime: Long = 0L,
    val expiresAt: Long = 0L
)
