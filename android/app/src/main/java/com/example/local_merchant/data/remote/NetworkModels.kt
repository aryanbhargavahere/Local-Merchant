package com.example.local_merchant.data.remote

import com.google.gson.annotations.SerializedName

// ==========================================
// 📤 REQUEST MODELS (Sent to Go Server)
// ==========================================

data class RegisterMerchantRequest(
    val name: String,
    val service: String,
    val phone: String,
    @SerializedName("base_rate") val baseRate: Int,
    @SerializedName("floor_rate") val floorRate: Int,
    @SerializedName("upsell_rules") val upsellRules: String = ""
)

data class HumanNegotiateRequest(
    @SerializedName("merchant_id") val merchantId: String,
    val offer: Int,
    val message: String,
    @SerializedName("buyer_phone") val buyerPhone: String,
    @SerializedName("buyer_address") val buyerAddress: String
)

// ==========================================
// 📥 RESPONSE MODELS (Received from Go Server)
// ==========================================

data class MerchantResponse(
    val success: Boolean,
    val merchant: MerchantData?
)

data class MerchantData(
    val id: String,
    val name: String,
    val service: String,
    val phone: String,
    @SerializedName("base_rate") val baseRate: Int,
    @SerializedName("floor_rate") val floorRate: Int
)

data class NegotiateResponse(
    val success: Boolean,
    val status: String?, // "ACCEPTED", "REJECTED", "COUNTER"
    @SerializedName("counter_offer") val counterOffer: Int?,
    @SerializedName("final_price") val finalPrice: Int?,
    @SerializedName("order_id") val orderId: String?,
    val message: String,
    @SerializedName("seller_phone") val sellerPhone: String? // Only populated when ACCEPTED
)

data class MerchantProfileResponse(
    val id: String,
    val name: String,
    val service: String,
    val phone: String,
    val rating: Float,
    @SerializedName("jobs_completed") val jobsCompleted: Int,
    @SerializedName("biometrics_enabled") val biometricsEnabled: Boolean
)

data class JobHistoryResponse(
    @SerializedName("job_id") val jobId: String,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("service_type") val serviceType: String,
    val date: String,
    val time: String,
    @SerializedName("final_cost") val finalCost: Int,
    @SerializedName("ai_negotiated") val aiNegotiated: Boolean,
    val status: String
)

data class RegisterBuyerRequest(
    val name: String,
    val phone: String,
    val address: String
)

data class BuyerRegistrationResponse(
    val success: Boolean,
    @SerializedName("buyer_id") val buyerId: String?
)

data class BuyerDashboardResponse(
    @SerializedName("activeRequests") val activeRequests: Int = 0,
    @SerializedName("recentNegotiations") val recentNegotiations: List<NegotiationSummary>? = emptyList(),
    val buyerName: String? = null,
    val phone: String? = null,
    val location: String? = null
)

data class NegotiationSummary(
    val id: String,
    @SerializedName("title") val title: String? = "Unknown Job",
    val status: String,
    @SerializedName("latest_offer") val latestOffer: Int? = 0
)

data class ChatSummary(
    @SerializedName("ID") val id: String,
    @SerializedName("Name") val name: String,
    @SerializedName("LastMessage") val lastMessage: String,
    @SerializedName("Time") val time: String,
    @SerializedName("UnreadCount") val unreadCount: Int,
    @SerializedName("Initials") val initials: String
)

// 🛑 THE FIX: Proper JSON translation for the Go Server!
data class OrderRequest(
    @SerializedName("amount")
    val finalPrice: Int
)

data class OrderResponse(
    @SerializedName("orderId")
    val orderId: String,
    val error: String? = null
)

data class PaymentSuccessRequest(
    @SerializedName("merchant_id") val merchantId: String,
    val amount: Int,
    @SerializedName("payment_id") val paymentId: String
)

data class ToggleAIRequest(
    @SerializedName("merchant_id") val merchantId: String,
    @SerializedName("is_ai_off") val isAiOff: Boolean
)

data class ActiveNegotiation(
    @SerializedName("id") val id: String? = null,
    @SerializedName("initial") val initial: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("task") val task: String? = null,
    @SerializedName("price") val price: String? = null,
    @SerializedName("status") val status: String? = null
)