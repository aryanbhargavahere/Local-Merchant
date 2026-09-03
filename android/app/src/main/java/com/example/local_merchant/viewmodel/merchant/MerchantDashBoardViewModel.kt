package com.example.local_merchant.viewmodel.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.data.model.DashboardStatsResponse
import com.example.local_merchant.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MerchantDashboardViewModel(
    private val repository: MarketplaceRepository
) : ViewModel() {

    // Represents the UI state of the Dashboard
    sealed class DashboardState {
        object Loading : DashboardState()
        data class Success(val stats: DashboardStatsResponse) : DashboardState()
        data class Error(val message: String) : DashboardState()
    }

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    init {
        // 🛑 THE FIX: A Live Heartbeat!
        // This forces the dashboard to pull fresh data every 2 seconds, forever.
        viewModelScope.launch {
            while (true) {
                fetchDashboard()
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    fun fetchDashboard() {
        viewModelScope.launch {
            // 🛑 THE FIX: Only show the loading spinner if we don't already have data!
            if (_dashboardState.value !is DashboardState.Success) {
                _dashboardState.value = DashboardState.Loading
            }

            try {
                val response = repository.getDashboardStats()

                if (response.isSuccessful && response.body() != null) {
                    // This will instantly swap the old numbers for the new numbers without flickering!
                    _dashboardState.value = DashboardState.Success(response.body()!!)
                } else {
                    // Only show an error if we completely failed to get data
                    if (_dashboardState.value !is DashboardState.Success) {
                        _dashboardState.value = DashboardState.Error("Failed to load data (Code: ${response.code()})")
                    }
                }
            } catch (e: Exception) {
                // Keep showing the old dashboard even if the network drops for a second
                if (_dashboardState.value !is DashboardState.Success) {
                    _dashboardState.value = DashboardState.Error("Network Error: Please check your connection.")
                }
            }
        }
    }
}