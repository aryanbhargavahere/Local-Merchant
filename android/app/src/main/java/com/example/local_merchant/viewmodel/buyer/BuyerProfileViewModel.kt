package com.example.local_merchant.viewmodel.buyer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.data.local.SessionManager
import com.example.local_merchant.data.repository.MarketplaceRepository
import com.example.local_merchant.ui.buyer.profile.BuyerProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class BuyerProfileViewModel(
    private val repository: MarketplaceRepository, // Kept to avoid breaking your ViewModelFactory
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(BuyerProfileState())
    val state: StateFlow<BuyerProfileState> = _state.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            // Combine both flows from DataStore to update the UI instantly
            sessionManager.buyerNameFlow.combine(sessionManager.buyerPhoneFlow) { name, phone ->
                Pair(name, phone)
            }.collect { (name, phone) ->
                _state.value = _state.value.copy(
                    fullName = name ?: "Guest User",
                    phone = phone ?: "No phone added",
                    location = "Location tracking active",
                    memberSince = "Today"
                )
            }
        }
    }
}