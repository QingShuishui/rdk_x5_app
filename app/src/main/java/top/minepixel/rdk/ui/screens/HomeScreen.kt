package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import top.minepixel.rdk.data.model.CleaningMode
import top.minepixel.rdk.data.model.CommandAction
import top.minepixel.rdk.data.model.RobotCommand
import top.minepixel.rdk.data.model.RobotMode
import top.minepixel.rdk.ui.components.MiCard
import top.minepixel.rdk.ui.components.MiClickableCard
import top.minepixel.rdk.ui.components.MiOutlinedButton
import top.minepixel.rdk.ui.components.MiPrimaryButton
import top.minepixel.rdk.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToMyDevices: () -> Unit = {},
    onNavigateToAddDevice: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToVoice: () -> Unit = {}
) {
    val robotStatus by viewModel.robotStatus.collectAsState()
    val detectedItems by viewModel.detectedItems.collectAsState()
    
    // 创建进入动画状态
    val animationState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    
    LaunchedEffect(Unit) {
        // 动画由现有的animationState处理，这里保持空即可
    }
    
    // 创建背景渐变
    val gradientColors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .safeDrawingPadding() // 使用safeDrawingPadding替代statusBarsPadding
    ) {
        // 欢迎信息
        AnimatedVisibility(
            visibleState = animationState,
            enter = fadeIn() + expandVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "智能家居",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "控制中心",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person, 
                        contentDescription = "个人信息",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // 可滚动内容区域
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 顶部状态栏
            item {
                AnimatedVisibility(
                    visibleState = animationState,
                    enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(initialAlpha = 0.3f),
                    exit = fadeOut()
                ) {
                    RobotStatusBar(
                        isOnline = robotStatus.isOnline,
                        batteryLevel = robotStatus.battery,
                        robotMode = robotStatus.mode
                    )
                }
            }
            
            // 设备管理模块
            item {
                var showDeviceSection by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(100)
                    showDeviceSection = true
                }
                
                AnimatedVisibility(
                    visible = showDeviceSection,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { 40 })
                ) {
                    DeviceManagementSection(
                        onNavigateToMyDevices = onNavigateToMyDevices,
                        onNavigateToAddDevice = onNavigateToAddDevice,
                        totalDevices = 1,
                        onlineDevices = 1
                    )
                }
            }
            
            // 任务管理模块
            item {
                var showTaskSection by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(150)
                    showTaskSection = true
                }
                
                AnimatedVisibility(
                    visible = showTaskSection,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { 40 })
                ) {
                    TaskManagementSection(
                        robotMode = robotStatus.mode,
                        cleaningProgress = robotStatus.cleaningProgress,
                        onNavigateToTasks = onNavigateToTasks,
                        onStartCleaning = { 
                            viewModel.sendCommand(RobotCommand(CommandAction.START_CLEANING))
                        },
                        onStopCleaning = {
                            viewModel.sendCommand(RobotCommand(CommandAction.STOP_CLEANING))
                        },
                        onReturnToDock = {
                            viewModel.sendCommand(RobotCommand(CommandAction.RETURN_TO_DOCK))
                        }
                    )
                }
            }
            
            // 检测到的物品
            if (detectedItems.isNotEmpty()) {
                item {
                    var showDetectedItems by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(200)
                        showDetectedItems = true
                    }
                    
                    AnimatedVisibility(
                        visible = showDetectedItems,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { 40 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                    ) {
                        DetectedItemsPreview(
                            detectedItems = detectedItems.size,
                            onViewAll = {}
                        )
                    }
                }
            }
            
            // 快速操作按钮
            item {
                var showQuickActions by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(250)
                    showQuickActions = true
                }
                
                AnimatedVisibility(
                    visible = showQuickActions,
                    enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                ) {
                    QuickActions(
                        onSpotCleaning = {
                            val params = mapOf("mode" to CleaningMode.SPOT.name)
                            viewModel.sendCommand(RobotCommand(CommandAction.START_CLEANING, params))
                        },
                        onFindRobot = {
                            viewModel.sendCommand(RobotCommand(CommandAction.LOCATE))
                        },
                        onVoiceCommand = onNavigateToVoice,
                        onViewSchedule = onNavigateToTasks
                    )
                }
            }
            
            // 底部连接按钮
            item {
                MiCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = if (robotStatus.isOnline) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (robotStatus.isOnline) 
                                        Icons.Default.SignalWifi4Bar 
                                    else 
                                        Icons.Default.SignalWifiOff,
                                    contentDescription = "连接状态",
                                    tint = if (robotStatus.isOnline) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (robotStatus.isOnline) "已连接" else "未连接",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (robotStatus.isOnline)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.tertiary,
                                fontWeight = if (robotStatus.isOnline) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                        
                        val buttonScale by animateFloatAsState(
                            targetValue = if (robotStatus.isOnline) 1f else 1.05f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "按钮缩放"
                        )
                        
                        if (robotStatus.isOnline) {
                            MiOutlinedButton(
                                onClick = { viewModel.disconnectFromRobot() },
                                modifier = Modifier.scale(buttonScale)
                            ) {
                                Text("断开连接")
                            }
                        } else {
                            MiPrimaryButton(
                                onClick = { viewModel.connectToRobot("demo_robot_1") },
                                modifier = Modifier.scale(buttonScale)
                            ) {
                                Text("连接机器人")
                            }
                        }
                    }
                }
            }
            
            // 底部间距
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RobotStatusBar(
    isOnline: Boolean,
    batteryLevel: Int,
    robotMode: RobotMode
) {
    // 为状态栏添加米家风格的卡片效果
    MiCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 连接状态
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconColor by animateColorAsState(
                    targetValue = if (isOnline) MaterialTheme.colorScheme.primary else Color(0xFFFF6700),
                    animationSpec = tween(durationMillis = 500),
                    label = "连接状态颜色"
                )
                
                Icon(
                    imageVector = if (isOnline) Icons.Default.Check else Icons.Default.Clear,
                    contentDescription = "连接状态",
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isOnline) "在线" else "离线",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // 电池状态
            Row(verticalAlignment = Alignment.CenterVertically) {
                val batteryIcon = when {
                    batteryLevel > 80 -> Icons.Default.BatteryFull
                    batteryLevel > 60 -> Icons.Default.Battery6Bar
                    batteryLevel > 40 -> Icons.Default.Battery4Bar
                    batteryLevel > 20 -> Icons.Default.Battery2Bar
                    else -> Icons.Default.BatteryAlert
                }
                
                val batteryColor = when {
                    batteryLevel > 50 -> MaterialTheme.colorScheme.primary
                    batteryLevel > 20 -> MaterialTheme.colorScheme.secondary
                    else -> Color(0xFFFF5252)
                }
                
                Icon(
                    imageVector = batteryIcon,
                    contentDescription = "电池状态",
                    tint = batteryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$batteryLevel%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // 工作模式
            val modeText = when (robotMode) {
                RobotMode.IDLE -> "待机中"
                RobotMode.CLEANING -> "清洁中"
                RobotMode.CHARGING -> "充电中"
                RobotMode.RETURNING_TO_DOCK -> "回基站中"
                RobotMode.ERROR -> "错误状态"
            }
            
            val modeColor = when (robotMode) {
                RobotMode.CLEANING -> MaterialTheme.colorScheme.primary
                RobotMode.ERROR -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.tertiary
            }
            
            Text(
                text = modeText,
                style = MaterialTheme.typography.bodySmall,
                color = modeColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusCard(
    robotName: String,
    robotMode: RobotMode,
    cleaningProgress: Int,
    onStartCleaning: () -> Unit,
    onStopCleaning: () -> Unit,
    onPauseCleaning: () -> Unit,
    onResumeCleaning: () -> Unit,
    onReturnToDock: () -> Unit
) {
    // 为卡片添加动画效果
    val containerColor = when (robotMode) {
        RobotMode.CLEANING -> MaterialTheme.colorScheme.primaryContainer
        RobotMode.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    MiCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = containerColor
    ) {
        // 机器人名称和状态
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = robotName,
                    style = MaterialTheme.typography.titleMedium
                )
                
                val statusText = when (robotMode) {
                    RobotMode.IDLE -> "待机中"
                    RobotMode.CLEANING -> "清洁中"
                    RobotMode.CHARGING -> "充电中"
                    RobotMode.RETURNING_TO_DOCK -> "回基站中"
                    RobotMode.ERROR -> "错误状态"
                }
                
                val statusColor by animateColorAsState(
                    targetValue = when (robotMode) {
                        RobotMode.CLEANING -> MaterialTheme.colorScheme.primary
                        RobotMode.ERROR -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.tertiary
                    },
                    animationSpec = tween(durationMillis = 500),
                    label = "状态文本颜色"
                )
                
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor
                )
            }
            
            // 显示对应模式的图标
            val modeIcon = when (robotMode) {
                RobotMode.IDLE -> Icons.Default.Home
                RobotMode.CLEANING -> Icons.Default.Refresh
                RobotMode.CHARGING -> Icons.Default.PowerSettingsNew
                RobotMode.RETURNING_TO_DOCK -> Icons.Default.ArrowBack
                RobotMode.ERROR -> Icons.Default.Warning
            }
            
            val iconScale by animateFloatAsState(
                targetValue = if (robotMode == RobotMode.CLEANING) 1.2f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "图标缩放"
            )
            
            Icon(
                imageVector = modeIcon,
                contentDescription = "机器人模式",
                modifier = Modifier
                    .size(40.dp)
                    .scale(iconScale),
                tint = when (robotMode) {
                    RobotMode.CLEANING -> MaterialTheme.colorScheme.primary
                    RobotMode.ERROR -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                }
            )
        }
        
        // 显示清洁进度 (仅在清洁模式时)
        if (robotMode == RobotMode.CLEANING) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "清洁进度",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "$cleaningProgress%",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { cleaningProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
        
        // 控制按钮
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (robotMode) {
                RobotMode.IDLE -> {
                    MiPrimaryButton(
                        onClick = onStartCleaning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "开始清洁",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("开始清洁", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                RobotMode.CLEANING -> {
                    MiOutlinedButton(
                        onClick = onPauseCleaning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "暂停",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("暂停", style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    MiPrimaryButton(
                        onClick = onStopCleaning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "停止",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("停止", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                RobotMode.RETURNING_TO_DOCK -> {
                    MiOutlinedButton(
                        onClick = onStopCleaning,
                        modifier = Modifier.weight(1f),
                        borderColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "取消返回",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("取消返回", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                else -> {
                    MiPrimaryButton(
                        onClick = onReturnToDock,
                        modifier = Modifier.weight(1f),
                        enabled = robotMode != RobotMode.CHARGING
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "返回基站",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("返回基站", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectedItemsPreview(
    detectedItems: Int,
    onViewAll: () -> Unit
) {
    MiClickableCard(
        onClick = onViewAll,
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "检测到的物品",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "检测到 $detectedItems 个物品",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "点击查看详情",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "查看更多",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun QuickActions(
    onSpotCleaning: () -> Unit,
    onFindRobot: () -> Unit,
    onVoiceCommand: () -> Unit,
    onViewSchedule: () -> Unit
) {
    MiCard(
        modifier = Modifier.fillMaxWidth(),
        title = "快速操作"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Default.CleaningServices,
                label = "定点清洁",
                description = "清洁指定区域",
                onClick = onSpotCleaning
            )
            
            QuickActionButton(
                icon = Icons.Default.LocationOn,
                label = "找机器人",
                description = "播放声音定位",
                onClick = onFindRobot
            )
            
            QuickActionButton(
                icon = Icons.Default.Mic,
                label = "语音控制",
                description = "语音指令操作",
                onClick = onVoiceCommand
            )
            
            QuickActionButton(
                icon = Icons.Default.DateRange,
                label = "任务计划",
                description = "查看清洁计划",
                onClick = onViewSchedule
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(75.dp)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(42.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = icon,
                modifier = Modifier.size(20.dp),
                contentDescription = label
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(1.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DeviceManagementSection(
    onNavigateToMyDevices: () -> Unit,
    onNavigateToAddDevice: () -> Unit,
    totalDevices: Int,
    onlineDevices: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(800f, 400f)
                ),
                RoundedCornerShape(24.dp)
            )
    ) {
        // 装饰性背景
        Box(
            modifier = Modifier
                .size(110.dp)
                .offset(x = (-40).dp, y = (-30).dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                    CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(70.dp)
                .offset(x = 300.dp, y = 100.dp)
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f),
                    CircleShape
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "设备管理",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(onClick = onNavigateToMyDevices) {
                    Text("查看全部", style = MaterialTheme.typography.bodySmall)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "查看全部",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            // 设备统计
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HomeDeviceStatItem("$totalDevices", "总设备", Icons.Default.Devices)
                HomeDeviceStatItem("$onlineDevices", "在线", Icons.Default.Wifi)
                HomeDeviceStatItem("${(onlineDevices.toFloat() / totalDevices * 100).toInt()}%", "连接率", Icons.Default.SignalWifi4Bar)
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToMyDevices,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "我的设备",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("我的设备", style = MaterialTheme.typography.bodySmall)
                }
                
                Button(
                    onClick = onNavigateToAddDevice,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加设备",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("添加设备", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun HomeDeviceStatItem(value: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TaskManagementSection(
    robotMode: RobotMode,
    cleaningProgress: Int,
    onNavigateToTasks: () -> Unit,
    onStartCleaning: () -> Unit,
    onStopCleaning: () -> Unit,
    onReturnToDock: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(300f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, 500f)
                ),
                RoundedCornerShape(24.dp)
            )
    ) {
        // 装饰性背景
        Box(
            modifier = Modifier
                .size(95.dp)
                .offset(x = 280.dp, y = (-25).dp)
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f),
                    CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(65.dp)
                .offset(x = (-20).dp, y = 90.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                    CircleShape
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "任务管理",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    val statusText = when (robotMode) {
                        RobotMode.IDLE -> "待机中"
                        RobotMode.CLEANING -> "清洁中 ($cleaningProgress%)"
                        RobotMode.CHARGING -> "充电中"
                        RobotMode.RETURNING_TO_DOCK -> "回基站中"
                        RobotMode.ERROR -> "错误状态"
                    }
                    
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (robotMode) {
                            RobotMode.CLEANING -> MaterialTheme.colorScheme.primary
                            RobotMode.ERROR -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                TextButton(onClick = onNavigateToTasks) {
                    Text("详情", style = MaterialTheme.typography.bodySmall)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "查看详情",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            // 显示清洁进度 (仅在清洁模式时)
            if (robotMode == RobotMode.CLEANING) {
                LinearProgressIndicator(
                    progress = { cleaningProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (robotMode) {
                    RobotMode.IDLE -> {
                        Button(
                            onClick = onStartCleaning,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "开始清洁",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("开始清洁", style = MaterialTheme.typography.bodySmall)
                        }
                        
                        OutlinedButton(
                            onClick = onReturnToDock,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "回基站",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("回基站", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    RobotMode.CLEANING -> {
                        Button(
                            onClick = onStopCleaning,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "停止清洁",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("停止清洁", style = MaterialTheme.typography.bodySmall)
                        }
                        
                        OutlinedButton(
                            onClick = onReturnToDock,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "回基站",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("回基站", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onReturnToDock,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = robotMode != RobotMode.CHARGING,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "返回基站",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("返回基站", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
} 