package com.example.local_merchant.ui.RoleSelection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Work
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
import com.example.local_merchant.ui.components.Background
import com.example.local_merchant.ui.theme.*

@Composable
fun RoleSelectionScreen(
    onNavigateToBuyer: () -> Unit,
    onNavigateToMerchant: () -> Unit
) {
    Background {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Local Merchant",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Choose how you'll trade today ",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Buyer Card (Blue Accent)
            SelectionCard(
                title = "Enter as Buyer",
                description = "Find local services and let your agent negotiate the price",
                icon = Icons.Rounded.ShoppingCart,
                iconTint = Color(0xFF60A5FA), // Soft Blue
                borderColor = Color(0xFF3B82F6).copy(alpha = 0.4f),
                onClick = onNavigateToBuyer
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Merchant Card (Emerald Accent)
            SelectionCard(
                title = "Enter as Merchant",
                description = "Deploy an AI clone that negotiates offers for you, 24/7",
                icon = Icons.Rounded.Work,
                iconTint = Color(0xFF34D399), // Emerald Green
                borderColor = Color(0xFF10B981).copy(alpha = 0.4f),
                onClick = onNavigateToMerchant
            )
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    // Uses a slightly translucent deep background to maintain the glassmorphic feel
    val cardBackground = Color(0xFF1E1E24).copy(alpha = 0.7f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Icon container with soft tinted background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Select",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}