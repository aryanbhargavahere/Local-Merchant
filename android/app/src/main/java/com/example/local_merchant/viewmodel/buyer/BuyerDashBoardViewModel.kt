package com.example.local_merchant.viewmodel.buyer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local_merchant.data.remote.MerchantData
import com.example.local_merchant.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuyerDashboardViewModel(
    private val repository: MarketplaceRepository,
    private val buyerId: String
) : ViewModel() {

    sealed class DashboardState {
        object Loading : DashboardState()

        data class Success(
            val allMerchants: List<MerchantData>,
            val filteredMerchants: List<MerchantData>,
            val categories: List<String>,
            val selectedCategory: String
        ) : DashboardState()

        data class Error(val message: String) : DashboardState()
    }

    private val _uiState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        fetchMarketplace()
    }

    fun fetchMarketplace() {
        viewModelScope.launch {
            _uiState.value = DashboardState.Loading
            try {
                val response = repository.getMerchants()
                if (response.isSuccessful && response.body() != null) {
                    val merchants = response.body()!!

                    val extractedCategories = listOf("All") + merchants.map { it.service }.distinct().sorted()

                    _uiState.value = DashboardState.Success(
                        allMerchants = merchants,
                        filteredMerchants = merchants,
                        categories = extractedCategories,
                        selectedCategory = "All"
                    )
                } else {
                    _uiState.value = DashboardState.Error("Failed to load marketplace.")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardState.Error("Network Error. Is the Go server running?")
            }
        }
    }

    fun filterByCategory(category: String) {
        val currentState = _uiState.value
        if (currentState is DashboardState.Success) {
            val filteredList = if (category == "All") {
                currentState.allMerchants
            } else {
                currentState.allMerchants.filter { it.service == category }
            }

            _uiState.value = currentState.copy(
                filteredMerchants = filteredList,
                selectedCategory = category
            )
        }
    }
}
