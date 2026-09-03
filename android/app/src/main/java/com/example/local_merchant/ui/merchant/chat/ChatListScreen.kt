package com.example.local_merchant.ui.merchant.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.local_merchant.data.model.ChatSummary

@Composable
fun ChatListScreen(
    chatList: List<ChatSummary>,
    onChatClick: (String, String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToProfile: () -> Unit
    // 🛑 History Navigation Parameter Removed
) {
    Scaffold(
        containerColor = Color(0xFF0D0D12),
        bottomBar = {
            // 🛑 Bottom Nav Call Updated
            MerchantBottomNav(
                currentRoute = "chat_inbox",
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToChats = { },
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (chatList.isEmpty()) {
                Text("No active chats", color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(chatList) { chat ->
                        ChatListItem(
                            chat = chat,
                            onClick = {
                                val safeName = chat.name.takeIf { it.isNotBlank() } ?: "Client"
                                onChatClick(chat.id, safeName)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E24)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.initials,
                color = Color(0xFF0EA5E9),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.name.takeIf { it.isNotBlank() } ?: "Client",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.lastMessage,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = chat.time,
                color = if (chat.unreadCount > 0) Color(0xFF0EA5E9) else Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0EA5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 🛑 Updated Bottom Nav (History Removed)
@Composable
fun MerchantBottomNav(
    currentRoute: String,
    onNavigateToDashboard: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF121212),
        contentColor = Color.Gray
    ) {
        NavigationBarItem(
            selected = currentRoute == "merchant_dashboard",
            onClick = onNavigateToDashboard,
            icon = { Icon(Icons.Rounded.ShowChart, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF0EA5E9), selectedTextColor = Color(0xFF0EA5E9), indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentRoute == "chat_inbox",
            onClick = onNavigateToChats,
            icon = { Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = "Chats") },
            label = { Text("Chats") }
        )
        NavigationBarItem(
            selected = currentRoute == "merchant_profile",
            onClick = onNavigateToProfile,
            icon = { Icon(Icons.Rounded.PersonOutline, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}