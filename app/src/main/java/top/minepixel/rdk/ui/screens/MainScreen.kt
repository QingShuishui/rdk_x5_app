package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import top.minepixel.rdk.ui.navigation.RobotDestinations

/**
 * 底部导航项
 */
sealed class BottomNavItem(val route: String, val icon: @Composable () -> Unit, val label: String) {
    object Home : BottomNavItem(
        route = RobotDestinations.HOME_ROUTE,
        icon = { Icon(Icons.Default.Home, contentDescription = "首页", modifier = Modifier.padding(bottom = 2.dp)) },
        label = "首页"
    )
    object Voice : BottomNavItem(
        route = RobotDestinations.VOICE_ROUTE,
        icon = { Icon(Icons.Default.Mic, contentDescription = "语音", modifier = Modifier.padding(bottom = 2.dp)) },
        label = "语音"
    )
    object Tasks : BottomNavItem(
        route = RobotDestinations.TASKS_ROUTE,
        icon = { Icon(Icons.Default.Task, contentDescription = "任务", modifier = Modifier.padding(bottom = 2.dp)) },
        label = "任务"
    )
    object Device : BottomNavItem(
        route = RobotDestinations.USER_CENTER_ROUTE,
        icon = { Icon(Icons.Default.Person, contentDescription = "用户", modifier = Modifier.padding(bottom = 2.dp)) },
        label = "用户"
    )
}

/**
 * 认证相关页面路由，这些页面不显示底部导航栏
 */
private val authRoutes = setOf(
    RobotDestinations.SPLASH_ROUTE,
    RobotDestinations.LOGIN_ROUTE,
    RobotDestinations.REGISTER_ROUTE,
    RobotDestinations.FORGOT_PASSWORD_ROUTE,
    RobotDestinations.RESET_PASSWORD_ROUTE
)

/**
 * 主屏幕，包含底部导航和内容
 */
@Composable
fun MainScreen(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    // 底部导航项列表
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Voice,
        BottomNavItem.Tasks,
        BottomNavItem.Device
    )
    
    // 获取当前导航位置
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // 判断是否应该显示底部导航栏
    // 认证相关页面不显示底部导航栏
    val shouldShowBottomBar = currentDestination?.route !in authRoutes
    
    Scaffold(
        // 移除系统导航栏的内边距，让导航栏可以延伸到底部
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // 添加动画效果的底部导航栏
            AnimatedVisibility(
                visible = shouldShowBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it }
                )
            ) {
                NavigationBar(
                    modifier = Modifier.shadow(4.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    // 确保考虑系统导航栏的高度
                    windowInsets = WindowInsets.navigationBars
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                // 如果当前不在该项对应的路由，则导航到该路由
                                if (currentDestination?.route != item.route) {
                                    navController.navigate(item.route) {
                                        // 弹出到起始目的地，避免堆栈中有大量重复目的地
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // 如果已存在，不重新创建
                                        launchSingleTop = true
                                        // 恢复状态
                                        restoreState = true
                                    }
                                }
                            },
                            icon = item.icon,
                            label = { Text(item.label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // 内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
} 