package top.minepixel.rdk.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import top.minepixel.rdk.data.manager.CameraManager
import top.minepixel.rdk.data.manager.CameraInfo
import javax.inject.Inject

/**
 * 摄像头ViewModel
 * 管理摄像头相关的UI状态和业务逻辑
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraManager: CameraManager
) : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"
    }

    // 摄像头状态
    val isCameraActive: StateFlow<Boolean> = cameraManager.isCameraActive
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // 摄像头预览数据
    val cameraPreviewData: StateFlow<ByteArray?> = cameraManager.cameraPreviewData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 错误消息
    val errorMessage: StateFlow<String?> = cameraManager.errorMessage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 摄像头信息
    private val _cameraInfo = MutableStateFlow(
        CameraInfo(
            isActive = false,
            resolution = "未知",
            frameRate = "0fps",
            connectionStatus = "未连接"
        )
    )
    val cameraInfo: StateFlow<CameraInfo> = _cameraInfo.asStateFlow()

    init {
        // 监听摄像头状态变化，更新摄像头信息
        viewModelScope.launch {
            isCameraActive.collect { isActive ->
                _cameraInfo.value = cameraManager.getCameraInfo()
            }
        }
    }

    /**
     * 启动摄像头
     */
    fun startCamera() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "开始启动摄像头")
                
                val result = cameraManager.startCamera()
                result.onSuccess {
                    Log.d(TAG, "摄像头启动成功")
                    _cameraInfo.value = cameraManager.getCameraInfo()
                }.onFailure { e ->
                    Log.e(TAG, "摄像头启动失败", e)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "启动摄像头时发生异常", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 停止摄像头
     */
    fun stopCamera() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "开始停止摄像头")
                
                val result = cameraManager.stopCamera()
                result.onSuccess {
                    Log.d(TAG, "摄像头停止成功")
                    _cameraInfo.value = cameraManager.getCameraInfo()
                }.onFailure { e ->
                    Log.e(TAG, "摄像头停止失败", e)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "停止摄像头时发生异常", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 拍照
     */
    fun capturePhoto() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "开始拍照")
                
                val result = cameraManager.capturePhoto()
                result.onSuccess { photoPath ->
                    Log.d(TAG, "拍照成功: $photoPath")
                    // TODO: 可以在这里处理拍照成功后的逻辑，比如显示通知
                }.onFailure { e ->
                    Log.e(TAG, "拍照失败", e)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "拍照时发生异常", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 设置摄像头参数
     */
    fun setCameraParameters(
        brightness: Float? = null,
        contrast: Float? = null,
        autoFocus: Boolean? = null
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "设置摄像头参数")
                
                val result = cameraManager.setCameraParameters(brightness, contrast, autoFocus)
                result.onSuccess {
                    Log.d(TAG, "摄像头参数设置成功")
                }.onFailure { e ->
                    Log.e(TAG, "摄像头参数设置失败", e)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "设置摄像头参数时发生异常", e)
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        cameraManager.clearError()
    }

    /**
     * 切换摄像头状态
     */
    fun toggleCamera() {
        if (isCameraActive.value) {
            stopCamera()
        } else {
            startCamera()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 释放摄像头资源
        cameraManager.release()
        Log.d(TAG, "CameraViewModel已清理")
    }
}

/**
 * 摄像头UI状态数据类
 */
data class CameraUiState(
    val isCameraActive: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cameraInfo: CameraInfo = CameraInfo(
        isActive = false,
        resolution = "未知",
        frameRate = "0fps",
        connectionStatus = "未连接"
    ),
    val previewData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CameraUiState

        if (isCameraActive != other.isCameraActive) return false
        if (isLoading != other.isLoading) return false
        if (errorMessage != other.errorMessage) return false
        if (cameraInfo != other.cameraInfo) return false
        if (previewData != null) {
            if (other.previewData == null) return false
            if (!previewData.contentEquals(other.previewData)) return false
        } else if (other.previewData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isCameraActive.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + cameraInfo.hashCode()
        result = 31 * result + (previewData?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * 扩展函数：将多个StateFlow组合成UI状态
 */
fun CameraViewModel.uiState(): StateFlow<CameraUiState> {
    return combine(
        isCameraActive,
        isLoading,
        errorMessage,
        cameraInfo,
        cameraPreviewData
    ) { isCameraActive, isLoading, errorMessage, cameraInfo, previewData ->
        CameraUiState(
            isCameraActive = isCameraActive,
            isLoading = isLoading,
            errorMessage = errorMessage,
            cameraInfo = cameraInfo,
            previewData = previewData
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CameraUiState()
    )
}
