package com.example.local_merchant.ui.Buyer.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.local_merchant.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegotiationChatScreen(
    viewModel: ChatViewModel,
    merchantId: String,
    merchantName: String,
    onDealAccepted: (Int) -> Unit
) {
    val messages by viewModel.messages.collectAsState(initial = emptyList())
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(merchantName) },
                actions = {
                    TextButton(onClick = {
                        val lastMerchantMsg = messages.lastOrNull { it.sender == "MERCHANT" }?.message ?: ""

                        val regex = Regex("₹(\\d+)")
                        val match = regex.find(lastMerchantMsg)
                        val finalPrice = match?.groupValues?.get(1)?.toIntOrNull() ?: 0

                        if (finalPrice > 0) {
                            com.example.local_merchant.ActiveCheckoutState.merchantId = merchantId
                            com.example.local_merchant.ActiveCheckoutState.amount = finalPrice

                            onDealAccepted(finalPrice)
                        } else {
                            Toast.makeText(context, "Please agree on a price first!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Accept Deal", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept Deal")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        viewModel.sendChatMessage(
                            conversationId = merchantId,
                            text = messageText
                        )
                        messageText = ""
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                val isMyMessage = msg.sender != "MERCHANT"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isMyMessage) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.message,
                            color = if (isMyMessage) MaterialTheme.colorScheme.onPrimary else Color.Black
                        )
                    }
                }
            }
        }
    }
}