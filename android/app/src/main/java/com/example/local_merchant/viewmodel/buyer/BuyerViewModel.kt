package com.example.local_merchant.viewmodel.buyer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.data.local.SessionManager
import com.example.local_merchant.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuyerViewModel(
    private val repository: MarketplaceRepository
) : ViewModel() {

    sealed class SetupState {
        object Idle : SetupState()
        object Loading : SetupState()
        object Success : SetupState()
        data class Error(val message: String) : SetupState()
    }

    private val _setupState = MutableStateFlow<SetupState>(SetupState.Idle)
    val setupState: StateFlow<SetupState> = _setupState.asStateFlow()

    fun register(
        name: String,
        phone: String,
        address: String,
        sessionManager: SessionManager
    ) {
        if (name.isBlank() || phone.isBlank() || address.isBlank()) {
            _setupState.value = SetupState.Error("Please fill out all fields.")
            return
        }

        _setupState.value = SetupState.Loading

        viewModelScope.launch {
            val generatedId = repository.registerBuyer(name, phone, address)

            if (generatedId != null) {
                sessionManager.saveBuyerDetails(
                    id = generatedId,
                    name = name,
                    phone = phone
                )

                _setupState.value = SetupState.Success
            } else {
                _setupState.value = SetupState.Error("Registration failed. Is the Go server running?")
            }
        }
    }

    fun resetState() {
        _setupState.value = SetupState.Idle
    }
}
