package com.example.local_merchant.ui.merchant

data class MerchantProfileState(
    val isLoading: Boolean = false,
    val name: String = "",
    val service: String = "",
    val phone: String = "",
    val rating: Float = 0f,
    val jobsCompleted: Int = 0,
    val isBiometricsEnabled: Boolean = false,
)
