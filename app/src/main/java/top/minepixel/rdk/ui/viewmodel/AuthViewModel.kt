package top.minepixel.rdk.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import top.minepixel.rdk.data.model.LoginResponse
import top.minepixel.rdk.data.model.User
import top.minepixel.rdk.data.model.UserSession
import top.minepixel.rdk.data.repository.AuthRepository
import javax.inject.Inject

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // 用户会话状态
    val userSession: StateFlow<UserSession> = authRepository
        .getUserSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSession()
        )
    
    // 登录状态
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()
    
    // 注册状态
    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()
    
    /**
     * 用户登录
     */
    fun login(username: String, password: String, rememberMe: Boolean = true) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, errorMessage = null)
            
            authRepository.login(username, password, rememberMe)
                .onSuccess { response ->
                    if (response.success) {
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null
                        )
                        Log.d(TAG, "登录成功: ${response.user?.username}")
                    } else {
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = response.message ?: "登录失败"
                        )
                        Log.w(TAG, "登录失败: ${response.message}")
                    }
                }
                .onFailure { e ->
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = "网络错误，请稍后重试"
                    )
                    Log.e(TAG, "登录异常", e)
                }
        }
    }
    
    /**
     * 用户注册
     */
    fun register(username: String, password: String, email: String? = null) {
        viewModelScope.launch {
            _registerState.value = _registerState.value.copy(isLoading = true, errorMessage = null)
            
            authRepository.register(username, password, email)
                .onSuccess { response ->
                    if (response.success) {
                        _registerState.value = _registerState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null
                        )
                        Log.d(TAG, "注册成功: ${response.user?.username}")
                    } else {
                        _registerState.value = _registerState.value.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = response.message ?: "注册失败"
                        )
                        Log.w(TAG, "注册失败: ${response.message}")
                    }
                }
                .onFailure { e ->
                    _registerState.value = _registerState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = "网络错误，请稍后重试"
                    )
                    Log.e(TAG, "注册异常", e)
                }
        }
    }
    
    /**
     * 用户登出
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    Log.d(TAG, "登出成功")
                }
                .onFailure { e ->
                    Log.e(TAG, "登出失败", e)
                }
        }
    }
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    /**
     * 获取当前用户
     */
    fun getCurrentUser(): User? {
        return authRepository.getCurrentUser()
    }
    
    /**
     * 重置登录状态
     */
    fun resetLoginState() {
        _loginState.value = LoginUiState()
    }
    
    /**
     * 重置注册状态
     */
    fun resetRegisterState() {
        _registerState.value = RegisterUiState()
    }
    
    /**
     * 刷新会话
     */
    fun refreshSession() {
        viewModelScope.launch {
            authRepository.refreshSession()
                .onSuccess {
                    Log.d(TAG, "会话刷新成功")
                }
                .onFailure { e ->
                    Log.e(TAG, "会话刷新失败", e)
                }
        }
    }
}

/**
 * 登录UI状态
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 注册UI状态
 */
data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
