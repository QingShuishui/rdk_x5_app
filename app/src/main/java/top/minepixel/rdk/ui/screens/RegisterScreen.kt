package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isAgreedToTerms by remember { mutableStateOf(false) }
    
    // 控制元素进入动画
    var showHeader by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(false) }
    var showAgreement by remember { mutableStateOf(false) }
    var showSubmitButton by remember { mutableStateOf(false) }
    var showBottomText by remember { mutableStateOf(false) }
    
    // 顺序播放动画
    LaunchedEffect(Unit) {
        showHeader = true
        delay(200)
        showForm = true
        delay(300)
        showAgreement = true
        delay(150)
        showSubmitButton = true
        delay(150)
        showBottomText = true
    }
    
    // 创建背景渐变
    val gradientColors = listOf(
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surface
    )
    
    // 验证逻辑
    fun validateAndRegister() {
        when {
            username.isBlank() -> errorMessage = "请输入用户名"
            username.length < 3 -> errorMessage = "用户名至少3个字符"
            email.isBlank() -> errorMessage = "请输入邮箱"
            !email.contains("@") -> errorMessage = "请输入有效的邮箱地址"
            password.isBlank() -> errorMessage = "请输入密码"
            password.length < 6 -> errorMessage = "密码至少6个字符"
            confirmPassword != password -> errorMessage = "两次输入的密码不一致"
            phoneNumber.isBlank() -> errorMessage = "请输入手机号"
            phoneNumber.length != 11 -> errorMessage = "请输入有效的手机号"
            !isAgreedToTerms -> errorMessage = "请阅读并同意用户协议"
            else -> {
                isLoading = true
                errorMessage = ""
            }
        }
    }
    
    // 监听loading状态进行模拟注册
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(2000) // 模拟网络请求
            onRegisterSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // 顶部标题 - 改进动画
            AnimatedVisibility(
                visible = showHeader,
                enter = slideInVertically(
                    initialOffsetY = { -80 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 400)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
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
                            text = "注册账号",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(48.dp)) // 平衡布局
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "创建您的智能机器人账号",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            // 注册表单 - 改进动画
            AnimatedVisibility(
                visible = showForm,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(
                    initialAlpha = 0.3f,
                    animationSpec = tween(durationMillis = 600)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        // 错误信息提示
                        if (errorMessage.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(8.dp)
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
                        
                        // 用户名
                        OutlinedTextField(
                            value = username,
                            onValueChange = { 
                                username = it
                                errorMessage = ""
                            },
                            label = { Text("用户名") },
                            placeholder = { Text("请输入用户名") },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Person, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            singleLine = true,
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 邮箱
                        OutlinedTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                errorMessage = ""
                            },
                            label = { Text("邮箱") },
                            placeholder = { Text("请输入邮箱地址") },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Email, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                ) 
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            singleLine = true,
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 手机号
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { 
                                if (it.length <= 11 && it.all { char -> char.isDigit() }) {
                                    phoneNumber = it
                                    errorMessage = ""
                                }
                            },
                            label = { Text("手机号") },
                            placeholder = { Text("请输入手机号") },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Phone, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                ) 
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            singleLine = true,
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 密码
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                errorMessage = ""
                            },
                            label = { Text("密码") },
                            placeholder = { Text("请输入密码") },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Lock, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                ) 
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            singleLine = true,
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 确认密码
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it
                                errorMessage = ""
                            },
                            label = { Text("确认密码") },
                            placeholder = { Text("请再次输入密码") },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Lock, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                ) 
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = if (confirmPassword.isNotEmpty() && confirmPassword == password) 
                                    MaterialTheme.colorScheme.primary 
                                else if (confirmPassword.isNotEmpty() && confirmPassword != password)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            singleLine = true,
                            enabled = !isLoading
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 用户协议 - 添加动画
            AnimatedVisibility(
                visible = showAgreement,
                enter = slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 400)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAgreedToTerms,
                        onCheckedChange = { isAgreedToTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.secondary
                        ),
                        enabled = !isLoading
                    )
                    
                    Text(
                        text = "我已阅读并同意",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    TextButton(
                        onClick = { /* 打开用户协议 */ },
                        enabled = !isLoading
                    ) {
                        Text(
                            "用户协议",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Text(
                        text = "和",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    TextButton(
                        onClick = { /* 打开隐私政策 */ },
                        enabled = !isLoading
                    ) {
                        Text(
                            "隐私政策",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 注册按钮 - 添加动画
            AnimatedVisibility(
                visible = showSubmitButton,
                enter = scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 400)
                )
            ) {
                Button(
                    onClick = { validateAndRegister() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = username.isNotBlank() && email.isNotBlank() && 
                            password.isNotBlank() && confirmPassword.isNotBlank() && 
                            phoneNumber.isNotBlank() && isAgreedToTerms && !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "注册",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 返回登录 - 添加动画
            AnimatedVisibility(
                visible = showBottomText,
                enter = slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(
                        durationMillis = 400,
                        delayMillis = 100,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 400,
                        delayMillis = 100
                    )
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "已有账号？",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    TextButton(onClick = onNavigateBack) {
                        Text(
                            "立即登录",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
} 