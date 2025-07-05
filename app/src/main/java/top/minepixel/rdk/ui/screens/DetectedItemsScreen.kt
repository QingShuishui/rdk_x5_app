package top.minepixel.rdk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.minepixel.rdk.data.model.DetectedItem
import top.minepixel.rdk.data.model.ItemType
import top.minepixel.rdk.ui.viewmodel.DetectedItemsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetectedItemsScreen(
    viewModel: DetectedItemsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val detectedItems by viewModel.detectedItems.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回"
                )
            }
            
            Text(
                text = "检测到的物品",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 物品列表
        if (detectedItems.isEmpty()) {
            EmptyItemsMessage()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(detectedItems) { item ->
                    DetectedItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun EmptyItemsMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "暂无检测到的物品",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "机器人会自动检测并避开清洁区域内的贵重物品",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectedItemCard(item: DetectedItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 格式化时间戳
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(item.timestamp))
            
            val itemTypeIcon = when (item.type) {
                ItemType.JEWELRY -> Icons.Default.Star
                ItemType.EARPHONE -> Icons.Default.Call
                ItemType.KEY -> Icons.Default.Lock
                ItemType.WALLET -> Icons.Default.ShoppingCart
                ItemType.OTHER -> Icons.Default.Info
            }
            
            Card(
                modifier = Modifier.size(64.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // 物品图标
                    Icon(
                        imageVector = itemTypeIcon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (item.type) {
                        ItemType.JEWELRY -> "珠宝首饰"
                        ItemType.EARPHONE -> "耳机"
                        ItemType.KEY -> "钥匙"
                        ItemType.WALLET -> "钱包"
                        ItemType.OTHER -> "其他物品"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "置信度: ${(item.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "检测时间: $formattedDate",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            IconButton(onClick = { /* 标记为已处理 */ }) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "标记为已处理",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 