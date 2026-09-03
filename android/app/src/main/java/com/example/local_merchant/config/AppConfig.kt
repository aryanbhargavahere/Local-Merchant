package com.example.local_merchant.config

import com.example.local_merchant.BuildConfig

object AppConfig {
    // We removed 'const' so Kotlin is happy!
    private val HOST_IP = BuildConfig.HOST_IP
    private val PORT = "8080"

    val BASE_URL = "http://$HOST_IP:$PORT/"
    val WS_URL = "ws://$HOST_IP:$PORT/"
}