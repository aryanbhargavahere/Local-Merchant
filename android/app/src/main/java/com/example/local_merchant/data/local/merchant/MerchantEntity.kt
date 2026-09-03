package com.example.local_merchant.data.local.merchant

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_profile")
data class MerchantEntity(
    @PrimaryKey
    val merchantId: String,
    val name: String,
    val service: String,
    val phone: String,
    val rating: Float,
    val jobsCompleted: Int,
    val isBiometricsEnabled: Boolean = false // Default to false
)