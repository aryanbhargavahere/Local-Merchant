# Local-Merchant: AI Negotiation Platform

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white) ![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white) ![Go](https://img.shields.io/badge/go-%2300ADD8.svg?style=for-the-badge&logo=go&logoColor=white) ![Razorpay](https://img.shields.io/badge/Razorpay-02042B?style=for-the-badge&logo=razorpay&logoColor=3395FF)

Local-Merchant is a comprehensive full-stack marketplace designed to connect local buyers and sellers through autonomous AI negotiation agents. By leveraging a high-performance native Android client alongside a concurrent Go backend, the platform successfully handles real-time bidding, live chat synchronization, and secure digital checkout integrations.

## Core Features

* **Autonomous AI Agents:** LLM-powered buyer and seller agents negotiate dynamic pricing based on strict merchant floor rates and user budgets.
* **Real-Time WebSockets:** Instant, bi-directional communication ensures live tracking and continuous updates of all ongoing negotiations.
* **Secure Checkout:** An integrated Razorpay gateway handles immediate, secure transaction processing once a deal reaches a mutual agreement.

## Tech Stack

* **Client Architecture:** Native Android utilizing Kotlin, Jetpack Compose, Retrofit, and StateFlow for reactive UI updates.
* **Backend Infrastructure:** Built with Go using the standard `net/http` library, Gorilla WebSockets, and Mutex locks for memory state management.
* **Third-Party Integrations:** Razorpay-Go SDK for payments and external LLM APIs for the core negotiation logic.
* **Security Posture:** Implements strict `.env` backend secrets and dynamic `local.properties` configuration for safe Android IP injection.

## Getting Started

* **Backend Configuration:** Create a `.env` file in the backend directory containing `PORT`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, and your chosen LLM API keys.
* **Run Server:** Execute `go run main.go` to initialize the backend, which automatically binds to your local network interface.
* **Android Configuration:** Add your specific machine IP as `DEV_HOST_IP=your_local_ip` inside the `local.properties` file in Android Studio.
* **Launch Client:** Sync your Gradle files, build the APK, and run the application on an emulator or physical testing device.
