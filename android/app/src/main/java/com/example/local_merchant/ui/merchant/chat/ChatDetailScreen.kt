package com.example.local_merchant.ui.merchant.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.local_merchant.data.local.ChatEntity
import com.example.local_merchant.ui.components.Background
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    merchantName: String,
    messages: List<ChatEntity>,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Background {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF272730)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = merchantName.take(1).uppercase(),
                                    color = Color(0xFF0EA5E9),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(merchantName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E24))
                )
            },
            bottomBar = {
                ChatInputBar(
                    text = messageText,
                    onTextChange = { messageText = it },
                    onSend = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages.reversed()) { msg ->
                    MessageBubble(message = msg)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatEntity) {
    val isFromMe = message.sender.equals("MERCHANT", ignoreCase = true)
    val bubbleColor = if (isFromMe) Color(0xFF0EA5E9) else Color(0xFF1E1E24)
    val alignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isFromMe) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    val timeString = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(text = message.message, color = Color.White, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeString,
                color = if (isFromMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Message...", color = Color.Gray) },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E24),
                unfocusedContainerColor = Color(0xFF1E1E24),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF0EA5E9),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF0EA5E9))
                .clickable(onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = Color.White)
        }
    }
}
