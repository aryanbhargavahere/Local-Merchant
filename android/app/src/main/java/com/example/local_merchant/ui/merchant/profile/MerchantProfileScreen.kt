package com.example.local_merchant.ui.merchant.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.local_merchant.data.remote.ApiClient
import com.example.local_merchant.data.remote.ToggleAIRequest
import com.example.local_merchant.ui.components.Background
import com.example.local_merchant.ui.merchant.chat.MerchantBottomNav
import com.example.local_merchant.viewmodel.merchant.MerchantDashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MerchantProfileScreen(
    viewModel: MerchantDashboardViewModel,
    merchantId: String,
    merchantName: String,
    merchantPhone: String,
    onNavigateToDashboard: () -> Unit,
    onNavigateToChats: () -> Unit,
    // 🛑 History Navigation Parameter Removed
    onNavigateToParameters: () -> Unit,
    onNavigateToDealHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("merchant_settings", Context.MODE_PRIVATE) }

    val dashboardState by viewModel.dashboardState.collectAsState()
    val stats = (dashboardState as? MerchantDashboardViewModel.DashboardState.Success)?.stats
    val jobsCompleted = stats?.closedToday ?: 0

    var isBiometricOn by remember { mutableStateOf(false) }
    var localAiActive by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isBiometricOn = prefs.getBoolean("biometric_enabled", false)
        localAiActive = !prefs.getBoolean("ai_is_off", false)
    }

    Background {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                // 🛑 Bottom Nav Call Updated
                MerchantBottomNav(
                    currentRoute = "merchant_profile",
                    onNavigateToDashboard = onNavigateToDashboard,
                    onNavigateToChats = onNavigateToChats,
                    onNavigateToProfile = {}
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF272730)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(merchantName.take(1).uppercase(), color = Color(0xFF0EA5E9), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(merchantName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(merchantPhone, color = Color.Gray, fontSize = 16.sp, letterSpacing = 1.sp)

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Jobs Completed", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$jobsCompleted", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("AI NEGOTIATION", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)), shape = RoundedCornerShape(20.dp)) {
                        Column {
                            ProfileMenuItem(Icons.Rounded.SmartToy, "Agent Parameters", "Update floor rates & upsell rules", onNavigateToParameters)
                            HorizontalDivider(color = Color(0xFF272730), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                            ProfileMenuItem(Icons.Rounded.History, "Deal History", "View AI closed deals", onNavigateToDealHistory)
                            HorizontalDivider(color = Color(0xFF272730), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

                            ProfileSwitchItem(
                                icon = Icons.Rounded.PowerSettingsNew,
                                title = "AI Auto-Pilot",
                                subtitle = if (localAiActive) "Handling chats automatically" else "Paused. Manual mode active.",
                                isChecked = localAiActive,
                                onCheckedChange = { turnedOn ->
                                    localAiActive = turnedOn
                                    prefs.edit().putBoolean("ai_is_off", !turnedOn).apply()
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            ApiClient.retrofitApi.toggleMerchantAI(ToggleAIRequest(merchantId, !turnedOn))
                                            withContext(Dispatchers.Main) { Toast.makeText(context, if (turnedOn) "AI Resumed" else "AI Paused", Toast.LENGTH_SHORT).show() }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("ACCOUNT", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)), shape = RoundedCornerShape(20.dp)) {
                        Column {
                            ProfileMenuItem(Icons.Rounded.AccountBalanceWallet, "Payout Methods", "Bank Accounts & UPI", {})
                            HorizontalDivider(color = Color(0xFF272730), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

                            ProfileSwitchItem(
                                icon = Icons.Rounded.Fingerprint,
                                title = "Biometric Lock",
                                subtitle = "Require authentication to open app",
                                isChecked = isBiometricOn,
                                onCheckedChange = {
                                    isBiometricOn = it
                                    prefs.edit().putBoolean("biometric_enabled", it).apply()
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                TextButton(onClick = onLogout) { Text("Log Out", color = Color(0xFFEF4444), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun ProfileSwitchItem(icon: ImageVector, title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0EA5E9), uncheckedThumbColor = Color.Gray, uncheckedTrackColor = Color(0xFF272730))
        )
    }
}