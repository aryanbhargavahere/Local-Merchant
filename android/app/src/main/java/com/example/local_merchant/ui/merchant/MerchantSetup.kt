package com.example.local_merchant.ui.merchant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.local_merchant.data.local.SessionManager
import com.example.local_merchant.ui.components.Background
import com.example.local_merchant.ui.theme.*
import com.example.local_merchant.viewmodel.merchant.MerchantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantSetupScreen(
    viewModel: MerchantViewModel,
    onNavigateNext: () -> Unit // Called when deployment succeeds
) {
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }

    // 1. Local State: Holding the user's typed input
    var merchantName by remember { mutableStateOf("") }
    var serviceName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var baseRate by remember { mutableFloatStateOf(1200f) }
    var floorRate by remember { mutableFloatStateOf(800f) }

    // 2. ViewModel State: Observing the network/logic status
    val setupState by viewModel.setupState.collectAsState()

    // 3. Navigation Trigger: Automatically move to the next screen on success
    LaunchedEffect(setupState) {
        if (setupState is MerchantViewModel.SetupState.Success) {
            onNavigateNext()
        }
    }

    Background {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Your AI Clone",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = { merchantName = it },
                        label = { Text("Your Name", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonEmerald,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = { Text("Service Type (e.g., Plumbing)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonEmerald,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🛑 THE FIX: Phone number locked to 10 digits and numeric keypad
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.filter { it.isDigit() }
                            if (digitsOnly.length <= 10) {
                                phone = digitsOnly
                            }
                        },
                        label = { Text("Secure Phone Number", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonEmerald,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Target Base Rate: ₹${baseRate.toInt()}", color = Color.White)
                    Slider(
                        value = baseRate,
                        onValueChange = { baseRate = it },
                        valueRange = 500f..5000f,
                        colors = SliderDefaults.colors(thumbColor = NeonEmerald, activeTrackColor = NeonEmerald)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Walk-Away Limit (Floor): ₹${floorRate.toInt()}", color = Color.LightGray)
                    Slider(
                        value = floorRate,
                        onValueChange = { floorRate = it },
                        valueRange = 100f..4000f,
                        colors = SliderDefaults.colors(thumbColor = Color.Gray, activeTrackColor = Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Dynamic Error Handling: Shows only if the ViewModel catches an error
            if (setupState is MerchantViewModel.SetupState.Error) {
                Text(
                    text = (setupState as MerchantViewModel.SetupState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // 5. Dynamic Button: Disables and shows a spinner while loading
            Button(
                onClick = {
                    viewModel.deployAgent(
                        name = merchantName,
                        service = serviceName,
                        phone = phone,
                        baseRate = baseRate.toInt(),
                        floorRate = floorRate.toInt(),
                        sessionManager = sessionManager
                    )
                },
                enabled = setupState !is MerchantViewModel.SetupState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (setupState is MerchantViewModel.SetupState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("DEPLOY AI AGENT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}