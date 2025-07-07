package top.minepixel.rdk.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import top.minepixel.rdk.data.model.VoiceAssistantState
import top.minepixel.rdk.ui.viewmodel.VoiceAssistantViewModel
import top.minepixel.rdk.ui.viewmodel.VoiceAssistantTestViewModel
import top.minepixel.rdk.ui.viewmodel.RealVoiceAssistantViewModel
import top.minepixel.rdk.ui.viewmodel.BaiduVoiceAssistantViewModel
import top.minepixel.rdk.utils.PermissionUtils
import top.minepixel.rdk.ui.components.MiCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceControlScreen(
    onNavigateBack: () -> Unit = {},
    voiceAssistantViewModel: BaiduVoiceAssistantViewModel = hiltViewModel()
) {
    var showContent by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("VoiceControlScreen", "权限请求结果: $permissions")

        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val modifyAudioGranted = permissions[Manifest.permission.MODIFY_AUDIO_SETTINGS] ?: false

        Log.d("VoiceControlScreen", "录音权限: $recordAudioGranted, 音频设置权限: $modifyAudioGranted")

        if (recordAudioGranted) {
            Log.d("VoiceControlScreen", "录音权限授予成功，开始录音")
            voiceAssistantViewModel.startRecording()
        } else {
            Log.e("VoiceControlScreen", "录音权限被拒绝")
            // 可以显示一个提示对话框告诉用户需要权限
        }
    }

    // 检查权限的函数
    fun checkAndRequestPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )

        val hasAllPermissions = permissions.all { permission ->
            val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            Log.d("VoiceControlScreen", "权限检查: $permission = $granted")
            granted
        }

        Log.d("VoiceControlScreen", "所有权限检查结果: $hasAllPermissions")

        return if (hasAllPermissions) {
            Log.d("VoiceControlScreen", "所有权限已授予")
            true
        } else {
            Log.d("VoiceControlScreen", "缺少权限，开始请求录音权限")
            permissionLauncher.launch(permissions)
            false
        }
    }

    // 观察语音助手状态
    val assistantState: VoiceAssistantState by voiceAssistantViewModel.assistantState.collectAsState()
    val messages: List<top.minepixel.rdk.data.model.VoiceMessage> by voiceAssistantViewModel.messages.collectAsState()
    val currentSpeakingText: String by voiceAssistantViewModel.currentSpeakingText.collectAsState()
    val errorMessage: String? by voiceAssistantViewModel.errorMessage.collectAsState()
    val isProcessing: Boolean by voiceAssistantViewModel.isProcessing.collectAsState()

    // 计算UI状态
    val isListening = assistantState == VoiceAssistantState.LISTENING
    val isSpeaking = assistantState == VoiceAssistantState.SPEAKING
    val recognizedText = messages.lastOrNull { it.isFromUser }?.content ?: ""
    
    // 麦克风动画
    val micScale by animateFloatAsState(
        targetValue = if (isListening || isSpeaking) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "mic_scale"
    )

    // 处理错误消息
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Log.d("VoiceControlScreen", "显示错误消息: $errorMessage")
            delay(3000)
            voiceAssistantViewModel.clearError()
        }
    }

    // 监听状态变化
    LaunchedEffect(assistantState) {
        Log.d("VoiceControlScreen", "状态变化: $assistantState")
    }

    // 监听消息变化
    LaunchedEffect(messages.size) {
        Log.d("VoiceControlScreen", "消息数量变化: ${messages.size}")
        messages.forEach { message ->
            Log.d("VoiceControlScreen", "消息: ${message.content} (来自用户: ${message.isFromUser})")
        }
    }
    
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }
    
    // 这个LaunchedEffect已经不需要了，因为现在使用真实的语音助手
    // 删除模拟的语音识别过程
    
    // 创建背景渐变
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.surface
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // 顶部导航栏
        TopAppBar(
            title = { Text("语音控制") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        )
        
        // 对话历史区域 - 固定在顶部
        AnimatedVisibility(
            visible = showContent && messages.isNotEmpty(),
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(800, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800)) + expandVertically(
                animationSpec = tween(800, easing = EaseOutCubic)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(400, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(400)) + shrinkVertically(
                animationSpec = tween(400, easing = EaseInCubic)
            )
        ) {
            ConversationHistorySection(
                messages = messages,
                assistantState = assistantState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                onClearHistory = {
                    voiceAssistantViewModel.clearConversation()
                }
            )
        }

        // 主要内容区域
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 如果有对话历史，减少顶部间距
                Spacer(modifier = Modifier.height(if (messages.isNotEmpty()) 16.dp else 32.dp))
                
                // 顶部提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 500f)
                            ),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    // 装饰性背景圆点
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .offset(x = 250.dp, y = (-30).dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                                CircleShape
                            )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .offset(x = (-20).dp, y = 60.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f),
                                CircleShape
                            )
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "语音助手",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "语音智能助手",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "说出指令，让机器人为您服务",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 麦克风区域
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(micScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isListening) {
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                    )
                                } else {
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surface
                                    )
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isListening) "停止录音" else "开始录音",
                            modifier = Modifier.size(80.dp),
                            tint = if (isListening) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 状态文本
                Text(
                    text = when {
                        isProcessing -> "正在处理..."
                        isListening -> "正在聆听您的指令..."
                        isSpeaking -> "正在播放：$currentSpeakingText"
                        errorMessage != null -> "错误：$errorMessage"
                        recognizedText.isNotEmpty() -> "识别到：$recognizedText"
                        assistantState == VoiceAssistantState.ERROR -> "录音失败，请检查权限"
                        else -> "点击麦克风开始语音控制"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = when {
                        errorMessage != null -> MaterialTheme.colorScheme.error
                        isListening || isSpeaking -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = if (isListening || isSpeaking) FontWeight.Bold else FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 控制按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            Log.d("VoiceControlScreen", "按钮点击，当前状态: $assistantState")
                            when (assistantState) {
                                VoiceAssistantState.IDLE -> {
                                    Log.d("VoiceControlScreen", "检查权限并开始录音")
                                    if (checkAndRequestPermissions()) {
                                        voiceAssistantViewModel.startRecording()
                                    }
                                }
                                VoiceAssistantState.LISTENING -> {
                                    Log.d("VoiceControlScreen", "停止录音并处理")
                                    voiceAssistantViewModel.stopRecordingAndProcess()
                                }
                                VoiceAssistantState.SPEAKING -> {
                                    Log.d("VoiceControlScreen", "停止播放")
                                    voiceAssistantViewModel.stopSpeaking()
                                }
                                else -> {
                                    Log.d("VoiceControlScreen", "其他状态: $assistantState")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = assistantState != VoiceAssistantState.PROCESSING,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (assistantState) {
                                VoiceAssistantState.LISTENING -> MaterialTheme.colorScheme.error
                                VoiceAssistantState.SPEAKING -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Icon(
                            imageVector = when (assistantState) {
                                VoiceAssistantState.LISTENING -> Icons.Default.Stop
                                VoiceAssistantState.SPEAKING -> Icons.AutoMirrored.Filled.VolumeOff
                                else -> Icons.Default.Mic
                            },
                            contentDescription = when (assistantState) {
                                VoiceAssistantState.LISTENING -> "停止录音"
                                VoiceAssistantState.SPEAKING -> "停止播放"
                                else -> "开始录音"
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (assistantState) {
                                VoiceAssistantState.LISTENING -> "停止录音"
                                VoiceAssistantState.SPEAKING -> "停止播放"
                                else -> "开始录音"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 清除对话按钮
                    Button(
                        onClick = { voiceAssistantViewModel.clearConversation() },
                        modifier = Modifier
                            .height(56.dp),
                        enabled = messages.isNotEmpty(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除对话",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Spacer(modifier = Modifier.weight(1f))
                
                // 常用指令提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                ),
                                start = Offset(0f, 300f),
                                end = Offset(800f, 0f)
                            ),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    // 装饰性几何形状
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = (-30).dp, y = (-20).dp)
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f),
                                CircleShape
                            )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .offset(x = 280.dp, y = 120.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                                CircleShape
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            Text(
                                text = "常用语音指令",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            VoiceCommandItem("开始清洁", "启动机器人进行清洁") {
                                voiceAssistantViewModel.sendTextMessage("开始清洁")
                            }
                            VoiceCommandItem("停止清洁", "停止当前清洁任务") {
                                voiceAssistantViewModel.sendTextMessage("停止清洁")
                            }
                            VoiceCommandItem("回到基站", "让机器人返回充电座") {
                                voiceAssistantViewModel.sendTextMessage("回到基站")
                            }
                            VoiceCommandItem("找机器人", "机器人发出声音帮助定位") {
                                voiceAssistantViewModel.sendTextMessage("找机器人")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * 对话历史区域组件 - 显示在屏幕顶部
 */
@Composable
fun ConversationHistorySection(
    messages: List<top.minepixel.rdk.data.model.VoiceMessage>,
    assistantState: VoiceAssistantState,
    modifier: Modifier = Modifier,
    onClearHistory: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .heightIn(max = 280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "对话历史",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "对话记录",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // 状态指示器
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            // 状态指示点 - 添加脉冲动画
                            val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = if (assistantState != VoiceAssistantState.IDLE) 1.3f else 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = EaseInOutCubic),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .scale(pulseScale)
                                    .background(
                                        when (assistantState) {
                                            VoiceAssistantState.LISTENING -> MaterialTheme.colorScheme.error
                                            VoiceAssistantState.PROCESSING -> MaterialTheme.colorScheme.tertiary
                                            VoiceAssistantState.SPEAKING -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.outline
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (assistantState) {
                                    VoiceAssistantState.LISTENING -> "正在听取"
                                    VoiceAssistantState.PROCESSING -> "正在思考"
                                    VoiceAssistantState.SPEAKING -> "正在回复"
                                    else -> "待机中"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 消息数量标识
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${messages.size}条",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // 清空按钮
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清空对话",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 消息列表
            LazyColumn(
                modifier = Modifier.heightIn(max = 180.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true // 最新消息在顶部
            ) {
                items(
                    items = messages.takeLast(8).reversed(), // 显示最近8条消息，倒序显示
                    key = { it.id }
                ) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInHorizontally(
                            initialOffsetX = { if (message.isFromUser) it else -it },
                            animationSpec = tween(400, easing = EaseOutCubic)
                        ) + fadeIn(animationSpec = tween(400))
                    ) {
                        EnhancedMessageItem(message = message)
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedMessageItem(message: top.minepixel.rdk.data.model.VoiceMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 用户/助手头像
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (message.isFromUser) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (message.isFromUser) Icons.Default.Person else Icons.Default.SmartToy,
                            contentDescription = if (message.isFromUser) "用户" else "助手",
                            tint = if (message.isFromUser) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 消息类型图标
                    if (message.type == top.minepixel.rdk.data.model.MessageType.AUDIO) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "语音消息",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // 发送者标识
                    Text(
                        text = if (message.isFromUser) "我" else "助手",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 消息内容
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 时间戳
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(if (message.isFromUser) Alignment.End else Alignment.Start)
                )
            }
        }
    }
}

@Composable
fun MessageItem(message: top.minepixel.rdk.data.model.VoiceMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 250.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 显示消息类型图标
                    if (message.type == top.minepixel.rdk.data.model.MessageType.AUDIO) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "语音消息",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (message.isFromUser) "我" else "助手",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(if (message.isFromUser) Alignment.End else Alignment.Start)
                )
            }
        }
    }
}

@Composable
fun VoiceCommandItem(
    command: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 100f)
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "\"$command\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 格式化时间戳
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
        else -> {
            val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}