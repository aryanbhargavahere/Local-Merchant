package com.example.local_merchant.ui.Buyer

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.local_merchant.data.local.SessionManager
import com.example.local_merchant.viewmodel.buyer.BuyerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerSetupScreen(
    viewModel: BuyerViewModel,
    onBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Observe the ViewModel state
    val setupState by viewModel.setupState.collectAsState()

    // Local UI State for text fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Handle navigation when registration is successful
    LaunchedEffect(setupState) {
        when (setupState) {
            is BuyerViewModel.SetupState.Success -> {
                viewModel.resetState() // Reset so it doesn't immediately re-trigger if we come back
                onNavigateNext()
            }
            is BuyerViewModel.SetupState.Error -> {
                val errorMessage = (setupState as BuyerViewModel.SetupState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buyer Registration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set up your Buyer Profile",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    val digitsOnly = newValue.filter { it.isDigit() }
                    if (digitsOnly.length <= 10) {
                        phone = digitsOnly
                    }
                },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Home Address") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            Button(
                onClick = {
                    viewModel.register(
                        name = name,
                        phone = phone,
                        address = address,
                        sessionManager = sessionManager
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = setupState !is BuyerViewModel.SetupState.Loading
            ) {
                if (setupState is BuyerViewModel.SetupState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Continue to Dashboard")
                }
            }
        }
    }
}