package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import top.minepixel.rdk.ui.viewmodel.DeviceViewModel
import top.minepixel.rdk.ui.components.MiCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCenterScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMyDevices: () -> Unit = {},
    onNavigateToAddDevice: () -> Unit = {},
    onNavigateToAboutApp: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    onLogout: () -> Unit = {},
    deviceViewModel: DeviceViewModel = hiltViewModel()
) {
    var showContent by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // 获取设备统计
    val connectedDevices by deviceViewModel.connectedDevices.collectAsState()
    
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }
    
    // 创建背景渐变
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.surface
    )
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出") },
            text = { Text("您确定要退出登录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("确认", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .safeDrawingPadding()
    ) {
        // 顶部用户信息区域
        AnimatedVisibility(
            visible = showContent,
            enter = slideInVertically(initialOffsetY = { -50 }) + fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1200f, 600f)
                        ),
                        RoundedCornerShape(32.dp)
                    )
            ) {
                // 装饰性元素
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .offset(x = 280.dp, y = (-40).dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                            CircleShape
                        )
                )
                
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .offset(x = (-30).dp, y = 80.dp)
                        .background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                            CircleShape
                        )
                )
                
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = 320.dp, y = 150.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.06f),
                            CircleShape
                        )
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 用户头像
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "用户头像",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "智能用户",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "admin@smartrobot.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // 用户统计信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        UserStatItem("$connectedDevices", "已连接设备")
                        UserStatItem("25", "完成任务")
                        UserStatItem("156h", "总使用时长")
                    }
                }
            }
        }
        
        // 功能菜单区域
        AnimatedVisibility(
            visible = showContent,
            enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // 个人设置
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = "个人设置",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            UserMenuItem(
                                icon = Icons.Default.Person,
                                title = "个人资料",
                                subtitle = "查看和编辑个人信息",
                                onClick = onNavigateToProfile
                            )
                            
                            UserMenuItem(
                                icon = Icons.Default.Settings,
                                title = "应用设置",
                                subtitle = "通知、主题等设置",
                                onClick = onNavigateToSettings
                            )
                            
                            UserMenuItem(
                                icon = Icons.Default.Security,
                                title = "安全设置",
                                subtitle = "密码、隐私等设置",
                                onClick = { /* 安全设置 */ }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 帮助与支持
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Help,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = "帮助与支持",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            UserMenuItem(
                                icon = Icons.Default.Help,
                                title = "使用帮助",
                                subtitle = "查看使用指南",
                                onClick = { /* 使用帮助 */ }
                            )
                            
                            UserMenuItem(
                                icon = Icons.Default.Feedback,
                                title = "意见反馈",
                                subtitle = "提交建议和问题",
                                onClick = onNavigateToFeedback
                            )
                            
                            UserMenuItem(
                                icon = Icons.Default.Info,
                                title = "关于应用",
                                subtitle = "版本信息和更新",
                                onClick = onNavigateToAboutApp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 退出登录按钮
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "退出登录",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "退出登录",
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun UserStatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
fun UserMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(300f, 80f)
                    ),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(18.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "进入",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
} 