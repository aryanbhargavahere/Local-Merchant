package com.example.local_merchant.viewmodel.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.data.repository.MarketplaceRepository
import com.example.local_merchant.ui.merchant.MerchantProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MerchantProfileViewModel(
    private val repository: MarketplaceRepository,
    private val merchantId: String
) : ViewModel() {

    private val _state = MutableStateFlow(MerchantProfileState(isLoading = true))
    val state: StateFlow<MerchantProfileState> = _state.asStateFlow()

    init {
        loadDynamicProfile()
    }

    private fun loadDynamicProfile() {
        viewModelScope.launch {
            repository.getDynamicMerchantProfile(merchantId).collect { entity ->
                if (entity != null) {
                    _state.value = MerchantProfileState(
                        isLoading = false,
                        name = entity.name,
                        service = entity.service,
                        phone = entity.phone,
                        rating = entity.rating,
                        jobsCompleted = entity.jobsCompleted,
                        isBiometricsEnabled = entity.isBiometricsEnabled
                    )
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isBiometricsEnabled = enabled) }
            repository.updateBiometricPreference(merchantId, enabled)
        }
    }
}
