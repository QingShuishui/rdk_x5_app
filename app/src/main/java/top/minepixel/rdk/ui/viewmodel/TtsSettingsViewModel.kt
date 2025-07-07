package top.minepixel.rdk.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.minepixel.rdk.data.manager.ByteDanceTtsManager
import top.minepixel.rdk.data.model.TtsConstants
import javax.inject.Inject

/**
 * TTS设置界面ViewModel
 */
@HiltViewModel
class TtsSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val byteDanceTtsManager: ByteDanceTtsManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "TtsSettingsViewModel"
        private const val PREFS_NAME = "tts_settings"
        private const val KEY_VOICE_TYPE = "voice_type"
        private const val KEY_SPEED_RATIO = "speed_ratio"
        private const val KEY_LOUDNESS_RATIO = "loudness_ratio"
        private const val TEST_TEXT = "你好，这是语音合成测试。"
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // UI状态
    private val _uiState = MutableStateFlow(TtsSettingsUiState())
    val uiState: StateFlow<TtsSettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
        observeTtsState()
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        val voiceType = sharedPreferences.getString(KEY_VOICE_TYPE, TtsConstants.VOICE_TYPE_FEMALE)
            ?: TtsConstants.VOICE_TYPE_FEMALE
        val speedRatio = sharedPreferences.getFloat(KEY_SPEED_RATIO, 1.0f)
        val loudnessRatio = sharedPreferences.getFloat(KEY_LOUDNESS_RATIO, 1.0f)
        
        _uiState.value = _uiState.value.copy(
            voiceType = voiceType,
            speedRatio = speedRatio,
            loudnessRatio = loudnessRatio
        )
        
        Log.d(TAG, "加载TTS设置: voiceType=$voiceType, speedRatio=$speedRatio, loudnessRatio=$loudnessRatio")
    }
    
    /**
     * 观察TTS状态
     */
    private fun observeTtsState() {
        viewModelScope.launch {
            byteDanceTtsManager.isPlaying.collect { isPlaying ->
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }
        }
        
        viewModelScope.launch {
            byteDanceTtsManager.error.collect { error ->
                _uiState.value = _uiState.value.copy(errorMessage = error)
            }
        }
    }
    
    /**
     * 设置音色类型
     */
    fun setVoiceType(voiceType: String) {
        _uiState.value = _uiState.value.copy(voiceType = voiceType)
        saveSettings()
        Log.d(TAG, "设置音色类型: $voiceType")
    }
    
    /**
     * 设置语速
     */
    fun setSpeedRatio(speedRatio: Float) {
        val clampedRatio = speedRatio.coerceIn(TtsConstants.MIN_SPEED_RATIO, TtsConstants.MAX_SPEED_RATIO)
        _uiState.value = _uiState.value.copy(speedRatio = clampedRatio)
        saveSettings()
        Log.d(TAG, "设置语速: $clampedRatio")
    }
    
    /**
     * 设置音量
     */
    fun setLoudnessRatio(loudnessRatio: Float) {
        val clampedRatio = loudnessRatio.coerceIn(TtsConstants.MIN_LOUDNESS_RATIO, TtsConstants.MAX_LOUDNESS_RATIO)
        _uiState.value = _uiState.value.copy(loudnessRatio = clampedRatio)
        saveSettings()
        Log.d(TAG, "设置音量: $clampedRatio")
    }
    
    /**
     * 测试TTS
     */
    fun testTts() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始测试TTS")
                val currentState = _uiState.value
                
                byteDanceTtsManager.speakText(
                    text = TEST_TEXT,
                    voiceType = currentState.voiceType,
                    speedRatio = currentState.speedRatio,
                    loudnessRatio = currentState.loudnessRatio
                ).onFailure { e ->
                    Log.e(TAG, "TTS测试失败", e)
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "TTS测试失败: ${e.message}"
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "TTS测试异常", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "TTS测试异常: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清理缓存
     */
    fun clearCache() {
        try {
            byteDanceTtsManager.clearCache()
            Log.d(TAG, "TTS缓存清理完成")
        } catch (e: Exception) {
            Log.e(TAG, "清理TTS缓存失败", e)
            _uiState.value = _uiState.value.copy(
                errorMessage = "清理缓存失败: ${e.message}"
            )
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        byteDanceTtsManager.clearError()
    }
    
    /**
     * 保存设置
     */
    private fun saveSettings() {
        val currentState = _uiState.value
        sharedPreferences.edit()
            .putString(KEY_VOICE_TYPE, currentState.voiceType)
            .putFloat(KEY_SPEED_RATIO, currentState.speedRatio)
            .putFloat(KEY_LOUDNESS_RATIO, currentState.loudnessRatio)
            .apply()
    }
    
    /**
     * 获取当前设置（供其他组件使用）
     */
    fun getCurrentSettings(): TtsSettings {
        val currentState = _uiState.value
        return TtsSettings(
            voiceType = currentState.voiceType,
            speedRatio = currentState.speedRatio,
            loudnessRatio = currentState.loudnessRatio
        )
    }
}

/**
 * TTS设置UI状态
 */
data class TtsSettingsUiState(
    val voiceType: String = TtsConstants.VOICE_TYPE_FEMALE,
    val speedRatio: Float = 1.0f,
    val loudnessRatio: Float = 1.0f,
    val isPlaying: Boolean = false,
    val errorMessage: String? = null
)

/**
 * TTS设置数据类
 */
data class TtsSettings(
    val voiceType: String,
    val speedRatio: Float,
    val loudnessRatio: Float
)
