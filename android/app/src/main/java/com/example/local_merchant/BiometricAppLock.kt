package com.example.local_merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import com.example.local_merchant.ui.buyer.checkout.showBiometricPrompt

@Composable
fun BiometricAppLock(
    isBiometricEnabled: Boolean,
    activity: FragmentActivity,
    content: @Composable () -> Unit
) {
    // If biometrics aren't enabled, we are instantly authenticated.
    var isAuthenticated by remember { mutableStateOf(!isBiometricEnabled) }
    var promptTriggered by remember { mutableStateOf(false) }

    if (isAuthenticated) {
        content() // The app unlocks and draws your NavHost!
    } else {
        // A secure, blank loading screen while the biometric prompt is up
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D12))
        ) {
            LaunchedEffect(Unit) {
                if (!promptTriggered) {
                    promptTriggered = true
                    showBiometricPrompt(activity) {
                        isAuthenticated = true
                    }
                }
            }
        }
    }
}