package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToResetPassword: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var countdownTime by remember { mutableStateOf(0) }
    
    // 简化动画 - 使用单一状态
    var isContentVisible by remember { mutableStateOf(false) }
    
    // 简单统一的动画
    LaunchedEffect(Unit) {
        delay(200)
        isContentVisible = true
    }
    
    // 倒计时逻辑
    LaunchedEffect(countdownTime) {
        if (countdownTime > 0) {
            delay(1000)
            countdownTime--
        }
    }
    
    // 创建背景渐变
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surface
    )
    
    // 发送验证码
    fun sendVerificationCode() {
        if (email.isBlank()) {
            errorMessage = "请输入邮箱地址"
            return
        }
        
        if (!email.contains("@")) {
            errorMessage = "请输入有效的邮箱地址"
            return
        }
        
        isLoading = true
        errorMessage = ""
    }
    
    // 监听loading状态模拟发送验证码
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(1500)
            isLoading = false
            isCodeSent = true
            countdownTime = 60
            successMessage = "验证码已发送到您的邮箱"
        }
    }
    
    // 验证验证码
    fun verifyCode() {
        if (verificationCode.isBlank()) {
            errorMessage = "请输入验证码"
            return
        }
        
        if (verificationCode.length != 6) {
            errorMessage = "验证码应为6位数字"
            return
        }
        
        // 临时保存当前验证码以便在LaunchedEffect中使用
        val currentCode = verificationCode
        isLoading = true
        errorMessage = ""
    }
    
    // 监听验证码验证状态
    LaunchedEffect(isLoading, verificationCode) {
        if (isLoading && verificationCode.length == 6) {
            delay(1500)
            
            // 模拟验证成功
            if (verificationCode == "123456") {
                onNavigateToResetPassword()
            } else {
                errorMessage = "验证码错误，请重新输入"
                isLoading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // 顶部导航栏
        TopAppBar(
            title = { Text("找回密码") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        )
        
        // 统一的内容动画
        androidx.compose.animation.AnimatedVisibility(
            visible = isContentVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(
                    durationMillis = 600,
                    easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 600,
                    easing = LinearEasing
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 顶部区域
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 导航栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = "找回密码",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = if (!isCodeSent) "输入您的邮箱地址，我们将发送验证码" else "请输入您收到的6位数验证码",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
                
                // 中间表单区域
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
                            .padding(24.dp)
                    ) {
                        // 错误信息
                        if (errorMessage.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = errorMessage,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // 成功信息
                        if (successMessage.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = successMessage,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        if (!isCodeSent) {
                            // 邮箱输入
                            OutlinedTextField(
                                value = email,
                                onValueChange = { 
                                    email = it
                                    errorMessage = ""
                                    successMessage = ""
                                },
                                label = { Text("邮箱地址") },
                                placeholder = { Text("请输入您的邮箱") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Email, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    ) 
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                enabled = !isLoading
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // 发送验证码按钮
                            Button(
                                onClick = { sendVerificationCode() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = email.isNotBlank() && !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "发送验证码",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // 验证码输入
                            OutlinedTextField(
                                value = verificationCode,
                                onValueChange = { 
                                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                        verificationCode = it
                                        errorMessage = ""
                                        successMessage = ""
                                    }
                                },
                                label = { Text("验证码") },
                                placeholder = { Text("请输入6位数验证码") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Security, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    ) 
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                enabled = !isLoading
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 重新发送验证码
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "没有收到验证码？",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                TextButton(
                                    onClick = { 
                                        if (countdownTime == 0) {
                                            sendVerificationCode()
                                        }
                                    },
                                    enabled = countdownTime == 0 && !isLoading
                                ) {
                                    Text(
                                        if (countdownTime > 0) "${countdownTime}s后重新发送" else "重新发送",
                                        color = if (countdownTime > 0) 
                                            MaterialTheme.colorScheme.tertiary 
                                        else 
                                            MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // 验证按钮
                            Button(
                                onClick = { verifyCode() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = verificationCode.length == 6 && !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "验证",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 底部提示
                Text(
                    text = if (!isCodeSent) 
                        "验证码将发送到您的邮箱，请注意查收" 
                    else 
                        "验证码有效期为10分钟，测试验证码：123456",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )
            }
        }
    }
} 