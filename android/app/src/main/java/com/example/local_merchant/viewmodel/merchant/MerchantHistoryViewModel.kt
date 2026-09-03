package com.example.local_merchant.viewmodel.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.data.repository.MarketplaceRepository
import com.example.local_merchant.ui.merchant.CompletedJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val jobs: List<CompletedJob>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class MerchantHistoryViewModel(
    private val repository: MarketplaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    // 🛑 THE FIX: We now pass the dynamic ID in when we want to load the data
    fun fetchDealHistory(merchantId: String) {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading

            try {
                repository.getDealHistoryFlow(merchantId).collect { networkJobs ->
                    if (networkJobs.isEmpty()) {
                        _uiState.value = HistoryUiState.Success(emptyList())
                    } else {
                        val uiJobs = networkJobs.map {
                            CompletedJob(
                                jobId = it.jobId,
                                customerName = it.customerName,
                                serviceType = it.serviceType,
                                date = it.date,
                                time = it.time,
                                finalCost = it.finalCost,
                                status = it.status,
                                aiNegotiated = it.aiNegotiated
                            )
                        }
                        _uiState.value = HistoryUiState.Success(uiJobs)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.localizedMessage ?: "Failed to connect to backend")
            }
        }
    }
}