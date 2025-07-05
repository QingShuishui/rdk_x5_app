package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import top.minepixel.rdk.ui.components.MiCard
import top.minepixel.rdk.ui.components.MiClickableCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {}
) {
    var showContent by remember { mutableStateOf(false) }
    
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
            title = { Text("个人资料") },
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
        
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { 40 }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 用户信息卡片
                MiCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 头像
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "头像",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "智能用户",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "admin@example.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStat("3", "已连接设备")
                            ProfileStat("12", "清洁任务")
                            ProfileStat("156", "总使用时长")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 个人信息
                MiCard(
                    title = "个人信息",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileMenuItem(
                        icon = Icons.Default.Edit,
                        title = "编辑资料",
                        subtitle = "修改个人信息",
                        onClick = { /* 编辑资料 */ }
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Security,
                        title = "修改密码",
                        subtitle = "更新登录密码",
                        onClick = { /* 修改密码 */ }
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Smartphone,
                        title = "绑定手机",
                        subtitle = "139****8888",
                        onClick = { /* 绑定手机 */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 使用统计
                MiCard(
                    title = "使用统计",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileMenuItem(
                        icon = Icons.Default.BarChart,
                        title = "使用报告",
                        subtitle = "查看详细统计数据",
                        onClick = { /* 使用报告 */ }
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Schedule,
                        title = "清洁历史",
                        subtitle = "查看清洁记录",
                        onClick = { /* 清洁历史 */ }
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Default.TrendingUp,
                        title = "效率分析",
                        subtitle = "设备性能分析",
                        onClick = { /* 效率分析 */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 其他设置
                MiCard(
                    title = "其他",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileMenuItem(
                        icon = Icons.Default.Share,
                        title = "分享应用",
                        subtitle = "推荐给朋友",
                        onClick = { /* 分享应用 */ }
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Star,
                        title = "评价应用",
                        subtitle = "去应用商店评价",
                        onClick = { /* 评价应用 */ }
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "关于我们",
                        subtitle = "版本 1.0.0",
                        onClick = { /* 关于我们 */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileStat(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "进入",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
} 