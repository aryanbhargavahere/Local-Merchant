package com.example.local_merchant.viewmodel.buyer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.data.remote.ChatSummary
import com.example.local_merchant.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuyerInboxViewModel(private val repository: MarketplaceRepository) : ViewModel() {
    private val _inboxState = MutableStateFlow<List<ChatSummary>>(emptyList())
    val inboxState: StateFlow<List<ChatSummary>> = _inboxState.asStateFlow()

    init {
        fetchRealInbox()
    }

    private fun fetchRealInbox() {
        viewModelScope.launch {
            repository.getRealInboxSummaries().collect { activeChats ->
                _inboxState.value = activeChats
            }
        }
    }
}
