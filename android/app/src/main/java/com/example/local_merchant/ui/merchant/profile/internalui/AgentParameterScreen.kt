package com.example.local_merchant.ui.merchant.profile.internalui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.local_merchant.ui.components.Background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentParametersScreen(
    currentBaseRate: Int,
    currentFloorRate: Int,
    onBack: () -> Unit,
    onSaveParameters: (Int, Int) -> Unit
) {
    var baseRate by remember { mutableStateOf(currentBaseRate.toString()) }
    var floorRate by remember { mutableStateOf(currentFloorRate.toString()) }
    val context = LocalContext.current

    Background {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Agent Parameters", color = Color.White) },
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
                    .padding(24.dp)
            ) {
                Text(
                    text = "Configure your AI Negotiator",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = baseRate,
                    onValueChange = { baseRate = it.filter { char -> char.isDigit() } },
                    label = { Text("Standard Base Rate (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0EA5E9),
                        focusedLabelColor = Color(0xFF0EA5E9),
                        unfocusedBorderColor = Color.DarkGray,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = floorRate,
                    onValueChange = { floorRate = it.filter { char -> char.isDigit() } },
                    label = { Text("Absolute Floor Rate (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0EA5E9),
                        focusedLabelColor = Color(0xFF0EA5E9),
                        unfocusedBorderColor = Color.DarkGray,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The AI will never accept a deal below the floor rate.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val finalBase = baseRate.toIntOrNull() ?: currentBaseRate
                        val finalFloor = floorRate.toIntOrNull() ?: currentFloorRate

                        onSaveParameters(finalBase, finalFloor)
                        Toast.makeText(context, "Parameters Updated Successfully", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                ) {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
