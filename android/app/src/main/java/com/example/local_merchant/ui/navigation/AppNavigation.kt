package com.example.local_merchant.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.local_merchant.data.local.SessionManager
import com.example.local_merchant.dependency.AppModule
import com.example.local_merchant.dependency.ViewModelFactory
import com.example.local_merchant.ui.Buyer.BuyerDashboardScreen
import com.example.local_merchant.ui.Buyer.chat.BuyerInboxScreen
import com.example.local_merchant.ui.Buyer.BuyerSetupScreen
import com.example.local_merchant.ui.Buyer.ThankYouScreen
import com.example.local_merchant.ui.RoleSelection.RoleSelectionScreen
import com.example.local_merchant.ui.Buyer.chat.NegotiationChatScreen
import com.example.local_merchant.ui.Buyer.profile.BuyerEditProfileScreen
import com.example.local_merchant.ui.buyer.checkout.CheckoutScreen
import com.example.local_merchant.ui.buyer.profile.BuyerOrderData
import com.example.local_merchant.ui.buyer.profile.BuyerProfileScreen
import com.example.local_merchant.ui.buyer.profile.EditProfileScreen
import com.example.local_merchant.ui.buyer.profile.OrderHistoryScreen
import com.example.local_merchant.ui.buyer.profile.PaymentMethodsScreen
import com.example.local_merchant.ui.buyer.profile.SavedAddressesScreen
import com.example.local_merchant.ui.merchant.MerchantDashboardScreen
import com.example.local_merchant.ui.merchant.MerchantSetupScreen
import com.example.local_merchant.ui.merchant.chat.ChatDetailScreen
import com.example.local_merchant.ui.merchant.chat.ChatListScreen
import com.example.local_merchant.ui.merchant.profile.MerchantProfileScreen
import com.example.local_merchant.ui.merchant.profile.internalui.AgentParametersScreen
import com.example.local_merchant.ui.merchant.profile.internalui.DealHistoryScreen
import com.example.local_merchant.viewmodel.ChatViewModel
import com.example.local_merchant.viewmodel.CheckoutViewModel
import com.example.local_merchant.viewmodel.buyer.BuyerDashboardViewModel
import com.example.local_merchant.viewmodel.buyer.BuyerInboxViewModel
import com.example.local_merchant.viewmodel.buyer.BuyerProfileViewModel
import com.example.local_merchant.viewmodel.buyer.BuyerViewModel
import com.example.local_merchant.viewmodel.merchant.MerchantDashboardViewModel
import com.example.local_merchant.viewmodel.merchant.MerchantHistoryViewModel
import com.example.local_merchant.viewmodel.merchant.MerchantProfileViewModel
import com.example.local_merchant.viewmodel.merchant.MerchantViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val repository = remember { AppModule.provideRepository(context) }
    val okHttpClient = remember { AppModule.provideOkHttpClient() }

    val sessionManager = remember { SessionManager(context) }

    val activeMerchantId by sessionManager.merchantIdFlow.collectAsState(initial = null)
    val safeMerchantId = activeMerchantId ?: ""

    val activeBuyerId by sessionManager.buyerIdFlow.collectAsState(initial = null)
    val safeBuyerId = activeBuyerId ?: ""

    val actualBuyerName by sessionManager.buyerNameFlow.collectAsState(initial = "")
    val actualBuyerPhone by sessionManager.buyerPhoneFlow.collectAsState(initial = "")

    val navigateBottomTab = { route: String ->
        navController.navigate(route) {
            popUpTo("merchant_dashboard") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(navController = navController, startDestination = "role_selection") {

        composable("role_selection") {
            RoleSelectionScreen(
                onNavigateToBuyer = {
                    if (safeBuyerId.isNotBlank()) {
                        navController.navigate("buyer_dashboard")
                    } else {
                        navController.navigate("buyer_setup")
                    }
                },
                onNavigateToMerchant = {
                    if (safeMerchantId.isNotBlank()) {
                        navController.navigate("merchant_dashboard")
                    } else {
                        navController.navigate("merchant_setup")
                    }
                }
            )
        }

        composable("merchant_setup") {
            val factory = remember { ViewModelFactory(repository, okHttpClient) }
            val merchantViewModel: MerchantViewModel = viewModel(factory = factory)

            MerchantSetupScreen(
                viewModel = merchantViewModel,
                onNavigateNext = {
                    navController.navigate("merchant_dashboard") {
                        popUpTo("merchant_setup") { inclusive = true }
                    }
                }
            )
        }

        composable("merchant_dashboard") {
            val factory = remember { ViewModelFactory(repository, okHttpClient) }
            val dashboardViewModel: MerchantDashboardViewModel = viewModel(factory = factory)

            LaunchedEffect(key1 = Unit) {
                dashboardViewModel.fetchDashboard()
            }

            MerchantDashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToChats = { navigateBottomTab("chat_inbox") },
                onNavigateToProfile = { navigateBottomTab("merchant_profile") }
            )
        }

        composable("chat_inbox") {
            val factory = remember { ViewModelFactory(repository, okHttpClient, safeMerchantId) }
            val chatViewModel: ChatViewModel = viewModel(factory = factory)

            val chatList by chatViewModel.inboxState.collectAsState(initial = emptyList())

            ChatListScreen(
                chatList = chatList,
                onChatClick = { conversationId, buyerName ->
                    navController.navigate("chat_detail/$conversationId/$buyerName")
                },
                onNavigateToDashboard = { navigateBottomTab("merchant_dashboard") },
                onNavigateToProfile = { navigateBottomTab("merchant_profile") }
            )
        }

        composable(
            route = "chat_detail/{conversationId}/{buyerName}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("buyerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            val buyerName = backStackEntry.arguments?.getString("buyerName") ?: "Customer"

            val factory = remember { ViewModelFactory(repository, okHttpClient, safeMerchantId) }
            val chatViewModel: ChatViewModel = viewModel(factory = factory)

            val messages by chatViewModel.messages.collectAsState(initial = emptyList())

            DisposableEffect(conversationId) {
                chatViewModel.connectWebSocket(conversationId)
                onDispose { chatViewModel.disconnectWebSocket() }
            }

            ChatDetailScreen(
                merchantName = buyerName,
                messages = messages,
                onBackClick = { navController.popBackStack() },
                onSendMessage = { text -> chatViewModel.sendChatMessage(conversationId, text) }
            )
        }

        composable("merchant_profile") {
            val factory = remember { ViewModelFactory(repository, okHttpClient) }
            val dashboardViewModel: MerchantDashboardViewModel = viewModel(factory = factory)

            val actualName by sessionManager.merchantNameFlow.collectAsState(initial = "Merchant")
            val actualPhone by sessionManager.merchantPhoneFlow.collectAsState(initial = "No Phone")

            MerchantProfileScreen(
                viewModel = dashboardViewModel,
                merchantId = safeMerchantId,
                merchantName = actualName ?: "Merchant",
                merchantPhone = actualPhone ?: "No Phone",
                onNavigateToDashboard = { navigateBottomTab("merchant_dashboard") },
                onNavigateToChats = { navigateBottomTab("chat_inbox") },
                onNavigateToParameters = { navController.navigate("agent_parameters") },
                onNavigateToDealHistory = { navController.navigate("deal_history") },
                onLogout = {
                    navController.navigate("role_selection") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable("buyer_setup") {
            val factory = remember { ViewModelFactory(repository, okHttpClient) }
            val buyerViewModel: BuyerViewModel = viewModel(factory = factory)

            BuyerSetupScreen(
                viewModel = buyerViewModel,
                onBack = { navController.popBackStack() },
                onNavigateNext = {
                    navController.navigate("buyer_dashboard") {
                        popUpTo("buyer_setup") { inclusive = true }
                    }
                }
            )
        }

        composable("buyer_dashboard") {
            val factory = remember { ViewModelFactory(repository, okHttpClient, safeBuyerId) }
            val dashboardViewModel: BuyerDashboardViewModel = viewModel(factory = factory)

            BuyerDashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToInbox = { navController.navigate("buyer_inbox") },
                onNavigateToChat = { merchantId, merchantName ->
                    val safeName = if (merchantName.isNotBlank()) merchantName else "Merchant"
                    navController.navigate("buyer_chat/$merchantId/$safeName")
                },
                onNavigateToProfile = { navController.navigate("buyer_profile") }
            )
        }

        composable("buyer_inbox") {
            val factory = remember { ViewModelFactory(repository, okHttpClient, safeBuyerId) }
            val inboxViewModel: BuyerInboxViewModel = viewModel(factory = factory)

            BuyerInboxScreen(
                viewModel = inboxViewModel,
                onNavigateToChat = { chatId, chatName ->
                    navController.navigate("buyer_chat/$chatId/$chatName")
                },
                onNavigateToDashboard = { navController.navigate("buyer_dashboard") },
                onNavigateToProfile = { navController.navigate("buyer_profile") }
            )
        }

        composable(route = "buyer_chat/{merchantId}/{merchantName}") { backStackEntry ->
            val merchantId = backStackEntry.arguments?.getString("merchantId") ?: ""
            val merchantName = backStackEntry.arguments?.getString("merchantName") ?: "Merchant"

            val factory = remember { ViewModelFactory(repository, okHttpClient, merchantId) }
            val chatViewModel: ChatViewModel = viewModel(factory = factory)

            DisposableEffect(key1 = merchantId) {
                chatViewModel.connectWebSocket(conversationId = merchantId)
                onDispose { chatViewModel.disconnectWebSocket() }
            }

            NegotiationChatScreen(
                viewModel = chatViewModel,
                merchantId = merchantId,
                merchantName = merchantName,
                onDealAccepted = { finalPrice ->
                    navController.navigate("checkout/$finalPrice/0000000000")
                }
            )
        }

        composable(
            route = "checkout/{finalPrice}/{sellerPhone}",
            arguments = listOf(
                navArgument("finalPrice") { type = NavType.IntType },
                navArgument("sellerPhone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val finalPrice = backStackEntry.arguments?.getInt("finalPrice") ?: 0
            val sellerPhone = backStackEntry.arguments?.getString("sellerPhone") ?: ""

            val factory = remember { ViewModelFactory(repository, okHttpClient) }
            val checkoutViewModel: CheckoutViewModel = viewModel(factory = factory)

            CheckoutScreen(
                finalPrice = finalPrice,
                sellerPhone = sellerPhone,
                viewModel = checkoutViewModel,
                onPaymentSuccess = {
                    // Drop the new navigation here!
                    navController.navigate("thank_you") {
                        // This prevents the user from going back to the payment screen
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("buyer_profile") {
            val factory = remember { ViewModelFactory(repository, okHttpClient, safeBuyerId, sessionManager) }
            val profileViewModel: BuyerProfileViewModel = viewModel(factory = factory)

            val profileState by profileViewModel.state.collectAsState()

            BuyerProfileScreen(
                state = profileState,
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate("edit_profile") },
                onAddressesClick = { navController.navigate("saved_addresses") },
                onPaymentMethodsClick = { navController.navigate("payment_methods") },
                onOrderHistoryClick = { navController.navigate("order_history") },
                onLogoutClick = {
                    navController.navigate("role_selection") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = "agent_parameters") {
            val context = LocalContext.current
            val sessionManager = remember { SessionManager(context) }
            val coroutineScope = rememberCoroutineScope()

            val currentBase by sessionManager.baseRateFlow.collectAsState(initial = 1000)
            val currentFloor by sessionManager.floorRateFlow.collectAsState(initial = 800)

            AgentParametersScreen(
                currentBaseRate = currentBase,
                currentFloorRate = currentFloor,
                onBack = { navController.popBackStack() },
                onSaveParameters = { newBase, newFloor ->
                    coroutineScope.launch {
                        sessionManager.saveAgentRates(base = newBase, floor = newFloor)
                    }
                    navController.popBackStack()
                }
            )
        }

        composable("deal_history") {
            val factory = remember { ViewModelFactory(repository, okHttpClient) }
            val dashboardViewModel: MerchantDashboardViewModel = viewModel(factory = factory)

            val state = dashboardViewModel.dashboardState.collectAsState().value
            val liveDeals = (state as? MerchantDashboardViewModel.DashboardState.Success)?.stats?.negotiations ?: emptyList()

            DealHistoryScreen(
                liveDeals = liveDeals,
                onBack = { navController.popBackStack() }
            )
        }

        composable("edit_profile") {
            val coroutineScope = rememberCoroutineScope()

            BuyerEditProfileScreen(
                currentName = actualBuyerName ?: "",
                currentPhone = actualBuyerPhone ?: "",
                onBack = { navController.popBackStack() },
                onSave = { newName, newPhone ->
                    coroutineScope.launch {
                        sessionManager.saveBuyerDetails(
                            id = safeBuyerId,
                            name = newName,
                            phone = newPhone
                        )
                    }
                    navController.popBackStack()
                }
            )
        }

        composable("saved_addresses") {
            SavedAddressesScreen(onBack = { navController.popBackStack() })
        }

        composable("payment_methods") {
            PaymentMethodsScreen(onBack = { navController.popBackStack() })
        }

        composable("order_history") {
            val factory = remember { ViewModelFactory(repository, okHttpClient, safeBuyerId, sessionManager) }
            val profileViewModel: BuyerProfileViewModel = viewModel(factory = factory)

            val profileState by profileViewModel.state.collectAsState()
            val realOrders = emptyList<BuyerOrderData>()

            OrderHistoryScreen(
                pastOrders = realOrders,
                onBack = { navController.popBackStack() }
            )
        }
        composable("thank_you") {
            ThankYouScreen(
                onDoneClick = {
                    // 🟢 Replace "buyer_dashboard" with your EXACT route name for that screen
                    navController.navigate("buyer_dashboard") {
                        // This completely wipes the history so they can't go back to the thank you screen
                        popUpTo(0)
                    }
                }
            )
        }
    }
}
