package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import top.minepixel.rdk.ui.components.MiCard
import top.minepixel.rdk.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var showContent by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }
    
    // 创建背景渐变
    val gradientColors = listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出") },
            text = { Text("您确定要退出登录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
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
    ) {
        // 顶部导航栏
        TopAppBar(
            title = { Text("设置") },
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
                // 账户设置
                MiCard(
                    title = "账户",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsMenuItem(
                        icon = Icons.Default.Person,
                        title = "个人资料",
                        subtitle = "查看和编辑个人信息",
                        onClick = onNavigateToProfile
                    )
                    
                    SettingsMenuItem(
                        icon = Icons.Default.Security,
                        title = "账户安全",
                        subtitle = "密码、验证等安全设置",
                        onClick = { /* 账户安全 */ }
                    )
                    
                    SettingsMenuItem(
                        icon = Icons.Default.Shield,
                        title = "隐私设置",
                        subtitle = "数据隐私和权限管理",
                        onClick = { /* 隐私设置 */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 设备管理
                MiCard(
                    title = "设备管理",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsMenuItem(
                        icon = Icons.Default.Devices,
                        title = "我的设备",
                        subtitle = "管理已连接的智能设备",
                        onClick = { /* 我的设备 */ }
                    )
                    
                    SettingsMenuItem(
                        icon = Icons.Default.Wifi,
                        title = "网络设置",
                        subtitle = "WiFi和网络配置",
                        onClick = { /* 网络设置 */ }
                    )
                    
                    SettingsMenuItem(
                        icon = Icons.Default.Bluetooth,
                        title = "蓝牙设置",
                        subtitle = "蓝牙连接和配对",
                        onClick = { /* 蓝牙设置 */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 应用设置
                MiCard(
                    title = "应用设置",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsMenuItemWithSwitch(
                        icon = Icons.Default.Notifications,
                        title = "推送通知",
                        subtitle = "接收设备状态和任务通知",
                        checked = true,
                        onCheckedChange = { /* 推送通知开关 */ }
                    )
                    
                    SettingsMenuItemWithSwitch(
                        icon = Icons.Default.DarkMode,
                        title = "深色模式",
                        subtitle = "自动或手动切换主题",
                        checked = false,
                        onCheckedChange = { /* 深色模式开关 */ }
                    )
                    
                    SettingsMenuItem(
                        icon = Icons.Default.Language,
                        title = "语言设置",
                        subtitle = "中文（简体）",
                        onClick = { /* 语言设置 */ }
                    )
                    
                    SettingsMenuItem(
                        icon = Icons.Default.Storage,
                        title = "存储管理",
                        subtitle = "清理缓存和数据",
                        onClick = { /* 存储管理 */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 帮助与反馈
                MiCard(
                    title = "帮助与反馈",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsMenuItem(
                        icon = Icons.Default.Help,
                        title = "使用帮助",
                        subtitle = "查看使用指南和常见问题",
                        onClick = { /* 使用帮助 */ }
                    )
                    
                    SettingsMenuItem(
                        icon = Icons.Default.Feedback,
                        title = "意见反馈",
                        subtitle = "提交问题和建议",
                        onClick = { /* 意见反馈 */ }
                    )
                    
                    SettingsMenuItem(
                        icon = Icons.Default.ContactSupport,
                        title = "联系客服",
                        subtitle = "在线客服和技术支持",
                        onClick = { /* 联系客服 */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 关于
                MiCard(
                    title = "关于",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsMenuItem(
                        icon = Icons.Default.Info,
                        title = "关于应用",
                        subtitle = "版本信息和开发团队",
                        onClick = { /* 关于应用 */ }
                    )
                    
                    SettingsMenuItem(
                        icon = Icons.Default.Update,
                        title = "检查更新",
                        subtitle = "当前版本 1.0.0",
                        onClick = { /* 检查更新 */ }
                    )
                    
                    SettingsMenuItem(
                        icon = Icons.Default.Policy,
                        title = "用户协议",
                        subtitle = "服务条款和隐私政策",
                        onClick = { /* 用户协议 */ }
                    )
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
fun SettingsMenuItem(
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

@Composable
fun SettingsMenuItemWithSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
} 