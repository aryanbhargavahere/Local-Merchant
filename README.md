<h1 align="center">Local-Merchant</h1>

<p align="center">
  <b>An AI-Powered Service Marketplace for Local Tradeworkers</b><br>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Go-00ADD8?style=for-the-badge&logo=go&logoColor=white" alt="Go">
  <img src="https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
  <img src="https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white" alt="Render">
</p>

---

## ✨ Core Features

*   **🤖 Autonomous Live Negotiation:** Dual AI agents (powered by the Groq API) handle dynamic price negotiations based on strict floor-rate parameters set by the tradeworker.
*   **⚡ Ultra-Low Latency Chat:** A high-concurrency Go backend utilizes Gorilla WebSockets to stream negotiation packets instantly to the Android client.
*   **💳 Seamless E-Commerce Flow:** Integrated Razorpay checkout triggers the exact millisecond a pricing consensus is reached.
*   **🔐 Secure Local Storage:** Uses Android Room DB for local chat caching and Biometric App Lock for secure merchant access.

---

## 🏗️ Project Architecture & Structure

```text
# ==============================================================================
#                      LOCAL MERCHANT - PROJECT ARCHITECTURE
# ==============================================================================

Local-Merchant (Android)
├── Architecture Pattern : MVVM (Model-View-ViewModel) + Repository Pattern
├── UI Framework         : Jetpack Compose + Material 3
├── Real-Time Comm.      : WebSockets (OkHttp) & Coroutine Flows
├── Networking           : Retrofit 2 + OkHttp Interceptors + Gson
├── Local Storage        : Room Database + DataStore Preferences
├── Security & Auth      : AndroidX Biometric API
└── Payment Gateway      : Razorpay Checkout SDK

--------------------------------------------------------------------------------
                         HIGH-LEVEL DATA FLOW ARCHITECTURE
--------------------------------------------------------------------------------

+------------------------------------------------------------------------------+
|                               PRESENTATION LAYER                             |
|                                                                              |
|   [ Buyer Screens ]   <--->   [ Buyer ViewModels ]                           |
|   - Marketplace                - BuyerDashboardViewModel                     |
|   - Negotiation Chat           - ChatViewModel (WebSockets)                  |
|   - Biometric Checkout         - CheckoutViewModel                           |
|                                                                              |
|   [ Merchant Screens ]  <--->  [ Merchant ViewModels ]                       |
|   - Agent Setup                - MerchantViewModel                           |
|   - Dashboard Stats            - MerchantDashboardViewModel                  |
|   - Parameters & Rates         - MerchantProfileViewModel                    |
+------------------------------------------------------------------------------+
                                       |
                                       v
+------------------------------------------------------------------------------+
|                                DOMAIN / REPO LAYER                           |
|                                                                              |
|                         [ MarketplaceRepository ]                            |
|        Central Data Coordinator for REST APIs, WebSockets & Caching          |
+------------------------------------------------------------------------------+
                                  /    |    \
                                 /     |     \
                                v      v      v
+------------------------------------------------------------------------------+
|                                DATA SOURCE LAYER                             |
|                                                                              |
|  [ Remote Data Source ]     [ Local Data Source ]     [ Session / Security ] |
|  - GoBackendApi (Retrofit)   - AppDatabase (Room)      - SessionManager      |
|  - WebSockets (OkHttp)       - ChatDao                 - BiometricPrompt     |
|  - Razorpay Payment API      - MerchantDao             - Encrypted Settings  |
+------------------------------------------------------------------------------+

--------------------------------------------------------------------------------
                            PACKAGE STRUCTURE TREE
--------------------------------------------------------------------------------

app/src/main/java/com/example/local_merchant/
├── LocalMerchant.kt               # Application entry point & Database initializer
├── MainActivity.kt                # FragmentActivity, Payment Result Listener & Biometric host
├── BiometricAppLock.kt            # Biometric App Lock Security Wrapper
│
├── config/
│   └── AppConfig.kt               # Secure Environment Configs (Injected via Gradle)
│
├── data/                          # Data Layer
│   ├── local/                     # Room DB & Local Persistence
│   │   ├── AppDatabase.kt         # Room Database Singleton
│   │   ├── ChatDao.kt             # Chat Message Persistence DAO
│   │   ├── ChatEntity.kt          # Room Entity for Messages
│   │   ├── Sessionmanager.kt      # DataStore Preferences for User/Agent States
│   │   └── merchant/
│   │       ├── MerchantDao.kt     # Merchant Profile DAO
│   │       └── MerchantEntity.kt  # Room Entity for Merchant Profiles
│   │
│   ├── model/                     # Domain & UI Models
│   │   └── Model.kt               # Chat & Dashboard Stats Data Models
│   │
│   ├── remote/                    # Remote Network Layer
│   │   ├── GoBackendApi.kt        # Retrofit Interface & ApiClient Engine
│   │   └── NetworkModels.kt       # Request/Response DTOs & Razorpay Payload Specs
│   │
│   └── repository/                # Central Repository Pattern
│       └── MarketPlaceRepository.kt # Handles Cache, REST API, & Live Inboxes
│
├── dependency/
│   └── AppModule.kt               # DI Container & Custom ViewModelFactory
│
├── viewmodel/                     # Business Logic Layer
│   ├── ChatViewModel.kt           # Real-Time WebSocket Messaging & Live Inbox State
│   ├── CheckoutViewModel.kt       # Razorpay Order ID Generation & Checkout Flow
│   ├── buyer/                     # Buyer-Specific ViewModels
│   │   ├── BuyerDashBoardViewModel.kt
│   │   ├── BuyerInboxViewModel.kt
│   │   ├── BuyerProfileViewModel.kt
│   │   └── BuyerViewModel.kt
│   └── merchant/                  # Merchant-Specific ViewModels
│       ├── MerchantDashBoardViewModel.kt
│       ├── MerchantHistoryViewModel.kt
│       ├── MerchantProfileViewModel.kt
│       └── MerchantViewModel.kt
│
└── ui/                            # Jetpack Compose UI Layer
    ├── components/                # Reusable Design Components (Backgrounds, Cards)
    ├── theme/                     # Material3 Color Systems, Typography, & Themes
    ├── navigation/
    │   └── AppNavigation.kt       # Central NavHost Navigation Graph
    │
    ├── RoleSelection/             # App Mode Gateway (Buyer vs Merchant)
    │
    ├── Buyer/                     # Buyer Surface
    │   ├── BuyerDashBoard.kt      # Live Pro Search & Category Filtering
    │   ├── buyersetup.kt          # Buyer Registration
    │   ├── PaymentFinish.kt       # Post-Checkout Confirmation
    │   ├── chat/
    │   │   ├── BuyerInbox.kt      # Negotiation Threads Inbox
    │   │   └── NegotiationChat.kt # Interactive AI Price Negotiation Screen
    │   ├── checkout/
    │   │   └── BiometricCheckout.kt # Biometric Authorization & Razorpay Checkout
    │   └── profile/
    │       ├── BuyerProfile.kt    # User Account Overview
    │       ├── EditProfile.kt     # Dynamic Profile Updates
    │       └── OrderHistory.kt    # Completed Bookings & Receipts
    │
    └── merchant/                  # Merchant Surface
        ├── MerchantDashboard.kt   # Live Revenue Trends & Active Deals Chart
        ├── MerchantSetup.kt       # AI Agent Deployment Setup
        ├── chat/
        │   ├── ChatListScreen.kt  # Active Buyer Deal Conversations
        │   └── ChatDetailScreen.kt# Real-Time Merchant Chat Window
        └── profile/
            ├── MerchantProfileScreen.kt # Account Settings & AI Toggle
            └── internalui/
                ├── AgentParameterScreen.kt # Base Rates, Floor Limits & Upsell Rules
                └── DealHistory.kt          # Closed Deals Audit Trail
```

---

## 🚀 Download & Installation

The complete source code for both the native Android client and the Go backend can be downloaded directly from this GitHub repository.

### 📥 Clone the Repository
```bash
git clone [https://github.com/yourusername/Local-Merchant.git](https://github.com/yourusername/Local-Merchant.git)
cd Local-Merchant
```

### 1. Run the Go Backend (Linux/macOS/Windows)
Ensure you have [Go](https://go.dev/dl/) installed on your machine.
```bash
cd BACKEND
# Install dependencies
go mod tidy
# Set up your environment variables
export GROQ_API_KEY="your_api_key_here"
# Start the server
go run .
```

### 2. Run the Android App
1. Open the cloned `Local-Merchant` folder in **Android Studio**.
2. Navigate to `com.example.local_merchant.dependency.AppModule`.
3. Update the `BASE_URL` and `WS_URL` variables to point to your live Render deployment or local IP address.
4. Sync the Gradle files and hit **Run** to install the app on your physical device or emulator.

---

## 🤝 Contributing
Built as a submission for the Track 1 Buildathon. Feel free to fork the repository, submit pull requests, or open issues to discuss new features!# 🛠️ Local-Merchant

**An AI-Powered Service Marketplace for Local Tradeworkers**  

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Go](https://img.shields.io/badge/Go-00ADD8?style=for-the-badge&logo=go&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)
![Render](https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white)

---

## ✨ Core Features

*   **🤖 Autonomous Live Negotiation:** Dual AI agents (powered by the Groq API) handle dynamic price negotiations based on strict floor-rate parameters set by the tradeworker.
*   **⚡ Ultra-Low Latency Chat:** A high-concurrency Go backend utilizes Gorilla WebSockets to stream negotiation packets instantly to the Android client.
*   **💳 Seamless E-Commerce Flow:** Integrated Razorpay checkout triggers the exact millisecond a pricing consensus is reached.
*   **🔐 Secure Local Storage:** Uses Android Room DB for local chat caching and Biometric App Lock for secure merchant access.

---

## 🏗️ Project Architecture & Structure

```text
# ==============================================================================
#                      LOCAL MERCHANT - PROJECT ARCHITECTURE
# ==============================================================================

Local-Merchant (Android)
├── Architecture Pattern : MVVM (Model-View-ViewModel) + Repository Pattern
├── UI Framework         : Jetpack Compose + Material 3
├── Real-Time Comm.      : WebSockets (OkHttp) & Coroutine Flows
├── Networking           : Retrofit 2 + OkHttp Interceptors + Gson
├── Local Storage        : Room Database + DataStore Preferences
├── Security & Auth      : AndroidX Biometric API
└── Payment Gateway      : Razorpay Checkout SDK

--------------------------------------------------------------------------------
                         HIGH-LEVEL DATA FLOW ARCHITECTURE
--------------------------------------------------------------------------------

+------------------------------------------------------------------------------+
|                               PRESENTATION LAYER                             |
|                                                                              |
|   [ Buyer Screens ]   <--->   [ Buyer ViewModels ]                           |
|   - Marketplace                - BuyerDashboardViewModel                     |
|   - Negotiation Chat           - ChatViewModel (WebSockets)                  |
|   - Biometric Checkout         - CheckoutViewModel                           |
|                                                                              |
|   [ Merchant Screens ]  <--->  [ Merchant ViewModels ]                       |
|   - Agent Setup                - MerchantViewModel                           |
|   - Dashboard Stats            - MerchantDashboardViewModel                  |
|   - Parameters & Rates         - MerchantProfileViewModel                    |
+------------------------------------------------------------------------------+
                                       |
                                       v
+------------------------------------------------------------------------------+
|                                DOMAIN / REPO LAYER                           |
|                                                                              |
|                         [ MarketplaceRepository ]                            |
|        Central Data Coordinator for REST APIs, WebSockets & Caching          |
+------------------------------------------------------------------------------+
                                  /    |    \
                                 /     |     \
                                v      v      v
+------------------------------------------------------------------------------+
|                                DATA SOURCE LAYER                             |
|                                                                              |
|  [ Remote Data Source ]     [ Local Data Source ]     [ Session / Security ] |
|  - GoBackendApi (Retrofit)   - AppDatabase (Room)      - SessionManager      |
|  - WebSockets (OkHttp)       - ChatDao                 - BiometricPrompt     |
|  - Razorpay Payment API      - MerchantDao             - Encrypted Settings  |
+------------------------------------------------------------------------------+

--------------------------------------------------------------------------------
                            PACKAGE STRUCTURE TREE
--------------------------------------------------------------------------------

app/src/main/java/com/example/local_merchant/
├── LocalMerchant.kt               # Application entry point & Database initializer
├── MainActivity.kt                # FragmentActivity, Payment Result Listener & Biometric host
├── BiometricAppLock.kt            # Biometric App Lock Security Wrapper
│
├── config/
│   └── AppConfig.kt               # Secure Environment Configs (Injected via Gradle)
│
├── data/                          # Data Layer
│   ├── local/                     # Room DB & Local Persistence
│   │   ├── AppDatabase.kt         # Room Database Singleton
│   │   ├── ChatDao.kt             # Chat Message Persistence DAO
│   │   ├── ChatEntity.kt          # Room Entity for Messages
│   │   ├── Sessionmanager.kt      # DataStore Preferences for User/Agent States
│   │   └── merchant/
│   │       ├── MerchantDao.kt     # Merchant Profile DAO
│   │       └── MerchantEntity.kt  # Room Entity for Merchant Profiles
│   │
│   ├── model/                     # Domain & UI Models
│   │   └── Model.kt               # Chat & Dashboard Stats Data Models
│   │
│   ├── remote/                    # Remote Network Layer
│   │   ├── GoBackendApi.kt        # Retrofit Interface & ApiClient Engine
│   │   └── NetworkModels.kt       # Request/Response DTOs & Razorpay Payload Specs
│   │
│   └── repository/                # Central Repository Pattern
│       └── MarketPlaceRepository.kt # Handles Cache, REST API, & Live Inboxes
│
├── dependency/
│   └── AppModule.kt               # DI Container & Custom ViewModelFactory
│
├── viewmodel/                     # Business Logic Layer
│   ├── ChatViewModel.kt           # Real-Time WebSocket Messaging & Live Inbox State
│   ├── CheckoutViewModel.kt       # Razorpay Order ID Generation & Checkout Flow
│   ├── buyer/                     # Buyer-Specific ViewModels
│   │   ├── BuyerDashBoardViewModel.kt
│   │   ├── BuyerInboxViewModel.kt
│   │   ├── BuyerProfileViewModel.kt
│   │   └── BuyerViewModel.kt
│   └── merchant/                  # Merchant-Specific ViewModels
│       ├── MerchantDashBoardViewModel.kt
│       ├── MerchantHistoryViewModel.kt
│       ├── MerchantProfileViewModel.kt
│       └── MerchantViewModel.kt
│
└── ui/                            # Jetpack Compose UI Layer
    ├── components/                # Reusable Design Components (Backgrounds, Cards)
    ├── theme/                     # Material3 Color Systems, Typography, & Themes
    ├── navigation/
    │   └── AppNavigation.kt       # Central NavHost Navigation Graph
    │
    ├── RoleSelection/             # App Mode Gateway (Buyer vs Merchant)
    │
    ├── Buyer/                     # Buyer Surface
    │   ├── BuyerDashBoard.kt      # Live Pro Search & Category Filtering
    │   ├── buyersetup.kt          # Buyer Registration
    │   ├── PaymentFinish.kt       # Post-Checkout Confirmation
    │   ├── chat/
    │   │   ├── BuyerInbox.kt      # Negotiation Threads Inbox
    │   │   └── NegotiationChat.kt # Interactive AI Price Negotiation Screen
    │   ├── checkout/
    │   │   └── BiometricCheckout.kt # Biometric Authorization & Razorpay Checkout
    │   └── profile/
    │       ├── BuyerProfile.kt    # User Account Overview
    │       ├── EditProfile.kt     # Dynamic Profile Updates
    │       └── OrderHistory.kt    # Completed Bookings & Receipts
    │
    └── merchant/                  # Merchant Surface
        ├── MerchantDashboard.kt   # Live Revenue Trends & Active Deals Chart
        ├── MerchantSetup.kt       # AI Agent Deployment Setup
        ├── chat/
        │   ├── ChatListScreen.kt  # Active Buyer Deal Conversations
        │   └── ChatDetailScreen.kt# Real-Time Merchant Chat Window
        └── profile/
            ├── MerchantProfileScreen.kt # Account Settings & AI Toggle
            └── internalui/
                ├── AgentParameterScreen.kt # Base Rates, Floor Limits & Upsell Rules
                └── DealHistory.kt          # Closed Deals Audit Trail
```

---

## 🚀 Download & Installation

The complete source code for both the native Android client and the Go backend can be downloaded directly from this GitHub repository.

### 📥 Clone the Repository
```bash
git clone [https://github.com/yourusername/Local-Merchant.git](https://github.com/yourusername/Local-Merchant.git)
cd Local-Merchant
```

### 1. Run the Go Backend (Linux/macOS/Windows)
Ensure you have [Go](https://go.dev/dl/) installed on your machine.
```bash
cd BACKEND
# Install dependencies
go mod tidy
# Set up your environment variables
export GROQ_API_KEY="your_api_key_here"
# Start the server
go run .
```

### 2. Run the Android App
1. Open the cloned `Local-Merchant` folder in **Android Studio**.
2. Navigate to `com.example.local_merchant.dependency.AppModule`.
3. Update the `BASE_URL` and `WS_URL` variables to point to your live Render deployment or local IP address.
4. Sync the Gradle files and hit **Run** to install the app on your physical device or emulator.

---

## 🤝 Contributing
Feel free to fork the repository, submit pull requests, or open issues to discuss new features!
