package com.example.local_merchant.viewmodel.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.data.model.DashboardStatsResponse
import com.example.local_merchant.data.repository.MarketplaceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MerchantDashboardViewModel(
    private val repository: MarketplaceRepository
) : ViewModel() {

    sealed class DashboardState {
        object Loading : DashboardState()
        data class Success(val stats: DashboardStatsResponse) : DashboardState()
        data class Error(val message: String) : DashboardState()
    }

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                fetchDashboard()
                delay(2000)
            }
        }
    }

    fun fetchDashboard() {
        viewModelScope.launch {
            if (_dashboardState.value !is DashboardState.Success) {
                _dashboardState.value = DashboardState.Loading
            }

            try {
                val response = repository.getDashboardStats()

                if (response.isSuccessful && response.body() != null) {
                    _dashboardState.value = DashboardState.Success(response.body()!!)
                } else {
                    if (_dashboardState.value !is DashboardState.Success) {
                        _dashboardState.value = DashboardState.Error("Failed to load data (Code: ${response.code()})")
                    }
                }
            } catch (e: Exception) {
                if (_dashboardState.value !is DashboardState.Success) {
                    _dashboardState.value = DashboardState.Error("Network Error: Please check your connection.")
                }
            }
        }
    }
}
