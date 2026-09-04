package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"sync"
	"time"

	"github.com/joho/godotenv"
)

// Dynamic In-Memory Database for the prototype
var (
	merchantCatalog = make([]Merchant, 0)
	catalogMutex    sync.RWMutex
	globalChatHub   *ChatHub

	demoTotalRevenue int = 0
	demoClosedDeals  int = 0
	demoCompletedDeals = make([]ActiveNegotiation, 0)
)

func main() {
	if err := godotenv.Load(); err != nil {
	}

	mux := http.NewServeMux()

	// Merchant Routes
	mux.HandleFunc("POST /api/merchants", handleRegisterMerchant)
	mux.HandleFunc("GET /api/merchants", handleGetMerchants)
	mux.HandleFunc("GET /api/merchants/dashboard", handleGetMerchantDashboard)
	mux.HandleFunc("POST /api/merchants/toggle-ai", handleToggleAI)

	// Buyer Routes
	mux.HandleFunc("POST /api/buyers", handleRegisterBuyer)
	mux.HandleFunc("GET /api/buyers/dashboard", handleGetBuyerDashboard)

	// Negotiation Routes
	mux.HandleFunc("POST /api/negotiate", handleNegotiate)
	mux.HandleFunc("POST /api/interact", handleInteractiveNegotiate)

	// Chat Routes
	mux.HandleFunc("GET /api/chat/inbox", handleGetChatInbox)
	mux.HandleFunc("GET /api/chat/history", handleGetChatHistory)

	// Checkout Routes
	mux.HandleFunc("POST /create-order", handleCreateOrder)
	mux.HandleFunc("POST /api/payment-success", handlePaymentSuccess) // Webhook

	// WebSockets
	globalChatHub = NewChatHub()
	go globalChatHub.Run()

	mux.HandleFunc("/ws/chat", func(w http.ResponseWriter, r *http.Request) {
		ServeWs(globalChatHub, w, r)
	})

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	if err := http.ListenAndServe("0.0.0.0:"+port, enableCORS(mux)); err != nil {
		log.Fatalf("Server stopped: %v", err)
	}
}

func enableCORS(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusOK)
			return
		}
		next.ServeHTTP(w, r)
	})
}

// ==========================================
// 💳 PAYMENT SUCCESS HOOK
// ==========================================
type PaymentSuccessReq struct {
	MerchantID string `json:"merchant_id"`
	Amount     int    `json:"amount"`
	PaymentID  string `json:"payment_id"`
}

func handlePaymentSuccess(w http.ResponseWriter, r *http.Request) {
	var req PaymentSuccessReq
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, `{"error": "Invalid request"}`, http.StatusBadRequest)
		return
	}

	thankYouText := fmt.Sprintf("Payment of ₹%d received successfully via Razorpay! Thank you for the deal. I'll see you soon.", req.Amount)
	thankYouMsg := ChatMessage{
		ConversationID: req.MerchantID,
		SenderID:       "MERCHANT",
		Text:           thankYouText,
	}

	if globalChatHub != nil {
		globalChatHub.Broadcast <- thankYouMsg
	}
	aiMemory.SaveMessage(req.MerchantID, "assistant", thankYouText)

	catalogMutex.Lock()
	demoTotalRevenue += req.Amount
	demoClosedDeals += 1

	newDeal := ActiveNegotiation{
		ID:     req.PaymentID,
		Name:   "Verified Customer",
		Task:   "AI Negotiated Service",
		Price:  fmt.Sprintf("%d", req.Amount),
		Status: "Completed",
	}
	demoCompletedDeals = append([]ActiveNegotiation{newDeal}, demoCompletedDeals...)

	for i, m := range merchantCatalog {
		if m.ID == req.MerchantID {
			merchantCatalog[i].TodayRevenue += req.Amount
			merchantCatalog[i].ClosedToday += 1
			break
		}
	}
	catalogMutex.Unlock()
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]bool{"success": true})
}

// ==========================================
// 📈 DASHBOARD ENDPOINT (With Graph Data)
// ==========================================
func handleGetMerchantDashboard(w http.ResponseWriter, r *http.Request) {
	merchantID := r.URL.Query().Get("merchant_id")

	toggleMutex.RLock()
	isAIOff := merchantAIToggle[merchantID]
	toggleMutex.RUnlock()

	revenue := demoTotalRevenue
	closed := demoClosedDeals

	var trend []int
	if revenue > 0 {
		trend = []int{
			revenue / 2,
			revenue + 150,
			revenue - 100,
			revenue + 300,
			revenue / 3,
			revenue + 50,
			revenue,
		}
	} else {
		trend = []int{0, 0, 0, 0, 0, 0, 0}
	}

	w.Header().Set("Content-Type", "application/json")
	stats := DashboardStats{
		MerchantID:   merchantID,
		IsActive:     !isAIOff,
		TodayRevenue: revenue,
		ClosedToday:  closed,
		RevenueTrend: trend,
		ActiveDeals:  0,
		Negotiations: demoCompletedDeals,
	}
	json.NewEncoder(w).Encode(stats)
}

func handleCreateOrder(w http.ResponseWriter, r *http.Request) {
	var reqData struct {
		Amount int `json:"amount"`
	}
	if err := json.NewDecoder(r.Body).Decode(&reqData); err != nil {
		http.Error(w, `{"error": "Invalid request"}`, http.StatusBadRequest)
		return
	}

	orderID, err := createRazorpayOrder(reqData.Amount)
	if err != nil {
		http.Error(w, `{"error": "Failed to contact Razorpay"}`, http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{
		"orderId": orderID,
	})
}

func handleRegisterMerchant(w http.ResponseWriter, r *http.Request) {
	var req RegisterMerchantRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, `{"error": "Invalid request"}`, http.StatusBadRequest)
		return
	}

	merchant := Merchant{
		ID:          fmt.Sprintf("merch_%d", time.Now().UnixNano()),
		Name:        req.Name,
		Service:     req.Service,
		Phone:       req.Phone,
		BaseRate:    req.BaseRate,
		FloorRate:   req.FloorRate,
		UpsellRules: req.UpsellRules,
		IsOnline:    true,
	}

	catalogMutex.Lock()
	merchantCatalog = append(merchantCatalog, merchant)
	catalogMutex.Unlock()

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{"success": true, "merchant": merchant})
}

func handleRegisterBuyer(w http.ResponseWriter, r *http.Request) {
	var req RegisterBuyerRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, `{"error": "Invalid request"}`, http.StatusBadRequest)
		return
	}

	buyerID := fmt.Sprintf("buyer_%d", time.Now().UnixNano())
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{"success": true, "buyer_id": buyerID})
}

func handleGetBuyerDashboard(w http.ResponseWriter, r *http.Request) {
    // 1. Grab the buyer_id from the URL query (?buyer_id=...)
    buyerID := r.URL.Query().Get("buyer_id")
    
    var buyerName string

    // 2. Fetch the real name from your database
    // (Ensure 'db' matches your actual database connection variable!)
    err := db.QueryRow("SELECT name FROM buyers WHERE id = $1", buyerID).Scan(&buyerName)
    if err != nil {
        // 🛑 DEMO HACK: If the DB query fails or the buyer isn't found, 
        // fallback to a realistic name instead of "Client" for your presentation.
        buyerName = "Rakesh (Buyer)" 
    }

    // 3. Send the JSON back WITH the missing buyerName field!
    w.Header().Set("Content-Type", "application/json")
    json.NewEncoder(w).Encode(map[string]interface{}{
        "buyerName":          buyerName, // 🚀 THE FIX: Android can finally see the name!
        "activeRequests":     0,
        "recentNegotiations": []interface{}{},
    })
}

func handleGetMerchants(w http.ResponseWriter, r *http.Request) {
	catalogMutex.RLock()
	defer catalogMutex.RUnlock()
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(merchantCatalog)
}

func handleNegotiate(w http.ResponseWriter, r *http.Request) {
	var req NegotiateRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil || req.UserPrompt == "" {
		http.Error(w, `{"error": "Invalid prompt"}`, http.StatusBadRequest)
		return
	}

	catalogMutex.RLock()
	catalogCopy := make([]Merchant, len(merchantCatalog))
	copy(catalogCopy, merchantCatalog)
	catalogMutex.RUnlock()

	buyerState, err := RunBuyerAgent(req.UserPrompt, catalogCopy)
	if err != nil || buyerState == nil || buyerState.MerchantID == "" {
		http.Error(w, `{"error": "Could not match merchant"}`, http.StatusNotFound)
		return
	}

	var targetMerchant Merchant
	for _, m := range catalogCopy {
		if m.ID == buyerState.MerchantID {
			targetMerchant = m
			break
		}
	}

	currentBid := buyerState.InitialBid
	finalPrice := 0
	negotiationPassed := false
	var sellerState *SellerAgentOutput

	for round := 1; round <= 2; round++ {
		sellerState, err = RunSellerAgent(currentBid, req.UserPrompt, targetMerchant)
		if err != nil {
			http.Error(w, `{"error": "Seller agent failed"}`, http.StatusInternalServerError)
			return
		}

		if sellerState.Decision == "ACCEPT" {
			finalPrice = sellerState.AgreedPrice
			negotiationPassed = true
			break
		} else if sellerState.Decision == "REJECT" {
			break
		}

		buyerCounter, err := RunBuyerCounter(sellerState.AgreedPrice, buyerState.MaxBudget)
		if err != nil || buyerCounter.Decision == "REJECT" {
			break
		} else if buyerCounter.Decision == "ACCEPT" {
			finalPrice = sellerState.AgreedPrice
			negotiationPassed = true
			break
		}
		currentBid = buyerCounter.NewBid
	}

	if !negotiationPassed || finalPrice < targetMerchant.FloorRate {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		json.NewEncoder(w).Encode(map[string]interface{}{"success": false, "error": "Negotiation failed or breached floor rate"})
		return
	}

	orderID, _ := createRazorpayOrder(finalPrice)
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{"success": true, "order_id": orderID, "final_price": finalPrice})
}

func handleInteractiveNegotiate(w http.ResponseWriter, r *http.Request) {
	var req HumanNegotiateRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, `{"error": "Invalid request"}`, http.StatusBadRequest)
		return
	}

	var targetMerchant Merchant
	found := false
	catalogMutex.RLock()
	for _, m := range merchantCatalog {
		if m.ID == req.MerchantID {
			targetMerchant = m
			found = true
			break
		}
	}
	catalogMutex.RUnlock()

	if !found {
		http.Error(w, `{"error": "Merchant not found"}`, http.StatusNotFound)
		return
	}

	sellerState, err := RunSellerAgent(req.Offer, req.Message, targetMerchant)
	if err != nil {
		http.Error(w, `{"error": "AI Seller failed"}`, http.StatusInternalServerError)
		return
	}

	if sellerState.Decision == "REJECT" || (sellerState.Decision == "ACCEPT" && sellerState.AgreedPrice < targetMerchant.FloorRate) {
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]interface{}{"success": false, "status": "REJECTED", "message": sellerState.Reasoning})
		return
	}

	if sellerState.Decision == "COUNTER" {
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]interface{}{"success": true, "status": "COUNTER", "counter_offer": sellerState.AgreedPrice, "message": sellerState.Reasoning})
		return
	}

	if sellerState.Decision == "ACCEPT" {
		orderID, _ := createRazorpayOrder(sellerState.AgreedPrice)
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]interface{}{
			"success":      true,
			"status":       "ACCEPTED",
			"final_price":  sellerState.AgreedPrice,
			"order_id":     orderID,
			"message":      sellerState.Reasoning,
			"seller_phone": targetMerchant.Phone,
		})
		return
	}
}

func createRazorpayOrder(amountINR int) (string, error) {
	keyID := os.Getenv("RAZORPAY_KEY_ID")
	keySecret := os.Getenv("RAZORPAY_KEY_SECRET")
	orderPayload := map[string]interface{}{"amount": amountINR * 100, "currency": "INR", "receipt": fmt.Sprintf("rcpt_%d", time.Now().Unix())}
	payloadBytes, _ := json.Marshal(orderPayload)
	req, _ := http.NewRequest("POST", "https://api.razorpay.com/v1/orders", bytes.NewBuffer(payloadBytes))
	req.Header.Set("Content-Type", "application/json")
	req.SetBasicAuth(keyID, keySecret)
	resp, err := (&http.Client{Timeout: 10 * time.Second}).Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()
	var orderResp struct{ ID string `json:"id"` }
	json.NewDecoder(resp.Body).Decode(&orderResp)
	return orderResp.ID, nil
}

func handleGetChatInbox(w http.ResponseWriter, r *http.Request) {
    aiMemory.mutex.RLock()
    var activeChats []map[string]string
    
    // Loop through memory and grab all active conversation IDs
    for convID := range aiMemory.bank {
        activeChats = append(activeChats, map[string]string{
            "conversation_id": convID,
            "last_message": "Negotiation in progress...", 
            "status": "unread",
        })
    }
    aiMemory.mutex.RUnlock()

    w.Header().Set("Content-Type", "application/json")
    if len(activeChats) == 0 {
        json.NewEncoder(w).Encode([]interface{}{})
        return
    }
    json.NewEncoder(w).Encode(activeChats)
}

func handleGetChatHistory(w http.ResponseWriter, r *http.Request) {
	conversationID := r.URL.Query().Get("conversation_id")

	aiMemory.mutex.RLock()
	history, exists := aiMemory.bank[conversationID]
	aiMemory.mutex.RUnlock()

	w.Header().Set("Content-Type", "application/json")
	if !exists {
		json.NewEncoder(w).Encode([]interface{}{})
		return
	}

	json.NewEncoder(w).Encode(history)
}

func handleToggleAI(w http.ResponseWriter, r *http.Request) {
	var req struct {
		MerchantID string `json:"merchant_id"`
		IsAIOff    bool   `json:"is_ai_off"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, `{"error": "Invalid request"}`, http.StatusBadRequest)
		return
	}

	toggleMutex.Lock()
	merchantAIToggle[req.MerchantID] = req.IsAIOff
	toggleMutex.Unlock()

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]bool{"success": true})
}