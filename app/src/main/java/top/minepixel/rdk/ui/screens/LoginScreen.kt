package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import top.minepixel.rdk.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var showErrorShake by remember { mutableStateOf(false) }

    // 观察登录状态
    val loginState by authViewModel.loginState.collectAsState()
    val isLoading = loginState.isLoading
    val errorMessage = loginState.errorMessage ?: ""
    
    // 简化动画 - 只使用一个统一的显示状态
    var isVisible by remember { mutableStateOf(false) }
    
    // 成功动画相关状态
    val successScale by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "success_scale"
    )
    
    // 错误抖动动画
    val shakeOffset by animateFloatAsState(
        targetValue = if (showErrorShake) 10f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { showErrorShake = false },
        label = "shake_offset"
    )
    
    // 简单的出现动画
    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }
    
    // 创建背景渐变
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surface
    )
    
    // 登录逻辑
    fun performLogin() {
        if (username.isBlank() || password.isBlank()) {
            showErrorShake = true
            return
        }

        authViewModel.login(username, password, rememberMe)
    }

    // 监听登录状态变化
    LaunchedEffect(loginState.isSuccess) {
        if (loginState.isSuccess) {
            showSuccessAnimation = true
            delay(600) // 等待成功动画播放
            authViewModel.resetLoginState()
            onLoginSuccess()
        }
    }

    // 监听错误状态
    LaunchedEffect(loginState.errorMessage) {
        if (loginState.errorMessage != null) {
            showErrorShake = true
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        // 统一的内容动画
        androidx.compose.animation.AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(
                    durationMillis = 700,
                    easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 700,
                    easing = LinearEasing
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(x = shakeOffset.dp), // 添加抖动效果
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo区域
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(successScale) // 添加成功缩放效果
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 成功状态显示对勾，否则显示Logo
                    if (showSuccessAnimation) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "登录成功",
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 标题区域
                Text(
                    text = if (showSuccessAnimation) "登录成功!" else "智能机器人",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (showSuccessAnimation) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = if (showSuccessAnimation) "欢迎回来" else "随时掌控您的设备",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // 登录表单
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "欢迎登录",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // 错误信息 - 添加动画效果
                        androidx.compose.animation.AnimatedVisibility(
                            visible = errorMessage.isNotEmpty(),
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "错误",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        
                        if (errorMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // 用户名输入
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                if (loginState.errorMessage != null) {
                                    authViewModel.resetLoginState()
                                }
                            },
                            label = { Text("用户名") },
                            placeholder = { Text("请输入用户名") },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Person, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !isLoading && !showSuccessAnimation
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 密码输入
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (loginState.errorMessage != null) {
                                    authViewModel.resetLoginState()
                                }
                            },
                            label = { Text("密码") },
                            placeholder = { Text("请输入密码") },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Lock, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                ) 
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !isLoading && !showSuccessAnimation
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // 记住我选项
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                enabled = !isLoading && !showSuccessAnimation
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "记住我",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 登录按钮
                        Button(
                            onClick = { performLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = username.isNotBlank() && password.isNotBlank() && !isLoading && !showSuccessAnimation,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showSuccessAnimation) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (showSuccessAnimation) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "成功",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "登录成功",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "登录中...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    "登录",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 底部链接 - 成功时隐藏
                if (!showSuccessAnimation) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onNavigateToForgotPassword) {
                            Text(
                                "忘记密码?",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        TextButton(onClick = onNavigateToRegister) {
                            Text(
                                "注册账号",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 测试提示
                    Text(
                        text = "测试账号: admin / 123456",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 