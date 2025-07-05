package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import top.minepixel.rdk.ui.components.MiCard

// 设备数据类
data class SmartDevice(
    val id: String,
    val name: String,
    val type: DeviceType,
    val isOnline: Boolean,
    val isConnected: Boolean,
    val batteryLevel: Int,
    val lastActivity: String
)

enum class DeviceType {
    VACUUM_ROBOT, AIR_PURIFIER, CAMERA, SPEAKER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceBindingScreen(onNavigateBack: () -> Unit = {}) {
    var showContent by remember { mutableStateOf(false) }
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    
    // 模拟设备列表
    var devices by remember {
        mutableStateOf(
            listOf(
                SmartDevice(
                    id = "robot_001",
                    name = "RDK_X5扫地机器人",
                    type = DeviceType.VACUUM_ROBOT,
                    isOnline = true,
                    isConnected = true,
                    batteryLevel = 85,
                    lastActivity = "2分钟前"
                )
            )
        )
    }
    
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }
    
    // 创建背景渐变
    val gradientColors = listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )
    
    // 添加设备对话框
    if (showAddDeviceDialog) {
        AlertDialog(
            onDismissRequest = { showAddDeviceDialog = false },
            title = { Text("添加设备") },
            text = {
                Column {
                    Text("选择要添加的设备类型：")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isScanning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("正在扫描设备...")
                        }
                    } else {
                        Column {
                            DeviceTypeOption("扫地机器人", Icons.Default.CleaningServices)
                            DeviceTypeOption("空气净化器", Icons.Default.Air)
                            DeviceTypeOption("摄像头", Icons.Default.Videocam)
                            DeviceTypeOption("智能音箱", Icons.Default.Speaker)
                        }
                    }
                }
            },
            confirmButton = {
                if (!isScanning) {
                    TextButton(onClick = {
                        isScanning = true
                    }) {
                        Text("开始扫描")
                    }
                    
                    // 监听扫描状态
                    if (isScanning) {
                        LaunchedEffect(isScanning) {
                            delay(3000)
                            // 添加新设备
                            devices = devices + SmartDevice(
                                id = "new_${System.currentTimeMillis()}",
                                name = "新设备",
                                type = DeviceType.VACUUM_ROBOT,
                                isOnline = true,
                                isConnected = false,
                                batteryLevel = 100,
                                lastActivity = "刚刚发现"
                            )
                            isScanning = false
                            showAddDeviceDialog = false
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDeviceDialog = false
                    isScanning = false
                }) {
                    Text("取消")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // 顶部导航栏
        TopAppBar(
            title = { Text("设备管理") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showAddDeviceDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加设备"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        )
        
        AnimatedVisibility(
            visible = showContent,
            enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 用户统计信息
                item {
                    MiCard(
                        title = "设备概览",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DeviceStatItem(
                                value = devices.size.toString(),
                                label = "总设备",
                                icon = Icons.Default.Devices
                            )
                            DeviceStatItem(
                                value = devices.count { it.isOnline }.toString(),
                                label = "在线设备",
                                icon = Icons.Default.CloudDone
                            )
                            DeviceStatItem(
                                value = devices.count { it.isConnected }.toString(),
                                label = "已连接",
                                icon = Icons.Default.Link
                            )
                        }
                    }
                }
                
                // 快速操作
                item {
                    MiCard(
                        title = "快速操作",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DeviceQuickActionButton(
                                icon = Icons.Default.Add,
                                label = "添加设备",
                                onClick = { showAddDeviceDialog = true }
                            )
                            DeviceQuickActionButton(
                                icon = Icons.Default.Refresh,
                                label = "刷新列表",
                                onClick = { /* 刷新设备列表 */ }
                            )
                            DeviceQuickActionButton(
                                icon = Icons.Default.Settings,
                                label = "网络设置", 
                                onClick = { /* 网络设置 */ }
                            )
                        }
                    }
                }
                
                // 设备列表
                item {
                    Text(
                        text = "我的设备",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                
                items(devices) { device ->
                    DeviceCard(
                        device = device,
                        onConnect = { deviceId ->
                            devices = devices.map { 
                                if (it.id == deviceId) it.copy(isConnected = !it.isConnected)
                                else it
                            }
                        },
                        onRemove = { deviceId ->
                            devices = devices.filter { it.id != deviceId }
                        }
                    )
                }
                
                // 添加底部间距
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DeviceTypeOption(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        onClick = { /* 选择设备类型 */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name)
        }
    }
}

@Composable
fun DeviceStatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DeviceQuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            onClick = onClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DeviceCard(
    device: SmartDevice,
    onConnect: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 设备图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (device.isOnline) 
                                MaterialTheme.colorScheme.primaryContainer
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (device.type) {
                            DeviceType.VACUUM_ROBOT -> Icons.Default.CleaningServices
                            DeviceType.AIR_PURIFIER -> Icons.Default.Air
                            DeviceType.CAMERA -> Icons.Default.Videocam
                            DeviceType.SPEAKER -> Icons.Default.Speaker
                        },
                        contentDescription = device.name,
                        tint = if (device.isOnline) 
                            MaterialTheme.colorScheme.primary
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 设备信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 在线状态指示器
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (device.isOnline) Color.Green else Color.Gray
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (device.isOnline) "在线" else "离线",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        if (device.type == DeviceType.VACUUM_ROBOT && device.isOnline) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.Default.Battery4Bar,
                                contentDescription = "电量",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${device.batteryLevel}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Text(
                        text = "最后活动: ${device.lastActivity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                // 操作按钮
                Column {
                    Button(
                        onClick = { onConnect(device.id) },
                        enabled = device.isOnline,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (device.isConnected) 
                                MaterialTheme.colorScheme.secondary
                            else 
                                MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.width(80.dp)
                    ) {
                        Text(
                            text = if (device.isConnected) "断开" else "连接",
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    OutlinedButton(
                        onClick = { onRemove(device.id) },
                        modifier = Modifier.width(80.dp)
                    ) {
                        Text(
                            text = "移除",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
} 