package top.minepixel.rdk.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import top.minepixel.rdk.ui.viewmodel.CameraViewModel

/**
 * 摄像头检测页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetectionScreen(
    onNavigateBack: () -> Unit = {},
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    var showContent by remember { mutableStateOf(false) }
    var detectionResults by remember { mutableStateOf<List<CameraDetectionResult>>(emptyList()) }

    // 收集摄像头状态
    val isCameraActive by cameraViewModel.isCameraActive.collectAsState()
    val cameraInfo by cameraViewModel.cameraInfo.collectAsState()
    val isLoading by cameraViewModel.isLoading.collectAsState()
    val errorMessage by cameraViewModel.errorMessage.collectAsState()
    
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }

    // 处理摄像头状态变化
    LaunchedEffect(isCameraActive) {
        if (isCameraActive) {
            // 模拟检测延迟
            delay(2000)
            detectionResults = getSampleDetectionResults()
        } else {
            detectionResults = emptyList()
        }
    }

    // 处理错误消息
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // TODO: 可以在这里显示错误提示
            // 例如：showSnackbar(it)
        }
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
                    text = "摄像头检测",
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
                // 摄像头状态卡片
                item {
                    CameraStatusCard(
                        isCameraActive = isCameraActive,
                        cameraInfo = cameraInfo
                    )
                }

                // 摄像头预览区域
                item {
                    CameraPreviewCard(
                        isCameraActive = isCameraActive,
                        isLoading = isLoading,
                        onStartCamera = {
                            cameraViewModel.startCamera()
                        },
                        onStopCamera = {
                            cameraViewModel.stopCamera()
                        },
                        onCapturePhoto = {
                            cameraViewModel.capturePhoto()
                        }
                    )
                }
                
                // 检测结果
                if (detectionResults.isNotEmpty()) {
                    item {
                        DetectionResultsCard(results = detectionResults)
                    }
                }
                
                // 摄像头设置
                item {
                    CameraSettingsCard()
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
 * 摄像头状态卡片
 */
@Composable
private fun CameraStatusCard(
    isCameraActive: Boolean,
    cameraInfo: top.minepixel.rdk.data.manager.CameraInfo
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "摄像头",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "摄像头状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusIndicator(
                    icon = if (isCameraActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    label = "摄像头状态",
                    value = if (isCameraActive) "运行中" else "已停止",
                    color = if (isCameraActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                )

                StatusIndicator(
                    icon = Icons.Default.HighQuality,
                    label = "分辨率",
                    value = cameraInfo.resolution,
                    color = Color(0xFF2196F3)
                )

                StatusIndicator(
                    icon = Icons.Default.Speed,
                    label = "帧率",
                    value = cameraInfo.frameRate,
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

/**
 * 摄像头预览卡片
 */
@Composable
private fun CameraPreviewCard(
    isCameraActive: Boolean,
    isLoading: Boolean,
    onStartCamera: () -> Unit,
    onStopCamera: () -> Unit,
    onCapturePhoto: () -> Unit
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
            Text(
                text = "实时摄像头",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isCameraActive) {
                // 摄像头预览区域
                Column {
                    // 摄像头画面容器
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // TODO: 这里将集成实际的摄像头预览
                        CameraPreviewPlaceholder()

                        // 录制指示器
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(
                                    Color.Red.copy(alpha = 0.8f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 控制按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onStopCamera,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "停止摄像头",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("停止摄像头")
                        }

                        Button(
                            onClick = onCapturePhoto,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "拍照",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("拍照")
                        }
                    }
                }
            } else {
                // 启动摄像头按钮
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "摄像头",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "点击开启实时摄像头",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onStartCamera,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "开启摄像头",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isLoading) "启动中..." else "开启摄像头")
                    }
                }
            }
        }
    }
}

/**
 * 检测结果卡片
 */
@Composable
private fun DetectionResultsCard(results: List<CameraDetectionResult>) {
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
                text = "检测结果 (${results.size}个物体)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            results.forEach { result ->
                DetectionResultItem(result = result)
                if (result != results.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * 摄像头设置卡片
 */
@Composable
private fun CameraSettingsCard() {
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
                text = "摄像头设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 设置选项
            SettingItem(
                icon = Icons.Default.Brightness6,
                title = "亮度调节",
                description = "自动"
            )
            
            SettingItem(
                icon = Icons.Default.Contrast,
                title = "对比度",
                description = "标准"
            )
            
            SettingItem(
                icon = Icons.Default.CenterFocusStrong,
                title = "自动对焦",
                description = "开启"
            )
        }
    }
}

/**
 * 状态指示器组件
 */
@Composable
private fun StatusIndicator(
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
 * 检测结果项组件
 */
@Composable
private fun DetectionResultItem(result: CameraDetectionResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = result.icon,
            contentDescription = result.objectType,
            tint = result.color,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.objectType,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "置信度: ${(result.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = result.position,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 设置项组件
 */
@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 摄像头检测结果数据类
 */
data class CameraDetectionResult(
    val objectType: String,
    val confidence: Float,
    val position: String,
    val icon: ImageVector,
    val color: Color
)

/**
 * 摄像头预览占位符
 */
@Composable
private fun CameraPreviewPlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // 模拟摄像头画面的网格
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Color.Gray.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "摄像头预览",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "实时画面",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "摄像头API接口待集成",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

/**
 * 获取示例检测结果
 */
private fun getSampleDetectionResults(): List<CameraDetectionResult> {
    return listOf(
        CameraDetectionResult(
            objectType = "钥匙",
            confidence = 0.95f,
            position = "左上角",
            icon = Icons.Default.Key,
            color = Color(0xFFFF9800)
        ),
        CameraDetectionResult(
            objectType = "耳机",
            confidence = 0.87f,
            position = "中央",
            icon = Icons.Default.Headphones,
            color = Color(0xFF2196F3)
        ),
        CameraDetectionResult(
            objectType = "钱包",
            confidence = 0.92f,
            position = "右下角",
            icon = Icons.Default.AccountBalanceWallet,
            color = Color(0xFF4CAF50)
        )
    )
}
