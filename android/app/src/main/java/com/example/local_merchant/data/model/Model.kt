package com.example.local_merchant.data.model

import com.example.local_merchant.data.remote.ActiveNegotiation
import com.google.gson.annotations.SerializedName

data class DashboardStatsResponse(
    @SerializedName("merchant_id") val merchantId: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = true,
    @SerializedName("today_revenue") val todayRevenue: String? = "0",
    @SerializedName("revenue_trend") val revenueTrend: List<Int>? = emptyList(),
    @SerializedName("active_deals") val activeDeals: Int? = 0,
    @SerializedName("closed_today") val closedToday: Int? = 0,
    @SerializedName("trend_labels") val trendLabels: List<String>? = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    @SerializedName("currency_symbol") val currencySymbol: String? = "₹",
    @SerializedName("revenue_growth") val revenueGrowth: String? = "0%",
    @SerializedName("is_growth_positive") val isGrowthPositive: Boolean? = true,
    @SerializedName("negotiations") val negotiations: List<ActiveNegotiation>? = emptyList()
)

data class ActiveNegotiationItem(
    @SerializedName("id") val id: String? = "",
    @SerializedName("initial") val initial: String? = "",
    @SerializedName("name") val name: String? = "",
    @SerializedName("task") val task: String? = "",
    @SerializedName("price") val price: String? = "",
    @SerializedName("status") val status: String? = ""
)

data class ChatSummary(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val initials: String
)

data class ChatMessage(
    val id: String,
    val text: String,
    val time: String,
    val isFromMe: Boolean
)