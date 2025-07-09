package top.minepixel.rdk.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import top.minepixel.rdk.ui.viewmodel.DeviceViewModel

/**
 * 设备管理主页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceManagementScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToSensor: () -> Unit = {},
    viewModel: DeviceViewModel = hiltViewModel()
) {
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // 顶部应用栏
        TopAppBar(
            title = {
                Text(
                    text = "设备管理",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 设备状态概览
                item {
                    DeviceOverviewSection()
                }
                
                // 检测功能区域
                item {
                    DetectionFunctionsSection(
                        onNavigateToCamera = onNavigateToCamera,
                        onNavigateToSensor = onNavigateToSensor
                    )
                }
                
                // 设备控制区域
                item {
                    DeviceControlSection()
                }
                
                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * 设备状态概览区域
 */
@Composable
private fun DeviceOverviewSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "光净精灵状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusItem(
                    icon = Icons.Default.Power,
                    label = "在线状态",
                    value = "正常",
                    color = Color(0xFF4CAF50)
                )
                
                StatusItem(
                    icon = Icons.Default.Battery6Bar,
                    label = "电池电量",
                    value = "85%",
                    color = Color(0xFF2196F3)
                )
                
                StatusItem(
                    icon = Icons.Default.CleaningServices,
                    label = "工作模式",
                    value = "待机",
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

/**
 * 检测功能区域
 */
@Composable
private fun DetectionFunctionsSection(
    onNavigateToCamera: () -> Unit,
    onNavigateToSensor: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "设备检测",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 摄像头检测按钮
                DetectionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Videocam,
                    title = "摄像头检测",
                    description = "检测摄像头状态",
                    onClick = onNavigateToCamera
                )
                
                // 传感器检测按钮
                DetectionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Sensors,
                    title = "传感器检测",
                    description = "检测传感器状态",
                    onClick = onNavigateToSensor
                )
            }
        }
    }
}

/**
 * 设备控制区域
 */
@Composable
private fun DeviceControlSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "设备控制",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 控制按钮网格
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ControlButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.PlayArrow,
                        title = "开始清洁",
                        onClick = { /* TODO: 实现开始清洁 */ }
                    )
                    
                    ControlButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Stop,
                        title = "停止清洁",
                        onClick = { /* TODO: 实现停止清洁 */ }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ControlButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Home,
                        title = "回充",
                        onClick = { /* TODO: 实现回充 */ }
                    )
                    
                    ControlButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocationOn,
                        title = "定位",
                        onClick = { /* TODO: 实现定位 */ }
                    )
                }
            }
        }
    }
}

/**
 * 状态项组件
 */
@Composable
private fun StatusItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * 检测按钮组件
 */
@Composable
private fun DetectionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
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
 * 控制按钮组件
 */
@Composable
private fun ControlButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
