package com.example.local_merchant.data.remote

import com.example.local_merchant.config.AppConfig
import com.example.local_merchant.data.model.ChatMessage
import com.example.local_merchant.data.model.ChatSummary
import com.example.local_merchant.data.model.DashboardStatsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import okhttp3.ResponseBody
import retrofit2.http.Headers

// 1. The Interface (The Map)
interface GoBackendApi {

    // --- Existing Routes ---

    @POST("/api/merchants")
    suspend fun registerMerchant(@Body request: RegisterMerchantRequest): Response<MerchantResponse>

    @GET("api/chat/inbox")
    suspend fun getChatInbox(): retrofit2.Response<List<com.example.local_merchant.data.remote.ChatSummary>>

    @GET("/api/merchants/dashboard")
    suspend fun getMerchantDashboard(): Response<DashboardStatsResponse>

    @POST("/api/interact")
    suspend fun interactiveNegotiation(@Body request: HumanNegotiateRequest): Response<NegotiateResponse>

    // --- Updated Chat Routes ---
    @GET("/api/chat/history")
    suspend fun getChatHistory(@Query("conversation_id") conversationId: String): Response<List<ChatMessage>>

    // --- NEW Production Routes ---
    @GET("/api/merchant/profile")
    suspend fun getMerchantProfile(@Query("merchant_id") merchantId: String): Response<MerchantProfileResponse>

    @GET("/api/merchant/history")
    suspend fun getDealHistory(@Query("merchant_id") merchantId: String): Response<List<JobHistoryResponse>>

    @POST("/api/buyers")
    suspend fun registerBuyer(@Body request: RegisterBuyerRequest): Response<BuyerRegistrationResponse>

    @GET("/api/buyers/dashboard")
    suspend fun getBuyerDashboard(@Query("buyer_id") buyerId: String): Response<BuyerDashboardResponse>

    @GET("api/merchants")
    suspend fun getMerchants(): retrofit2.Response<List<com.example.local_merchant.data.remote.MerchantData>>

    // 🛑 THE FIX: URL exactly matches the Go server now!
    @POST("/create-order")
    suspend fun createRazorpayOrder(@Body request: OrderRequest): Response<OrderResponse>

    // 🛑 THE FIX: ResponseBody completely bypasses JSON parsing crashes!
    @POST("/api/payment-success")
    suspend fun confirmPayment(@Body request: PaymentSuccessRequest): Response<ResponseBody>

    @Headers("Cache-Control: no-cache")
    @GET("/api/merchants/dashboard")
    suspend fun getDashboardStats(@Query("merchant_id") merchantId: String): Response<DashboardStatsResponse>

    @POST("/api/merchants/toggle-ai")
    suspend fun toggleMerchantAI(@Body request: ToggleAIRequest): Response<Any>

}


// 2. The Client Builder (The Engine)
object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofitApi: GoBackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoBackendApi::class.java)
    }
}


