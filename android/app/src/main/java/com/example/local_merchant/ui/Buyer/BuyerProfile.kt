package com.example.local_merchant.ui.buyer.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// DYNAMIC STATE: This holds the user's data pulled from your DB/Session
data class BuyerProfileState(
    val fullName: String = "",
    val phone: String = "",
    val location: String = "",
    val memberSince: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerProfileScreen(
    state: BuyerProfileState,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit,
    onOrderHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF0D0D12), // Dark App Background
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D12)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. DYNAMIC HEADER: Avatar & User Info
            ProfileHeader(state = state)

            Spacer(modifier = Modifier.height(32.dp))

            // 2. PROFILE ACTIONS
            Text(
                text = "Account Settings",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            ProfileOptionRow(
                icon = Icons.Rounded.Person,
                title = "Edit Profile",
                onClick = onEditProfileClick
            )
            ProfileOptionRow(
                icon = Icons.Rounded.LocationOn,
                title = "Saved Addresses",
                subtitle = state.location.ifBlank { "Add a location for accurate services" },
                onClick = onAddressesClick
            )
            ProfileOptionRow(
                icon = Icons.Rounded.CreditCard,
                title = "Payment Methods",
                onClick = onPaymentMethodsClick
            )
            ProfileOptionRow(
                icon = Icons.Rounded.List,
                title = "Order History",
                onClick = onOrderHistoryClick
            )

            Spacer(modifier = Modifier.weight(1f))

            // 3. LOGOUT BUTTON
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1E24), // Surface color
                    contentColor = Color(0xFFEF4444)    // Red accent for logout
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                    contentDescription = "Logout",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileHeader(state: BuyerProfileState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar Box
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E24)),
            contentAlignment = Alignment.Center
        ) {
            val initial = if (state.fullName.isNotBlank()) state.fullName.take(1).uppercase() else "?"
            Text(
                text = initial,
                color = Color(0xFF0EA5E9), // Brand Blue
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Dynamic User Details
        Column {
            Text(
                text = state.fullName.ifBlank { "Loading User..." },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.phone.ifBlank { "No phone number added" },
                color = Color.Gray,
                fontSize = 14.sp
            )
            if (state.memberSince.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Member since ${state.memberSince}",
                    color = Color(0xFF0EA5E9),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E24))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF0D0D12)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF0EA5E9),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Texts
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }

        // Action Arrow
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = Color.Gray
        )
    }
}