package com.example.local_merchant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.config.AppConfig
import com.example.local_merchant.data.local.ChatEntity
import com.example.local_merchant.data.model.ChatSummary
import com.example.local_merchant.data.repository.MarketplaceRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class ChatViewModel(
    private val repository: MarketplaceRepository,
    private val currentUserId: String,
    private val okHttpClient: OkHttpClient = OkHttpClient()
) : ViewModel() {

    private val _inboxState = MutableStateFlow<List<ChatSummary>>(emptyList())
    val inboxState: StateFlow<List<ChatSummary>> = _inboxState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatEntity>>(emptyList())
    val messages: StateFlow<List<ChatEntity>> = _messages.asStateFlow()

    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private var chatJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getRealInboxSummaries().collect { remoteSummaries ->
                _inboxState.value = remoteSummaries.map { remote ->
                    val displayName = remote.name.takeIf { it.isNotBlank() } ?: "Client"

                    ChatSummary(
                        id = remote.id,
                        name = displayName,
                        lastMessage = remote.lastMessage,
                        time = remote.time,
                        unreadCount = remote.unreadCount,
                        initials = displayName.take(1).uppercase()
                    )
                }
            }
        }
    }

    fun connectWebSocket(conversationId: String) {
        chatJob?.cancel()
        _messages.value = emptyList()

        chatJob = viewModelScope.launch {
            while (true) {
                val refreshJob = launch {
                    repository.getChatHistory(conversationId).collect { history ->
                        _messages.value = history
                    }
                }
                delay(2000)
                refreshJob.cancel()
            }
        }

        val url = "${AppConfig.WS_URL}ws/chat?conversation_id=$conversationId&user_id=$currentUserId"
        val request = Request.Builder().url(url).build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val incomingMsg = gson.fromJson(text, ChatEntity::class.java)
                        incomingMsg.timestamp = System.currentTimeMillis()
                        if (incomingMsg.merchantId == conversationId) {
                            repository.saveIncomingMessage(incomingMsg)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
            }
        })
    }

    fun disconnectWebSocket() {
        chatJob?.cancel()
        webSocket?.close(1000, "User left chat screen")
        webSocket = null
    }

    fun sendChatMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        val outgoingMsg = ChatEntity(
            merchantId = conversationId,
            sender = currentUserId,
            message = text,
            timestamp = System.currentTimeMillis()
        )
        val jsonPayload = gson.toJson(outgoingMsg)
        webSocket?.send(jsonPayload)

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveIncomingMessage(outgoingMsg)
        }
    }

    override fun onCleared() {
        disconnectWebSocket()
    }
}
