package com.example.local_merchant.ui.buyer.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- 1. EDIT PROFILE SCREEN (100% DYNAMIC) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentName: String,
    currentPhone: String,
    onBack: () -> Unit
) {
    // Local state to hold the typed values, initialized with the real DataStore values
    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf(currentPhone) }

    Scaffold(
        containerColor = Color(0xFF0D0D12),
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onBack, // In a full build, this would save to DataStore first
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

// --- 2. SAVED ADDRESSES SCREEN (DYNAMIC EMPTY STATE) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedAddressesScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Color(0xFF0D0D12),
        topBar = {
            TopAppBar(
                title = { Text("Saved Addresses", color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }, containerColor = Color(0xFF0EA5E9)) { Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color.White) }
        }
    ) { padding ->
        EmptyStateView(
            padding = padding,
            icon = Icons.Rounded.LocationOff,
            title = "No Saved Addresses",
            subtitle = "You haven't added any delivery or service locations yet."
        )
    }
}

// --- 3. PAYMENT METHODS SCREEN (DYNAMIC EMPTY STATE) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Color(0xFF0D0D12),
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods", color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }, containerColor = Color(0xFF0EA5E9)) { Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color.White) }
        }
    ) { padding ->
        EmptyStateView(
            padding = padding,
            icon = Icons.Rounded.CreditCardOff,
            title = "No Payment Methods",
            subtitle = "Transactions are currently handled securely via Razorpay checkout."
        )
    }
}

// --- 4. ORDER HISTORY SCREEN (DYNAMIC EMPTY STATE) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Color(0xFF0D0D12),
        topBar = {
            TopAppBar(
                title = { Text("Order History", color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        EmptyStateView(
            padding = padding,
            icon = Icons.Rounded.History,
            title = "No Order History",
            subtitle = "You haven't closed any deals with merchants yet."
        )
    }
}

// --- REUSABLE EMPTY STATE COMPONENT ---
@Composable
fun EmptyStateView(padding: PaddingValues, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}