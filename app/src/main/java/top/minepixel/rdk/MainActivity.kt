package top.minepixel.rdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import top.minepixel.rdk.ui.navigation.RobotNavGraph
import top.minepixel.rdk.ui.theme.RobotCleanerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 启用边缘到边缘显示，支持全面屏
        enableEdgeToEdge()
        
        // 使窗口能够绘制到刘海区域
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            RobotCleanerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    // 移除statusBarsPadding，由各个屏幕自行处理安全区域
                    RobotNavGraph(
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .padding(16.dp)
            .safeDrawingPadding(), // 确保内容在安全区域内
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = uiState.message,
            style = MaterialTheme.typography.headlineLarge
        )
        
        Button(
            onClick = { viewModel.updateMessage("已更新消息!") },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("更新消息")
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "欢迎使用RDK项目",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Button(
                onClick = { },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("更新消息")
            }
        }
    }
} 