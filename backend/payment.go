package main

import (
	"encoding/json"
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
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req OrderRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		json.NewEncoder(w).Encode(OrderResponse{Error: "Invalid request payload"})
		return
	}

	key := os.Getenv("RAZORPAY_KEY_ID")
	secret := os.Getenv("RAZORPAY_KEY_SECRET")
	client := razorpay.NewClient(key, secret)

	data := map[string]interface{}{
		"amount":   req.FinalPrice * 100,
		"currency": "INR",
		"receipt":  "receipt_AI_negotiation",
	}

	body, err := client.Order.Create(data, nil)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(OrderResponse{Error: "Failed to generate Razorpay order"})
		return
	}

	orderID := body["id"].(string)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(OrderResponse{OrderID: orderID})
}