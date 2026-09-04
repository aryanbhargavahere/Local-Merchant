package com.example.local_merchant.dependency

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.local_merchant.data.local.AppDatabase
import com.example.local_merchant.data.local.SessionManager
import com.example.local_merchant.data.remote.ApiClient
import com.example.local_merchant.data.repository.MarketplaceRepository
import com.example.local_merchant.viewmodel.ChatViewModel
import com.example.local_merchant.viewmodel.CheckoutViewModel
import com.example.local_merchant.viewmodel.buyer.BuyerDashboardViewModel
import com.example.local_merchant.viewmodel.buyer.BuyerInboxViewModel
import com.example.local_merchant.viewmodel.buyer.BuyerProfileViewModel
import com.example.local_merchant.viewmodel.buyer.BuyerViewModel
import com.example.local_merchant.viewmodel.merchant.MerchantDashboardViewModel
import com.example.local_merchant.viewmodel.merchant.MerchantHistoryViewModel
import com.example.local_merchant.viewmodel.merchant.MerchantProfileViewModel
import com.example.local_merchant.viewmodel.merchant.MerchantViewModel
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object AppModule {
    private var repository: MarketplaceRepository? = null
    private var okHttpClient: OkHttpClient? = null

    // 1. Provide a Singleton OkHttpClient configured for WebSockets
    fun provideOkHttpClient(): OkHttpClient {
        return okHttpClient ?: synchronized(this) {
            val client = OkHttpClient.Builder()
                // WebSockets need longer timeouts so the tunnel doesn't drop
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(39, TimeUnit.SECONDS)
                .build()
            okHttpClient = client
            client
        }
    }

    // 2. Provide the Repository
    fun provideRepository(context: Context): MarketplaceRepository {
        return repository ?: synchronized(this) {
            val database = AppDatabase.getDatabase(context)
            val repo = MarketplaceRepository(
                api = ApiClient.retrofitApi,
                chatDao = database.chatDao(),
                merchantDao = database.merchantDao()
            )
            repository = repo
            repo
        }
    }
}

class ViewModelFactory(
    private val repository: MarketplaceRepository,
    private val okHttpClient: OkHttpClient,
    // Note: This parameter acts as a generic "userId", holding either the Merchant's ID or Buyer's ID
    // depending on which side of the app is currently open.
    private val merchantId: String = "",
    private val sessionManager: SessionManager? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // 1. Profile ViewModel (Requires ID)
            modelClass.isAssignableFrom(MerchantProfileViewModel::class.java) -> {
                require(merchantId.isNotBlank()) { "merchantId is required for MerchantProfileViewModel" }
                MerchantProfileViewModel(repository, merchantId) as T
            }

            // 2. Chat ViewModel
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                require(merchantId.isNotBlank()) { "userId is required for ChatViewModel" }
                ChatViewModel(
                    repository = repository,
                    currentUserId = merchantId,
                    okHttpClient = okHttpClient
                ) as T
            }

            // 3. Dashboard ViewModel
            modelClass.isAssignableFrom(MerchantDashboardViewModel::class.java) -> {
                MerchantDashboardViewModel(repository) as T
            }

            // 4. History ViewModel
            modelClass.isAssignableFrom(MerchantHistoryViewModel::class.java) -> {
                MerchantHistoryViewModel(repository) as T
            }

            // 5. Setup/Registration ViewModel
            modelClass.isAssignableFrom(MerchantViewModel::class.java) -> {
                MerchantViewModel(repository) as T
            }

            // 6. Buyer Registration ViewModel
            modelClass.isAssignableFrom(BuyerViewModel::class.java) -> {
                BuyerViewModel(repository) as T
            }

            // 7. Buyer Profile ViewModel
            modelClass.isAssignableFrom(BuyerProfileViewModel::class.java) -> {
                val sm = sessionManager ?: throw IllegalArgumentException("sessionManager is required for BuyerProfileViewModel")
                BuyerProfileViewModel(repository, sm) as T
            }

            // 8. Buyer Dashboard ViewModel
            modelClass.isAssignableFrom(BuyerDashboardViewModel::class.java) -> {
                BuyerDashboardViewModel(repository, buyerId = merchantId) as T
            }

            // 9. Buyer Inbox ViewModel
            modelClass.isAssignableFrom(BuyerInboxViewModel::class.java) -> {
                BuyerInboxViewModel(repository) as T
            }

            // 10. Checkout ViewModel
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) -> {
                CheckoutViewModel(repository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}