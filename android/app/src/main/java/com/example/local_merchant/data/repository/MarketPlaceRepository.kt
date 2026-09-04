package com.example.local_merchant.data.repository

import com.example.local_merchant.data.local.ChatDao
import com.example.local_merchant.data.local.ChatEntity
import com.example.local_merchant.data.local.merchant.MerchantDao
import com.example.local_merchant.data.local.merchant.MerchantEntity
import com.example.local_merchant.data.model.DashboardStatsResponse
import com.example.local_merchant.data.remote.BuyerDashboardResponse
import com.example.local_merchant.data.remote.GoBackendApi
import com.example.local_merchant.data.remote.HumanNegotiateRequest
import com.example.local_merchant.data.remote.JobHistoryResponse
import com.example.local_merchant.data.remote.OrderRequest
import com.example.local_merchant.data.remote.RegisterBuyerRequest
import com.example.local_merchant.data.remote.RegisterMerchantRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

// Model for buyer profile data
data class BuyerProfileResponse(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val location: String = "",
    val createdAt: String = ""
)

class MarketplaceRepository(
    private val api: GoBackendApi,
    private val chatDao: ChatDao,
    private val merchantDao: MerchantDao
) {

    // ==========================================
    // 🛒 BUYER LOGIC (Negotiation & Caching)
    // ==========================================

    fun getChatHistory(merchantId: String): Flow<List<ChatEntity>> {
        return chatDao.getChatHistory(merchantId)
    }

    suspend fun saveIncomingMessage(message: ChatEntity) {
        chatDao.insertMessage(message)
    }

    suspend fun sendNegotiationOffer(merchantId: String, offerAmount: Int, userMessage: String) {
        // 1. Save user message locally so it shows up on screen instantly
        chatDao.insertMessage(
            ChatEntity(merchantId = merchantId, message = userMessage, sender = "USER")
        )

        // 2. Prepare payload for Go Server
        val request = HumanNegotiateRequest(
            merchantId = merchantId,
            offer = offerAmount,
            message = userMessage,
            buyerPhone = "9876543210",
            buyerAddress = "Local User Address"
        )

        // 3. Send over network and handle response
        try {
            val response = api.interactiveNegotiation(request)

            if (response.isSuccessful && response.body() != null) {
                val aiResponse = response.body()!!

                // Save AI response to local DB
                val aiMessage = aiResponse.message ?: "Offer ${aiResponse.status}"
                chatDao.insertMessage(
                    ChatEntity(merchantId = merchantId, message = aiMessage, sender = "AI")
                )

                // If accepted, generate a System Alert bubble revealing the seller's phone number
                if (aiResponse.status == "ACCEPTED") {
                    val systemMsg = "✅ Offer Accepted! Final Price: ₹${aiResponse.finalPrice}\nSeller Phone: ${aiResponse.sellerPhone}"
                    chatDao.insertMessage(
                        ChatEntity(merchantId = merchantId, message = systemMsg, sender = "SYSTEM")
                    )
                }
            } else {
                chatDao.insertMessage(
                    ChatEntity(merchantId = merchantId, message = "Network Error. AI could not respond.", sender = "SYSTEM")
                )
            }
        } catch (e: Exception) {
            chatDao.insertMessage(
                ChatEntity(merchantId = merchantId, message = "Connection Failed: ${e.localizedMessage}", sender = "SYSTEM")
            )
        }
    }


    // ==========================================
    // 💼 MERCHANT LOGIC (Deploying AI Agents)
    // ==========================================

    suspend fun registerMerchant(
        name: String, service: String, phone: String, baseRate: Int, floorRate: Int
    ): String? {
        val request = RegisterMerchantRequest(name, service, phone, baseRate, floorRate)

        return try {
            val response = api.registerMerchant(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val backendMerchant = response.body()?.merchant

                if (backendMerchant != null) {
                    // Instantly cache the new profile into Room!
                    val newProfile = MerchantEntity(
                        merchantId = backendMerchant.id,
                        name = backendMerchant.name,
                        service = backendMerchant.service,
                        phone = backendMerchant.phone,
                        rating = 5.0f, // Default 5-star for a brand new account
                        jobsCompleted = 0,
                        isBiometricsEnabled = false
                    )
                    merchantDao.insertOrUpdateProfile(newProfile)

                    return backendMerchant.id // Return the ID to SessionManager
                }
                null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ==========================================
    // 🛒 BUYER PROFILE & REGISTRATION
    // ==========================================

    suspend fun registerBuyer(
        name: String,
        phone: String,
        address: String
    ): String? {
        val request = RegisterBuyerRequest(name, phone, address)

        return try {
            val response = api.registerBuyer(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val backendBuyerId = response.body()?.buyerId
                if (backendBuyerId != null) {
                    return backendBuyerId
                }
                null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * DYNAMIC PROFILE FETCH: Grabs the live profile data from the Go API.
     * Note: You will need to add @GET("/api/buyer/{id}") fun getBuyerProfile(@Path("id") id: String) to GoBackendApi!
     */
    fun getBuyerProfile(buyerId: String): Flow<BuyerProfileResponse> = flow {
        try {
            // Check if the method exists in your API interface.
            // If you haven't built the Go route yet, this acts as a dynamic safety net so the app doesn't crash.
            val response = api.getBuyerDashboard(buyerId)

            if (response.isSuccessful && response.body() != null) {
                val dashboardData = response.body()!!
                // Mapping the dashboard data to the profile state dynamically
                emit(BuyerProfileResponse(
                    id = buyerId,
                    name = dashboardData.buyerName ?: "Unknown User",
                    phone = "Phone hidden for privacy", // Update this if your dashboard response includes phone
                    location = "Location tracking active",
                    createdAt = "Today"
                ))
            }
        } catch (e: Exception) {
            // Failsafe empty emit so the UI doesn't crash on network failure
            emit(BuyerProfileResponse())
        }
    }

    // ==========================================
    // 👤 MERCHANT PROFILE LOGIC (Hybrid Data Model)
    // ==========================================

    fun getDynamicMerchantProfile(merchantId: String): Flow<MerchantEntity?> = flow {
        val localProfile = merchantDao.getMerchantProfileDirect(merchantId)
        val localBiometricsEnabled = localProfile?.isBiometricsEnabled ?: false

        try {
            val response = api.getMerchantProfile(merchantId)

            if (response.isSuccessful && response.body() != null) {
                val backendProfile = response.body()!!

                val combinedEntity = MerchantEntity(
                    merchantId = merchantId,
                    name = backendProfile.name,
                    service = backendProfile.service,
                    phone = backendProfile.phone,
                    rating = backendProfile.rating,
                    jobsCompleted = backendProfile.jobsCompleted,
                    isBiometricsEnabled = localBiometricsEnabled
                )

                merchantDao.insertOrUpdateProfile(combinedEntity)
                emit(combinedEntity)

            } else {
                emit(localProfile)
            }
        } catch (e: Exception) {
            emit(localProfile)
        }
    }

    suspend fun updateBiometricPreference(merchantId: String, isEnabled: Boolean) {
        merchantDao.updateBiometricFlag(merchantId, isEnabled)
    }

    // ==========================================
    // 📜 MERCHANT HISTORY LOGIC
    // ==========================================

    fun getDealHistoryFlow(merchantId: String): Flow<List<JobHistoryResponse>> = flow {
        try {
            val response = api.getDealHistory(merchantId)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun getBuyerDashboard(buyerId: String): Response<BuyerDashboardResponse> {
        return api.getBuyerDashboard(buyerId)
    }

    suspend fun getDashboardStats(): Response<DashboardStatsResponse> {
        return api.getMerchantDashboard()
    }

    suspend fun getMerchants(): retrofit2.Response<List<com.example.local_merchant.data.remote.MerchantData>> {
        return api.getMerchants()
    }

    suspend fun getChatInbox(): retrofit2.Response<List<com.example.local_merchant.data.remote.ChatSummary>> {
        return api.getChatInbox()
    }

    // ==========================================
    // 📬 DYNAMIC INBOX GENERATOR
    // ==========================================

    fun getRealInboxSummaries(): Flow<List<com.example.local_merchant.data.remote.ChatSummary>> = flow {
        val merchants = try {
            val response = api.getMerchants()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val merchantMap = merchants.associateBy { it.id }

        chatDao.getAllChats().collect { allChats ->
            val dynamicInbox = allChats
                .groupBy { it.merchantId }
                .map { (merchantId, messages) ->
                    val lastMessage = messages.last()

                    val realName = merchantMap[merchantId]?.name ?: "Pro ($merchantId)"

                    com.example.local_merchant.data.remote.ChatSummary(
                        id = merchantId,
                        name = realName,
                        lastMessage = lastMessage.message,
                        time = "Recent",
                        unreadCount = 0,
                        initials = realName.take(1).uppercase()
                    )
                }
                .reversed()

            emit(dynamicInbox)
        }
    }

    suspend fun generateRazorpayOrderId(finalPrice: Int): String? {
        return try {
            val response = api.createRazorpayOrder(OrderRequest(finalPrice))
            if (response.isSuccessful) response.body()?.orderId else null
        } catch (e: Exception) {
            null
        }
    }

    // ==========================================
    // MERCHANT INBOX GENERATOR
    // ==========================================
    fun getMerchantInboxSummaries(currentMerchantId: String): Flow<List<com.example.local_merchant.data.remote.ChatSummary>> = flow {
        chatDao.getAllChats().collect { allChats ->
            // 1. Group messages by the BUYER'S ID, not the Merchant's ID
            val groupedChats = allChats.groupBy { msg ->
                if (msg.sender == currentMerchantId) {
                    msg.merchantId // Our outgoing messages hold the buyer ID here
                } else {
                    msg.sender // Incoming messages hold the buyer ID in sender
                }
            }

            val dynamicInbox = mutableListOf<com.example.local_merchant.data.remote.ChatSummary>()

            // 2. Loop through each chat thread to fetch the real buyer's name
            for ((buyerId, messages) in groupedChats) {
                val lastMessage = messages.last()
                var buyerName = "Client"

                try {
                    // Try to fetch the real buyer profile from the Go backend!
                    val response = api.getBuyerDashboard(buyerId)
                    if (response.isSuccessful && response.body() != null) {
                        buyerName = response.body()!!.buyerName ?: "Client"
                    }
                } catch (e: Exception) {
                    // If network fails, default to a safe generic name
                    val safeId = if (buyerId.length >= 4) buyerId.take(4) else buyerId
                    buyerName = "Client ($safeId)"
                }

                dynamicInbox.add(
                    com.example.local_merchant.data.remote.ChatSummary(
                        id = buyerId,
                        name = buyerName,
                        lastMessage = lastMessage.message,
                        time = "Recent",
                        unreadCount = 0,
                        initials = buyerName.take(1).uppercase()
                    )
                )
            }

            emit(dynamicInbox.reversed())
        }
    }
}

