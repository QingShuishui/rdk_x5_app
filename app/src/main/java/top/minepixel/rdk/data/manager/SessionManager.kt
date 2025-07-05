package top.minepixel.rdk.data.manager

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.minepixel.rdk.data.model.User
import top.minepixel.rdk.data.model.UserSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val userSessionAdapter = moshi.adapter(UserSession::class.java)
    
    private val _userSession = MutableStateFlow(loadSession())
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()
    
    companion object {
        private const val PREFS_NAME = "user_session"
        private const val KEY_SESSION = "session_data"
        private const val KEY_AUTO_LOGIN = "auto_login"
        private const val SESSION_DURATION = 30L * 24 * 60 * 60 * 1000 // 30天
    }
    
    /**
     * 保存用户会话
     */
    fun saveSession(user: User, token: String, rememberMe: Boolean = true) {
        val currentTime = System.currentTimeMillis()
        val session = UserSession(
            isLoggedIn = true,
            user = user,
            token = token,
            loginTime = currentTime,
            expiresAt = currentTime + SESSION_DURATION
        )
        
        // 保存到SharedPreferences
        prefs.edit().apply {
            putString(KEY_SESSION, userSessionAdapter.toJson(session))
            putBoolean(KEY_AUTO_LOGIN, rememberMe)
            apply()
        }
        
        // 更新状态流
        _userSession.value = session
    }
    
    /**
     * 加载用户会话
     */
    private fun loadSession(): UserSession {
        val sessionJson = prefs.getString(KEY_SESSION, null)
        val autoLogin = prefs.getBoolean(KEY_AUTO_LOGIN, false)
        
        if (sessionJson != null && autoLogin) {
            try {
                val session = userSessionAdapter.fromJson(sessionJson)
                if (session != null && isSessionValid(session)) {
                    return session
                }
            } catch (e: Exception) {
                // JSON解析失败，清除无效数据
                clearSession()
            }
        }
        
        return UserSession()
    }
    
    /**
     * 检查会话是否有效
     */
    private fun isSessionValid(session: UserSession): Boolean {
        return session.isLoggedIn && 
               session.expiresAt > System.currentTimeMillis() &&
               session.user != null &&
               session.token != null
    }
    
    /**
     * 清除用户会话
     */
    fun clearSession() {
        prefs.edit().clear().apply()
        _userSession.value = UserSession()
    }
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        val session = _userSession.value
        return session.isLoggedIn && isSessionValid(session)
    }
    
    /**
     * 获取当前用户
     */
    fun getCurrentUser(): User? {
        return if (isLoggedIn()) _userSession.value.user else null
    }
    
    /**
     * 获取访问令牌
     */
    fun getAccessToken(): String? {
        return if (isLoggedIn()) _userSession.value.token else null
    }
    
    /**
     * 刷新会话过期时间
     */
    fun refreshSession() {
        val currentSession = _userSession.value
        if (currentSession.isLoggedIn && currentSession.user != null && currentSession.token != null) {
            saveSession(currentSession.user, currentSession.token, true)
        }
    }
}
