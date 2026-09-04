package com.example.local_merchant

import android.app.Application
import android.util.Log
import com.example.local_merchant.data.local.AppDatabase
import com.example.local_merchant.data.local.SessionManager
import com.example.local_merchant.data.remote.GoBackendApi
import com.example.local_merchant.data.repository.MarketplaceRepository

class LocalMerchant : Application() {

    lateinit var sessionManager: SessionManager
        private set

    lateinit var database: AppDatabase
        private set

    lateinit var repository: MarketplaceRepository
        private set

    override fun onCreate() {
        super.onCreate()

        sessionManager = SessionManager(this)
        database = AppDatabase.getDatabase(this)

        Log.d("LocalMerchant", "An App Connecting Tradesman With The Buyers")
    }
}