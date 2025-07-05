package top.minepixel.rdk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import top.minepixel.rdk.data.model.CleaningMode
import top.minepixel.rdk.data.model.CleaningTask
import top.minepixel.rdk.data.model.TaskStatus
import top.minepixel.rdk.ui.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }
    
    // 创建背景渐变
    val gradientColors = listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // 顶部导航栏
        TopAppBar(
            title = { Text("清洁任务") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showAddTaskDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加任务"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        )
        
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 任务列表
                if (tasks.isEmpty()) {
                    EmptyTasks()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tasks) { task ->
                            TaskCard(
                                task = task,
                                onDelete = { viewModel.deleteTask(task.id) },
                                onToggleEnabled = { viewModel.toggleTaskEnabled(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 添加任务对话框
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { name, time, room, mode ->
                viewModel.addTask(name, time, room, mode)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun EmptyTasks() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "暂无定时任务",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "点击右上角的 + 按钮添加定时清洁任务",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: CleaningTask,
    onDelete: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 任务状态开关
            Switch(
                checked = task.status == top.minepixel.rdk.data.model.TaskStatus.PENDING,
                onCheckedChange = { onToggleEnabled() }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 任务详情
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 房间
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = task.rooms.firstOrNull() ?: "未指定房间",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 开始时间
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    Text(
                        text = task.startTime?.let { dateFormat.format(Date(it)) } ?: "未指定时间",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 清洁模式
                Text(
                    text = when (task.mode) {
                        CleaningMode.STANDARD -> "标准清洁"
                        CleaningMode.DEEP -> "深度清洁"
                        CleaningMode.QUICK -> "快速清洁"
                        CleaningMode.SPOT -> "定点清洁"
                        CleaningMode.EDGE -> "边缘清洁"
                        else -> "未知模式"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // 删除按钮
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除任务",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (name: String, time: Long, room: String, mode: CleaningMode) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var selectedRoom by remember { mutableStateOf("客厅") }
    var selectedMode by remember { mutableStateOf(CleaningMode.STANDARD) }
    
    // 简化起见，使用当前时间加1小时作为默认开始时间
    val currentTimeMillis = System.currentTimeMillis()
    val oneHourLater = currentTimeMillis + 3600000 // 1小时后
    
    var selectedTime by remember { mutableStateOf(oneHourLater) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "添加定时任务",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 任务名称
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("任务名称") },
                    placeholder = { Text("例如：每日清洁") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 房间选择
                Text(
                    text = "选择房间",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 房间选择(简化版本)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoomChip(
                        name = "客厅",
                        isSelected = selectedRoom == "客厅",
                        onClick = { selectedRoom = "客厅" }
                    )
                    
                    RoomChip(
                        name = "卧室",
                        isSelected = selectedRoom == "卧室",
                        onClick = { selectedRoom = "卧室" }
                    )
                    
                    RoomChip(
                        name = "厨房",
                        isSelected = selectedRoom == "厨房",
                        onClick = { selectedRoom = "厨房" }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 清洁模式选择
                Text(
                    text = "清洁模式",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 清洁模式选择器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeChip(
                        mode = "标准",
                        isSelected = selectedMode == CleaningMode.STANDARD,
                        onClick = { selectedMode = CleaningMode.STANDARD }
                    )
                    
                    ModeChip(
                        mode = "深度",
                        isSelected = selectedMode == CleaningMode.DEEP,
                        onClick = { selectedMode = CleaningMode.DEEP }
                    )
                    
                    ModeChip(
                        mode = "快速",
                        isSelected = selectedMode == CleaningMode.QUICK,
                        onClick = { selectedMode = CleaningMode.QUICK }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (taskName.isNotBlank()) {
                                onAddTask(taskName, selectedTime, selectedRoom, selectedMode)
                            }
                        },
                        enabled = taskName.isNotBlank()
                    ) {
                        Text("添加")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(name) },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeChip(
    mode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(mode) },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null
    )
} 