package com.example.local_merchant.viewmodel.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.data.local.SessionManager
import com.example.local_merchant.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MerchantViewModel(
    private val repository: MarketplaceRepository
) : ViewModel() {

    // Define the possible states for the UI to observe
    sealed class SetupState {
        object Idle : SetupState()
        object Loading : SetupState()
        object Success : SetupState()
        data class Error(val message: String) : SetupState()
    }

    private val _setupState = MutableStateFlow<SetupState>(SetupState.Idle)
    val setupState: StateFlow<SetupState> = _setupState.asStateFlow()

    fun deployAgent(
        name: String,
        service: String,
        phone: String,
        baseRate: Int,
        floorRate: Int,
        sessionManager: SessionManager // <-- Passed in from the Composable
    ) {
        // 1. Prevent bad data from hitting your Go server
        if (name.isBlank() || service.isBlank() || phone.isBlank()) {
            _setupState.value = SetupState.Error("Please fill out all fields.")
            return
        }

        if (floorRate >= baseRate) {
            _setupState.value = SetupState.Error("Walk-away limit must be lower than the base rate.")
            return
        }

        // 2. Trigger loading spinner on the UI
        _setupState.value = SetupState.Loading

        // 3. Launch background coroutine for network request
        viewModelScope.launch {
            // Note: This now returns a String? (the generated ID) instead of a Boolean
            val generatedId = repository.registerMerchant(
                name = name,
                service = service,
                phone = phone,
                baseRate = baseRate,
                floorRate = floorRate
            )

            if (generatedId != null) {
                // Save the real ID to the device via DataStore!
                sessionManager.saveMerchantDetails(
                    id = generatedId,
                    name = name,
                    phone = phone
                )

                // Now we can tell the UI it's safe to navigate
                _setupState.value = SetupState.Success
            } else {
                _setupState.value = SetupState.Error("Failed to deploy AI Agent. Is the Go server running?")
            }
        }
    }

    // Call this after a successful navigation to reset the screen
    fun resetState() {
        _setupState.value = SetupState.Idle
    }
}