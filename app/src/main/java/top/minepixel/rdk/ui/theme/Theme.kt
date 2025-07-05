package top.minepixel.rdk.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 米家风格配色方案 - 浅色模式
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3F7EDE),         // 蓝色强调色，类似米家App
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECF4FF), // 非常淡的蓝色背景
    onPrimaryContainer = Color(0xFF0F56B3),
    secondary = Color(0xFFFF6700),        // 橙色辅助色，米家常用
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECE1),
    onSecondaryContainer = Color(0xFFAE4600),
    tertiary = Color(0xFF636363),         // 浅灰色，用于次要文字
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5F5F5),
    onTertiaryContainer = Color(0xFF333333),
    error = Color(0xFFFF5252),            // 红色错误提示
    background = Color(0xFFFFFFFF),       // 纯白背景
    onBackground = Color(0xFF333333),     // 深灰文字
    surface = Color(0xFFFFFFFF),          // 纯白表面
    onSurface = Color(0xFF333333),
    surfaceVariant = Color(0xFFF7F7F7),   // 浅灰色变体，用于卡片背景
    outlineVariant = Color(0xFFE0E0E0)    // 边框颜色，浅灰
)

// 米家风格配色方案 - 深色模式 (较少使用，但仍然保留)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81AFFD),         // 淡蓝色
    onPrimary = Color(0xFF00387C),
    primaryContainer = Color(0xFF2F5DA8), // 蓝色背景
    onPrimaryContainer = Color(0xFFD5E3FF),
    secondary = Color(0xFFFFAB7D),        // 淡橙色
    onSecondary = Color(0xFF7A3100),
    secondaryContainer = Color(0xFFAB4600),
    onSecondaryContainer = Color(0xFFFFECE2),
    tertiary = Color(0xFFB6B6B6),         // 灰色
    onTertiary = Color(0xFF363636),
    tertiaryContainer = Color(0xFF4D4D4D),
    onTertiaryContainer = Color(0xFFE6E6E6),
    error = Color(0xFFFF8A8A),            // 淡红色
    background = Color(0xFF121212),       // 深黑色背景
    onBackground = Color(0xFFE6E6E6),
    surface = Color(0xFF1E1E1E),          // 深灰色表面
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF2D2D2D),   // 深灰色变体
    outlineVariant = Color(0xFF444444)    // 边框颜色
)

@Composable
fun RobotCleanerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // 设置为false，使用自定义配色
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // 设置状态栏和导航栏为透明
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            // 根据主题设置状态栏和导航栏图标颜色
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MiTypography,
        content = content
    )
} 