package top.minepixel.rdk.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * 权限工具类
 */
object PermissionUtils {
    
    /**
     * 语音相关权限
     */
    val VOICE_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    )

    /**
     * 语音识别权限
     */
    val SPEECH_RECOGNITION_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )
    
    /**
     * 存储权限
     */
    val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    
    /**
     * 检查权限是否已授予
     */
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 检查语音权限
     */
    fun hasVoicePermissions(context: Context): Boolean {
        return hasPermissions(context, VOICE_PERMISSIONS)
    }
    
    /**
     * 检查存储权限
     */
    fun hasStoragePermissions(context: Context): Boolean {
        return hasPermissions(context, STORAGE_PERMISSIONS)
    }
    
    /**
     * 请求权限的扩展函数
     */
    fun ComponentActivity.requestVoicePermissions(
        onGranted: () -> Unit,
        onDenied: () -> Unit = {}
    ) {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                onGranted()
            } else {
                onDenied()
            }
        }
        
        if (hasVoicePermissions(this)) {
            onGranted()
        } else {
            requestPermissionLauncher.launch(VOICE_PERMISSIONS)
        }
    }
}
