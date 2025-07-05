package top.minepixel.rdk.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import top.minepixel.rdk.data.manager.SessionManager
import top.minepixel.rdk.data.model.*
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {
    
    override fun getUserSession(): StateFlow<UserSession> {
        return sessionManager.userSession
    }
    
    override suspend fun login(username: String, password: String, rememberMe: Boolean): Result<LoginResponse> {
        return withContext(ioDispatcher) {
            try {
                // 模拟网络请求延迟
                delay(1500)
                
                // 模拟登录验证逻辑
                when {
                    username.isBlank() || password.isBlank() -> {
                        Result.success(LoginResponse(
                            success = false,
                            message = "用户名和密码不能为空"
                        ))
                    }
                    username == "admin" && password == "123456" -> {
                        // 创建模拟用户
                        val user = User(
                            id = "user_001",
                            username = username,
                            email = "admin@example.com",
                            nickname = "管理员",
                            avatar = null
                        )
                        
                        // 生成模拟token
                        val token = "token_${UUID.randomUUID()}"
                        
                        // 保存会话
                        sessionManager.saveSession(user, token, rememberMe)
                        
                        Result.success(LoginResponse(
                            success = true,
                            user = user,
                            token = token,
                            message = "登录成功"
                        ))
                    }
                    username == "test" && password == "123456" -> {
                        // 创建测试用户
                        val user = User(
                            id = "user_002",
                            username = username,
                            email = "test@example.com",
                            nickname = "测试用户",
                            avatar = null
                        )
                        
                        val token = "token_${UUID.randomUUID()}"
                        sessionManager.saveSession(user, token, rememberMe)
                        
                        Result.success(LoginResponse(
                            success = true,
                            user = user,
                            token = token,
                            message = "登录成功"
                        ))
                    }
                    else -> {
                        Result.success(LoginResponse(
                            success = false,
                            message = "用户名或密码错误"
                        ))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun register(username: String, password: String, email: String?): Result<LoginResponse> {
        return withContext(ioDispatcher) {
            try {
                // 模拟网络请求延迟
                delay(2000)
                
                // 模拟注册逻辑
                when {
                    username.isBlank() || password.isBlank() -> {
                        Result.success(LoginResponse(
                            success = false,
                            message = "用户名和密码不能为空"
                        ))
                    }
                    username.length < 3 -> {
                        Result.success(LoginResponse(
                            success = false,
                            message = "用户名至少3个字符"
                        ))
                    }
                    password.length < 6 -> {
                        Result.success(LoginResponse(
                            success = false,
                            message = "密码至少6个字符"
                        ))
                    }
                    username == "admin" -> {
                        Result.success(LoginResponse(
                            success = false,
                            message = "用户名已存在"
                        ))
                    }
                    else -> {
                        // 创建新用户
                        val user = User(
                            id = "user_${UUID.randomUUID()}",
                            username = username,
                            email = email,
                            nickname = username,
                            avatar = null
                        )
                        
                        val token = "token_${UUID.randomUUID()}"
                        sessionManager.saveSession(user, token, true)
                        
                        Result.success(LoginResponse(
                            success = true,
                            user = user,
                            token = token,
                            message = "注册成功"
                        ))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun logout(): Result<Boolean> {
        return try {
            sessionManager.clearSession()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
    
    override fun getCurrentUser(): User? {
        return sessionManager.getCurrentUser()
    }
    
    override suspend fun refreshSession(): Result<Boolean> {
        return try {
            sessionManager.refreshSession()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
