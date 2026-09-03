package com.example.local_merchant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel(private val repository: MarketplaceRepository) : ViewModel() {

    private val _orderId = MutableStateFlow<String?>(null)
    val orderId: StateFlow<String?> = _orderId.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchOrderId(finalPrice: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            // Dynamically fetch the real order ID from the Go backend
            val id = repository.generateRazorpayOrderId(finalPrice)
            _orderId.value = id
            _isLoading.value = false
        }
    }
}