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
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 传感器检测页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDetectionScreen(
    onNavigateBack: () -> Unit = {}
) {
    var showContent by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var sensorData by remember { mutableStateOf(getSampleSensorData()) }
    
    // 模拟实时数据更新
    LaunchedEffect(isScanning) {
        if (isScanning) {
            while (isScanning) {
                delay(1000)
                sensorData = sensorData.map { sensor ->
                    sensor.copy(
                        value = when (sensor.type) {
                            SensorType.TEMPERATURE -> "${Random.nextInt(20, 28)}°C"
                            SensorType.HUMIDITY -> "${Random.nextInt(40, 70)}%"
                            SensorType.DUST -> "${Random.nextInt(10, 50)} μg/m³"
                            SensorType.DISTANCE -> "${Random.nextInt(5, 200)} cm"
                            SensorType.GYROSCOPE -> "正常"
                            SensorType.ACCELEROMETER -> "稳定"
                        },
                        status = if (Random.nextBoolean()) SensorStatus.NORMAL else SensorStatus.WARNING
                    )
                }
            }
        }
    }
    
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
                    text = "传感器检测",
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
                // 扫描控制区域
                item {
                    ScanControlCard(
                        isScanning = isScanning,
                        onStartScan = { isScanning = true },
                        onStopScan = { isScanning = false }
                    )
                }
                
                // 传感器状态概览
                item {
                    SensorOverviewCard(sensors = sensorData)
                }
                
                // 传感器详细信息
                sensorData.forEach { sensor ->
                    item {
                        SensorDetailCard(sensor = sensor)
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

/**
 * 扫描控制卡片
 */
@Composable
private fun ScanControlCard(
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = "传感器",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "传感器扫描",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isScanning) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "正在扫描传感器...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = onStopScan,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "停止扫描",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("停止扫描")
                    }
                }
            } else {
                Button(
                    onClick = onStartScan,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "开始扫描",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始扫描")
                }
            }
        }
    }
}

/**
 * 传感器概览卡片
 */
@Composable
private fun SensorOverviewCard(sensors: List<SensorData>) {
    val normalCount = sensors.count { it.status == SensorStatus.NORMAL }
    val warningCount = sensors.count { it.status == SensorStatus.WARNING }
    val errorCount = sensors.count { it.status == SensorStatus.ERROR }
    
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
                text = "传感器状态概览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusCounter(
                    icon = Icons.Default.CheckCircle,
                    label = "正常",
                    count = normalCount,
                    color = Color(0xFF4CAF50)
                )
                
                StatusCounter(
                    icon = Icons.Default.Warning,
                    label = "警告",
                    count = warningCount,
                    color = Color(0xFFFF9800)
                )
                
                StatusCounter(
                    icon = Icons.Default.Error,
                    label = "错误",
                    count = errorCount,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

/**
 * 传感器详细信息卡片
 */
@Composable
private fun SensorDetailCard(sensor: SensorData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 传感器图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sensor.status.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = sensor.icon,
                    contentDescription = sensor.name,
                    tint = sensor.status.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 传感器信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sensor.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = sensor.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 传感器数值和状态
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = sensor.value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = sensor.status.color
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = sensor.status.icon,
                        contentDescription = sensor.status.name,
                        tint = sensor.status.color,
                        modifier = Modifier.size(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = sensor.status.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = sensor.status.color
                    )
                }
            }
        }
    }
}

/**
 * 状态计数器组件
 */
@Composable
private fun StatusCounter(
    icon: ImageVector,
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 传感器类型枚举
 */
enum class SensorType {
    TEMPERATURE, HUMIDITY, DUST, DISTANCE, GYROSCOPE, ACCELEROMETER
}

/**
 * 传感器状态枚举
 */
enum class SensorStatus(
    val displayName: String,
    val color: Color,
    val icon: ImageVector
) {
    NORMAL("正常", Color(0xFF4CAF50), Icons.Default.CheckCircle),
    WARNING("警告", Color(0xFFFF9800), Icons.Default.Warning),
    ERROR("错误", Color(0xFFF44336), Icons.Default.Error)
}

/**
 * 传感器数据类
 */
data class SensorData(
    val type: SensorType,
    val name: String,
    val description: String,
    val value: String,
    val status: SensorStatus,
    val icon: ImageVector
)

/**
 * 获取示例传感器数据
 */
private fun getSampleSensorData(): List<SensorData> {
    return listOf(
        SensorData(
            type = SensorType.TEMPERATURE,
            name = "温度传感器",
            description = "环境温度监测",
            value = "24°C",
            status = SensorStatus.NORMAL,
            icon = Icons.Default.Thermostat
        ),
        SensorData(
            type = SensorType.HUMIDITY,
            name = "湿度传感器",
            description = "环境湿度监测",
            value = "55%",
            status = SensorStatus.NORMAL,
            icon = Icons.Default.WaterDrop
        ),
        SensorData(
            type = SensorType.DUST,
            name = "灰尘传感器",
            description = "空气质量监测",
            value = "25 μg/m³",
            status = SensorStatus.WARNING,
            icon = Icons.Default.Air
        ),
        SensorData(
            type = SensorType.DISTANCE,
            name = "距离传感器",
            description = "障碍物检测",
            value = "120 cm",
            status = SensorStatus.NORMAL,
            icon = Icons.Default.Straighten
        ),
        SensorData(
            type = SensorType.GYROSCOPE,
            name = "陀螺仪",
            description = "姿态检测",
            value = "正常",
            status = SensorStatus.NORMAL,
            icon = Icons.Default.RotateRight
        ),
        SensorData(
            type = SensorType.ACCELEROMETER,
            name = "加速度计",
            description = "运动检测",
            value = "稳定",
            status = SensorStatus.NORMAL,
            icon = Icons.Default.Speed
        )
    )
}
