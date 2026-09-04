package com.example.local_merchant.ui.buyer.checkout

import android.app.Activity
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.local_merchant.viewmodel.CheckoutViewModel
import com.razorpay.Checkout
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    finalPrice: Int,
    sellerPhone: String,
    viewModel: CheckoutViewModel, // Injected dynamically via AppNavigation
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // 💡 CRUCIAL FIX: Cast exactly to FragmentActivity for the Biometric prompt!
    val activity = context as? FragmentActivity

    val orderId by viewModel.orderId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Dynamically trigger the Go server call the moment the screen opens
    LaunchedEffect(finalPrice) {
        viewModel.fetchOrderId(finalPrice)
    }

    Scaffold(
        containerColor = Color(0xFF0D0D12),
        topBar = {
            TopAppBar(
                title = { Text("Secure Checkout", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Total Negotiated Amount",
                color = Color.Gray,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹$finalPrice",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF0EA5E9))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Generating secure order...", color = Color.Gray)
            } else if (orderId != null) {
                Button(
                    onClick = {
                        if (activity != null) {
                            // 🚀 THE FIX: Trigger biometrics first, then launch Razorpay on success!
                            showBiometricPrompt(activity) {
                                launchRazorpay(activity, finalPrice, orderId!!, sellerPhone)
                            }
                        } else {
                            Toast.makeText(context, "UI Error: Cannot launch payment", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                ) {
                    Text("Pay Now", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Text("Failed to generate order from server.", color = Color.Red)
            }
        }
    }
}

private fun launchRazorpay(activity: Activity, amount: Int, orderId: String, sellerPhone: String) {
    val checkout = Checkout()
    // ⚠️ YOU MUST PASTE YOUR ACTUAL RAZORPAY TEST KEY HERE!
    checkout.setKeyID("rzp_test_TViNHBKbdqnQXr")

    try {
        val options = JSONObject()
        options.put("name", "Local Service Booking")
        options.put("description", "AI Negotiated Contract")
        options.put("theme.color", "#0EA5E9")
        options.put("currency", "INR")
        options.put("amount", amount * 100)
        options.put("order_id", orderId)

        val prefill = JSONObject()
        prefill.put("contact", sellerPhone)
        options.put("prefill", prefill)

        checkout.open(activity, options)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)

    val biometricPrompt = BiometricPrompt(
        activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess() // Trigger the payment!
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authorize Payment")
        .setSubtitle("Verify your identity to lock in this rate")
        .setNegativeButtonText("Cancel")
        .build()

    biometricPrompt.authenticate(promptInfo)
}