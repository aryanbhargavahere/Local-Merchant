package com.example.local_merchant.config

import com.example.local_merchant.BuildConfig

object AppConfig {
    // Directly use the full secure URLs we injected from local.properties via Gradle
    val BASE_URL = BuildConfig.BASE_URL
    val WS_URL = BuildConfig.WS_URL
}