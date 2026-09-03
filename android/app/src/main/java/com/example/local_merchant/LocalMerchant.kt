package com.example.local_merchant

import android.app.Application
import android.util.Log
import com.example.local_merchant.data.local.AppDatabase
import com.example.local_merchant.data.local.SessionManager
import com.example.local_merchant.data.remote.GoBackendApi // Adjust import to match your setup
import com.example.local_merchant.data.repository.MarketplaceRepository

class LocalMerchant : Application() {

    // 1. Declare your memory and repository components globally
    lateinit var sessionManager: SessionManager
        private set

    lateinit var database: AppDatabase
        private set

    lateinit var repository: MarketplaceRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // 2. Initialize Android "Memory" exactly once on boot
        sessionManager = SessionManager(this)

        // Assuming you have a standard Room setup like AppDatabase.getDatabase(context)
        database = AppDatabase.getDatabase(this)

        // 3. Wire up the central Repository (You will need to pass your Retrofit API instance here)
        // repository = MarketplaceRepository(
        //     api = RetrofitClient.api, // Replace with your actual Retrofit instance
        //     chatDao = database.chatDao(),
        //     merchantDao = database.merchantDao()
        // )

        Log.d("LocalMerchant", "An App Connecting Tradesman With The Buyers")
    }
}