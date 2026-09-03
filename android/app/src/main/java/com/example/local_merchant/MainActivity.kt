package com.example.local_merchant

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.example.local_merchant.data.remote.ApiClient
import com.example.local_merchant.data.remote.PaymentSuccessRequest
import com.example.local_merchant.ui.navigation.AppNavigation
import com.example.local_merchant.ui.theme.LocalMerchantTheme
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

// Global state to track the active order and broadcast payment success
object ActiveCheckoutState {
    var merchantId: String = ""
    var amount: Int = 0
    // 🛑 THE BRIDGE: This flow will signal the UI when the payment is done
    val paymentSuccessSignal = MutableSharedFlow<Unit>()
}

class MainActivity : FragmentActivity(), PaymentResultWithDataListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        Checkout.preload(applicationContext)

        setContent {
            LocalMerchantTheme {
                AppNavigation()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onPaymentSuccess(razorpayPaymentID: String?, paymentData: PaymentData?) {
        val paymentId = razorpayPaymentID ?: "unknown_id"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (ActiveCheckoutState.merchantId.isNotEmpty()) {
                    val request = PaymentSuccessRequest(
                        merchantId = ActiveCheckoutState.merchantId,
                        amount = ActiveCheckoutState.amount,
                        paymentId = paymentId
                    )
                    ApiClient.retrofitApi.confirmPayment(request)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 🛑 THE FIX: Tell the Compose UI to drop the checkout screen!
        GlobalScope.launch(Dispatchers.Main) {
            ActiveCheckoutState.paymentSuccessSignal.emit(Unit)
        }
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
    }
}