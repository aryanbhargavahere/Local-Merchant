package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"

	razorpay "github.com/razorpay/razorpay-go"
)

// The incoming request payload from Android
type OrderRequest struct {
	FinalPrice int `json:"finalPrice"`
}

// The outgoing response payload to Android
type OrderResponse struct {
	OrderID string `json:"orderId"`
	Error   string `json:"error,omitempty"`
}

// The actual HTTP Handler
func CreateRazorpayOrderHandler(w http.ResponseWriter, r *http.Request) {
	// 1. Ensure it's a POST request
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	// 2. Parse the dynamic price requested by the Android app
	var req OrderRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		json.NewEncoder(w).Encode(OrderResponse{Error: "Invalid request payload"})
		return
	}

	// 3. Initialize Razorpay Client (Use environment variables for security)
	// Make sure to export RZP_TEST_KEY and RZP_TEST_SECRET in your terminal!
	key := os.Getenv("RAZORPAY_KEY_ID")
	secret := os.Getenv("RAZORPAY_KEY_SECRET")
	client := razorpay.NewClient(key, secret)

	// 4. Create the dynamic order
	data := map[string]interface{}{
		"amount":   req.FinalPrice * 100, // Converts to paise dynamically
		"currency": "INR",
		"receipt":  "receipt_AI_negotiation",
	}

	body, err := client.Order.Create(data, nil)
	if err != nil {
		fmt.Println("❌ Error creating Razorpay Order:", err)
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(OrderResponse{Error: "Failed to generate Razorpay order"})
		return
	}

	// 5. Extract the Order ID and send it back to Jetpack Compose
	orderID := body["id"].(string)
	fmt.Println("✅ Successfully generated Razorpay Order:", orderID)
	
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(OrderResponse{OrderID: orderID})
}