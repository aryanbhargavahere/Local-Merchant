package com.example.local_merchant.ui.merchant

data class CompletedJob(
    val jobId: String,
    val customerName: String,
    val serviceType: String,
    val date: String,
    val time: String,
    val finalCost: Int,
    val status: String,
    val aiNegotiated: Boolean
)
