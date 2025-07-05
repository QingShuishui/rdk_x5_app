package top.minepixel.rdk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import top.minepixel.rdk.ui.screens.*

/**
 * 导航路由
 */
object RobotDestinations {
    const val SPLASH_ROUTE = "splash"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val FORGOT_PASSWORD_ROUTE = "forgot_password"
    const val RESET_PASSWORD_ROUTE = "reset_password"
    const val HOME_ROUTE = "home"
    const val DETECTED_ITEMS_ROUTE = "detected_items"
    const val TASKS_ROUTE = "tasks"
    const val VOICE_ROUTE = "voice"
    const val DEVICE_BIND_ROUTE = "device_bind"
    const val USER_CENTER_ROUTE = "user_center"
    const val PROFILE_ROUTE = "profile"
    const val SETTINGS_ROUTE = "settings"
    
    // 设备管理相关
    const val MY_DEVICES_ROUTE = "my_devices"
    const val ADD_DEVICE_ROUTE = "add_device"
    const val NETWORK_SETTINGS_ROUTE = "network_settings"
    
    // 任务管理相关
    const val CLEAN_TASKS_ROUTE = "clean_tasks"
    const val SCHEDULED_TASKS_ROUTE = "scheduled_tasks"
    const val TASK_HISTORY_ROUTE = "task_history"
    
    // 设置相关
    const val SECURITY_SETTINGS_ROUTE = "security_settings"
    const val HELP_GUIDE_ROUTE = "help_guide"
    const val FEEDBACK_ROUTE = "feedback"
    const val ABOUT_APP_ROUTE = "about_app"
}

/**
 * 主导航图
 */
@Composable
fun RobotNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = RobotDestinations.SPLASH_ROUTE,
    modifier: Modifier = Modifier
) {
    // 使用MainScreen作为容器，实现全局底部导航栏
    MainScreen(navController = navController) {
        NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
            // 启动画面
            composable(RobotDestinations.SPLASH_ROUTE) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(RobotDestinations.LOGIN_ROUTE) {
                            popUpTo(RobotDestinations.SPLASH_ROUTE) { inclusive = true }
                        }
                    },
                    onAutoLogin = {
                        navController.navigate(RobotDestinations.HOME_ROUTE) {
                            popUpTo(RobotDestinations.SPLASH_ROUTE) { inclusive = true }
                        }
                    }
                )
            }
            
            // 认证相关页面
            composable(RobotDestinations.LOGIN_ROUTE) {
                LoginScreen(
                    onLoginSuccess = { 
                        navController.navigate(RobotDestinations.HOME_ROUTE) {
                            popUpTo(RobotDestinations.LOGIN_ROUTE) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(RobotDestinations.REGISTER_ROUTE)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(RobotDestinations.FORGOT_PASSWORD_ROUTE)
                    }
                )
            }
            
            composable(RobotDestinations.REGISTER_ROUTE) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(RobotDestinations.HOME_ROUTE) {
                            popUpTo(RobotDestinations.LOGIN_ROUTE) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(RobotDestinations.FORGOT_PASSWORD_ROUTE) {
                ForgotPasswordScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToResetPassword = {
                        navController.navigate(RobotDestinations.RESET_PASSWORD_ROUTE)
                    }
                )
            }
            
            composable(RobotDestinations.RESET_PASSWORD_ROUTE) {
                ResetPasswordScreen(
                    onResetSuccess = {
                        navController.navigate(RobotDestinations.LOGIN_ROUTE) {
                            popUpTo(RobotDestinations.LOGIN_ROUTE) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 主要功能页面
            composable(RobotDestinations.HOME_ROUTE) {
                HomeScreen(
                    onNavigateToMyDevices = {
                        navController.navigate(RobotDestinations.MY_DEVICES_ROUTE)
                    },
                    onNavigateToAddDevice = {
                        navController.navigate(RobotDestinations.ADD_DEVICE_ROUTE)
                    },
                    onNavigateToTasks = {
                        navController.navigate(RobotDestinations.TASKS_ROUTE)
                    },
                    onNavigateToVoice = {
                        navController.navigate(RobotDestinations.VOICE_ROUTE)
                    }
                )
            }

            composable(RobotDestinations.DETECTED_ITEMS_ROUTE) {
                DetectedItemsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(RobotDestinations.TASKS_ROUTE) {
                TasksScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(RobotDestinations.VOICE_ROUTE) {
                VoiceControlScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(RobotDestinations.DEVICE_BIND_ROUTE) {
                DeviceBindingScreen(onNavigateBack = { navController.popBackStack() })
            }
            
            // 用户中心页面
            composable(RobotDestinations.USER_CENTER_ROUTE) {
                UserCenterScreen(
                    onNavigateToProfile = {
                        navController.navigate(RobotDestinations.PROFILE_ROUTE)
                    },
                    onNavigateToSettings = {
                        navController.navigate(RobotDestinations.SETTINGS_ROUTE)
                    },
                    onNavigateToMyDevices = {
                        navController.navigate(RobotDestinations.MY_DEVICES_ROUTE)
                    },
                    onNavigateToAddDevice = {
                        navController.navigate(RobotDestinations.ADD_DEVICE_ROUTE)
                    },
                    onNavigateToAboutApp = {
                        navController.navigate(RobotDestinations.ABOUT_APP_ROUTE)
                    },
                    onNavigateToFeedback = {
                        navController.navigate(RobotDestinations.FEEDBACK_ROUTE)
                    },
                    onLogout = {
                        navController.navigate(RobotDestinations.LOGIN_ROUTE) {
                            popUpTo(0) // 清空所有返回栈
                        }
                    }
                )
            }
            
            // 用户相关页面
            composable(RobotDestinations.PROFILE_ROUTE) {
                ProfileScreen(onNavigateBack = { navController.popBackStack() })
            }
            
            composable(RobotDestinations.SETTINGS_ROUTE) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProfile = {
                        navController.navigate(RobotDestinations.PROFILE_ROUTE)
                    },
                    onLogout = {
                        navController.navigate(RobotDestinations.LOGIN_ROUTE) {
                            popUpTo(0) // 清空所有返回栈
                        }
                    }
                )
            }
            
            // 设备管理相关页面
            composable(RobotDestinations.MY_DEVICES_ROUTE) {
                MyDevicesScreen(onNavigateBack = { navController.popBackStack() })
            }
            
            composable(RobotDestinations.ADD_DEVICE_ROUTE) {
                AddDeviceScreen(onNavigateBack = { navController.popBackStack() })
            }
            
            // 关于应用页面
            composable(RobotDestinations.ABOUT_APP_ROUTE) {
                AboutAppScreen(onNavigateBack = { navController.popBackStack() })
            }
            
            // 意见反馈页面
            composable(RobotDestinations.FEEDBACK_ROUTE) {
                FeedbackScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
} 