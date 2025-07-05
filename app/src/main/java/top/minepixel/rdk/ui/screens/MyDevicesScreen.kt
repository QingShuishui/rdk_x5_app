package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class ManagedDevice(
    val id: String,
    val name: String,
    val category: DeviceCategory,
    val isOnline: Boolean,
    val batteryLevel: Int,
    val lastSeen: String,
    val firmwareVersion: String
)

enum class DeviceCategory(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    VACUUM("扫地机器人", Icons.Default.CleaningServices),
    PURIFIER("空气净化器", Icons.Default.Air),
    CAMERA("智能摄像头", Icons.Default.Videocam),
    SPEAKER("智能音箱", Icons.Default.Speaker)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDevicesScreen(onNavigateBack: () -> Unit = {}) {
    var showContent by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf(getSampleManagedDevices()) }
    
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }
    
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
            title = { Text("我的设备") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* 刷新设备列表 */ }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        )
        
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 设备统计卡片
                item {
                    DeviceOverviewCard(devices = devices)
                }
                
                // 设备列表
                items(devices) { device ->
                    ManagedDeviceCard(
                        device = device,
                        onDeviceClick = { /* 进入设备详情 */ },
                        onToggleConnection = { deviceId ->
                            devices = devices.map { 
                                if (it.id == deviceId) it.copy(isOnline = !it.isOnline) 
                                else it 
                            }
                        },
                        onRemoveDevice = { deviceId ->
                            devices = devices.filter { it.id != deviceId }
                        }
                    )
                }
                
                // 添加设备按钮
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* 导航到添加设备页面 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加设备",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "添加新设备",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun DeviceOverviewCard(devices: List<ManagedDevice>) {
    val totalDevices = devices.size
    val onlineDevices = devices.count { it.isOnline }
    val avgBattery = if (devices.isNotEmpty()) devices.map { it.batteryLevel }.average().toInt() else 0
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 300f)
                ),
                RoundedCornerShape(24.dp)
            )
    ) {
        // 装饰性元素
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 280.dp, y = (-20).dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                    CircleShape
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
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
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "设备概览",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem("$totalDevices", "总设备", Icons.Default.Devices)
                StatisticItem("$onlineDevices", "在线", Icons.Default.Wifi)
                StatisticItem("$avgBattery%", "平均电量", Icons.Default.Battery4Bar)
            }
        }
    }
}

@Composable
fun StatisticItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ManagedDeviceCard(
    device: ManagedDevice,
    onDeviceClick: () -> Unit,
    onToggleConnection: (String) -> Unit,
    onRemoveDevice: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 150f)
                ),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = device.category.icon,
                            contentDescription = device.category.displayName,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = device.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多选项"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (device.isOnline) "断开连接" else "连接设备") },
                            onClick = {
                                onToggleConnection(device.id)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (device.isOnline) Icons.Default.WifiOff else Icons.Default.Wifi,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("移除设备") },
                            onClick = {
                                onRemoveDevice(device.id)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 状态指示器
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (device.isOnline) Color.Green else Color.Gray,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (device.isOnline) "在线" else "离线",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (device.isOnline) Color.Green else Color.Gray
                    )
                }
                
                // 电量显示
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when {
                            device.batteryLevel > 75 -> Icons.Default.BatteryFull
                            device.batteryLevel > 50 -> Icons.Default.Battery6Bar
                            device.batteryLevel > 25 -> Icons.Default.Battery3Bar
                            else -> Icons.Default.Battery1Bar
                        },
                        contentDescription = "电量",
                        modifier = Modifier.size(16.dp),
                        tint = when {
                            device.batteryLevel > 50 -> Color.Green
                            device.batteryLevel > 25 -> Color.Yellow
                            else -> Color.Red
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${device.batteryLevel}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "最后活动: ${device.lastSeen}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getSampleManagedDevices(): List<ManagedDevice> {
    return listOf(
        ManagedDevice(
            id = "1",
            name = "客厅扫地机",
            category = DeviceCategory.VACUUM,
            isOnline = true,
            batteryLevel = 85,
            lastSeen = "2分钟前",
            firmwareVersion = "1.2.3"
        )
    )
}