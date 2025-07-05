package top.minepixel.rdk.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import top.minepixel.rdk.ui.viewmodel.AuthViewModel

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    onAutoLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // 动画状态
    var showLogo by remember { mutableStateOf(false) }
    var showBrand by remember { mutableStateOf(false) }
    var showSlogan by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }
    
    // Logo缩放动画
    val logoScale by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )
    
    // Logo透明度动画
    val logoAlpha by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "logo_alpha"
    )
    
    // 品牌名透明度动画
    val brandAlpha by animateFloatAsState(
        targetValue = if (showBrand) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "brand_alpha"
    )
    
    // 标语透明度动画
    val sloganAlpha by animateFloatAsState(
        targetValue = if (showSlogan) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "slogan_alpha"
    )
    
    // 进度条透明度动画
    val progressAlpha by animateFloatAsState(
        targetValue = if (showProgress) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "progress_alpha"
    )
    
    // 背景渐变动画
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.background
    )
    
    // 启动动画序列
    LaunchedEffect(Unit) {
        // 1. 显示Logo
        delay(300)
        showLogo = true

        // 2. 显示品牌名
        delay(800)
        showBrand = true

        // 3. 显示标语
        delay(400)
        showSlogan = true

        // 4. 显示进度条
        delay(300)
        showProgress = true

        // 5. 等待一段时间后检查登录状态
        delay(1200)

        // 检查是否已登录
        if (authViewModel.isLoggedIn()) {
            onAutoLogin()
        } else {
            onSplashFinished()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo区域
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                            radius = 200f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "App Logo",
                    tint = Color.White,
                    modifier = Modifier.size(70.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 品牌名称
            Text(
                text = "智能机器人",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(brandAlpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 品牌标语
            Text(
                text = "随时掌控您的智能设备",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(sloganAlpha)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 加载进度指示器
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(progressAlpha)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "正在启动...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
        
        // 底部版本信息
        Text(
            text = "版本 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(progressAlpha)
        )
    }
} 