package main

type Merchant struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Service     string `json:"service"`
	Phone       string `json:"phone"`
	BaseRate    int    `json:"base_rate"`
	FloorRate   int    `json:"floor_rate"`
	UpsellRules string `json:"upsell_rules"`
	IsOnline    bool   `json:"isOnline"` // Used by Android UI to show the green badge
	TodayRevenue  int    `json:"today_revenue"`
	ClosedToday   int    `json:"closed_today"`
}

type RegisterMerchantRequest struct {
	Name        string `json:"name"`
	Service     string `json:"service"`
	Phone       string `json:"phone"`
	BaseRate    int    `json:"base_rate"`
	FloorRate   int    `json:"floor_rate"`
	UpsellRules string `json:"upsell_rules"`
}

type RegisterBuyerRequest struct {
	Name    string `json:"name"`
	Phone   string `json:"phone"`
	Address string `json:"address"`
}

type HumanNegotiateRequest struct {
	MerchantID   string `json:"merchant_id"`
	Offer        int    `json:"offer"`
	Message      string `json:"message"`
	BuyerPhone   string `json:"buyer_phone"`
	BuyerAddress string `json:"buyer_address"`
}

type NegotiateRequest struct {
	UserPrompt string `json:"prompt"`
}

type BuyerAgentOutput struct {
	MerchantID string `json:"merchant_id"`
	Service    string `json:"service"`
	MaxBudget  int    `json:"buyer_max_budget"`
	InitialBid int    `json:"initial_bid"`
	Reasoning  string `json:"reasoning"`
}

type SellerAgentOutput struct {
	Decision    string `json:"decision"`
	AgreedPrice int    `json:"agreed_price"`
	Reasoning   string `json:"reasoning"`
}

type GroqMessage struct {
	Role    string `json:"role"`
	Content string `json:"content"`
}

type GroqRequestPayload struct {
	Model          string            `json:"model"`
	Messages       []GroqMessage     `json:"messages"`
	ResponseFormat map[string]string `json:"response_format,omitempty"`
	Temperature    float64           `json:"temperature"`
}

type GroqResponsePayload struct {
	Choices []struct {
		Message struct {
			Content string `json:"content"`
		} `json:"message"`
	} `json:"choices"`
}

type ActiveNegotiation struct {
	ID      string `json:"id"`
	Initial string `json:"initial"`
	Name    string `json:"name"`
	Task    string `json:"task"`
	Price   string `json:"price"`
	Status  string `json:"status"`
}

type DashboardStats struct {
	MerchantID   string              `json:"merchant_id"`
	IsActive     bool                `json:"is_active"`
	TodayRevenue int                 `json:"today_revenue"`
	RevenueTrend []int               `json:"revenue_trend"`
	ActiveDeals  int                 `json:"active_deals"`
	ClosedToday  int                 `json:"closed_today"`
	Negotiations []ActiveNegotiation `json:"negotiations"`
}