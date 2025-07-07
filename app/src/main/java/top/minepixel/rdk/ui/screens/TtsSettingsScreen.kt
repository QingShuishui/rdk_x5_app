package top.minepixel.rdk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.minepixel.rdk.data.model.TtsConstants
import top.minepixel.rdk.ui.viewmodel.TtsSettingsViewModel

/**
 * TTS设置界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TtsSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("语音合成设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 音色选择
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "音色设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { viewModel.setVoiceType(TtsConstants.VOICE_TYPE_MALE) },
                            label = { Text("男声") },
                            selected = uiState.voiceType == TtsConstants.VOICE_TYPE_MALE,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            onClick = { viewModel.setVoiceType(TtsConstants.VOICE_TYPE_FEMALE) },
                            label = { Text("女声") },
                            selected = uiState.voiceType == TtsConstants.VOICE_TYPE_FEMALE,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // 语速设置
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "语速: ${String.format("%.1f", uiState.speedRatio)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Slider(
                        value = uiState.speedRatio,
                        onValueChange = { viewModel.setSpeedRatio(it) },
                        valueRange = TtsConstants.MIN_SPEED_RATIO..TtsConstants.MAX_SPEED_RATIO,
                        steps = 11
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("慢", style = MaterialTheme.typography.bodySmall)
                        Text("快", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // 音量设置
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "音量: ${String.format("%.1f", uiState.loudnessRatio)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Slider(
                        value = uiState.loudnessRatio,
                        onValueChange = { viewModel.setLoudnessRatio(it) },
                        valueRange = TtsConstants.MIN_LOUDNESS_RATIO..TtsConstants.MAX_LOUDNESS_RATIO,
                        steps = 14
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("小", style = MaterialTheme.typography.bodySmall)
                        Text("大", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // 测试播放
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "测试播放",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Button(
                        onClick = { viewModel.testTts() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isPlaying
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uiState.isPlaying) "播放中..." else "测试播放")
                    }
                }
            }
            
            // 缓存管理
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "缓存管理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedButton(
                        onClick = { viewModel.clearCache() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("清理TTS缓存")
                    }
                }
            }
            
            // 错误信息显示
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "错误信息",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text("清除")
                        }
                    }
                }
            }
        }
    }
}
