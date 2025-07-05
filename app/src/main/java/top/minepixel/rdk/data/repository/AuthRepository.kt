package top.minepixel.rdk.data.repository

import kotlinx.coroutines.flow.StateFlow
import top.minepixel.rdk.data.model.*

/**
 * 认证数据仓库接口
 */
interface AuthRepository {
    /**
     * 获取用户会话状态
     */
    fun getUserSession(): StateFlow<UserSession>
    
    /**
     * 用户登录
     */
    suspend fun login(username: String, password: String, rememberMe: Boolean = true): Result<LoginResponse>
    
    /**
     * 用户注册
     */
    suspend fun register(username: String, password: String, email: String? = null): Result<LoginResponse>
    
    /**
     * 用户登出
     */
    suspend fun logout(): Result<Boolean>
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean
    
    /**
     * 获取当前用户
     */
    fun getCurrentUser(): User?
    
    /**
     * 刷新会话
     */
    suspend fun refreshSession(): Result<Boolean>
}
