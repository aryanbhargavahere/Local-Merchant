package main

import (
    "bytes"
    "encoding/json"
    "fmt"
    "io"
    "net/http"
    "os"
)

// CallGroq sends structured requests to Groq's OpenAI-compatible completions API
func CallGroq(systemPrompt, userPrompt string) (string, error) {
    apiKey := os.Getenv("GROQ_API_KEY")
    if apiKey == "" {
        return "", fmt.Errorf("GROQ_API_KEY is not configured")
    }

    payload := GroqRequestPayload{
        Model: "qwen/qwen3.8-27b",
        Messages: []GroqMessage{
            {Role: "system", Content: systemPrompt},
            {Role: "user", Content: userPrompt},
        },
        ResponseFormat: map[string]string{"type": "json_object"},
        Temperature:    0.1,
    }

    jsonData, err := json.Marshal(payload)
    if err != nil {
        return "", err
    }

    req, err := http.NewRequest("POST", "https://api.groq.com/openai/v1/chat/completions", bytes.NewBuffer(jsonData))
    if err != nil {
        return "", err
    }

    req.Header.Set("Authorization", "Bearer "+apiKey)
    req.Header.Set("Content-Type", "application/json")

    client := &http.Client{}
    resp, err := client.Do(req)
    if err != nil {
        return "", err
    }
    defer resp.Body.Close()

    if resp.StatusCode != http.StatusOK {
        bodyBytes, _ := io.ReadAll(resp.Body)
        return "", fmt.Errorf("groq API error status: %d, body: %s", resp.StatusCode, string(bodyBytes))
    }

    var groqResp GroqResponsePayload
    if err := json.NewDecoder(resp.Body).Decode(&groqResp); err != nil {
        return "", err
    }

    if len(groqResp.Choices) == 0 {
        return "", fmt.Errorf("no response choices returned from Groq")
    }

    rawContent := groqResp.Choices[0].Message.Content
    return rawContent, nil
}

// RunBuyerAgent dynamically analyzes the live catalog and forms an initial bid
func RunBuyerAgent(userPrompt string, activeCatalog []Merchant) (*BuyerAgentOutput, error) {
    catalogBytes, err := json.Marshal(activeCatalog)
    if err != nil {
        return nil, err
    }

    systemPrompt := fmt.Sprintf(`
You are an autonomous procurement agent. Match the buyer's request against the current active catalog.

ACTIVE CATALOG:
%s

RULES:
1. Select the best matching merchant ID based on the requested service.
2. Extract the buyer's maximum budget and propose an initial starting bid.
3. If no matching merchant is found, set "merchant_id" to "".

OUTPUT FORMAT (JSON only):
{
  "merchant_id": "string",
  "service": "string",
  "buyer_max_budget": number,
  "initial_bid": number,
  "reasoning": "brief explanation"
}
`, string(catalogBytes))

    rawJSON, err := CallGroq(systemPrompt, fmt.Sprintf("User Request: %s", userPrompt))
    if err != nil {
        return nil, err
    }

    var output BuyerAgentOutput
    if err := json.Unmarshal([]byte(rawJSON), &output); err != nil {
        return nil, err
    }

    return &output, nil
}

// RunSellerAgent negotiates dynamically on behalf of the selected merchant
func RunSellerAgent(buyerInitialBid int, buyerPrompt string, merchant Merchant) (*SellerAgentOutput, error) {
    systemPrompt := fmt.Sprintf(`
    You are the autonomous sales agent representing merchant '%s'.

    MERCHANT PARAMETERS:
    - Base Rate: ₹%d (The ideal target price)
    - Absolute Floor Limit: ₹%d (Your strict bottom-line negotiation limit)
    - Upsell / Special Rules: %s

    RULES:
    1. If the buyer's offer is >= Base Rate, ACCEPT the offer.
    2. If the buyer's offer is between the Floor Limit and Base Rate, you can COUNTER to negotiate a middle ground, or ACCEPT if it is fair.
    3. THE ULTIMATUM RULE: If the buyer's offer goes strictly below your Floor Limit (₹%d), you MUST COUNTER with exactly your Floor Limit (₹%d) and explicitly state in your reasoning that this is your final price—they can either take it or leave it.
    4. If the buyer continues to push below the Floor Limit after the ultimatum has been given, you MUST choose REJECT and walk away.

    OUTPUT FORMAT (JSON only):
    {
    "decision": "ACCEPT" | "COUNTER" | "REJECT",
    "agreed_price": number,
    "reasoning": "your direct message/response to the buyer"
    }
    `, merchant.Name, merchant.BaseRate, merchant.FloorRate, merchant.UpsellRules, merchant.FloorRate, merchant.FloorRate)

    rawJSON, err := CallGroq(systemPrompt, fmt.Sprintf("Buyer context: '%s'. Initial Bid: ₹%d.", buyerPrompt, buyerInitialBid))
    if err != nil {
        return nil, err
    }

    var output SellerAgentOutput
    if err := json.Unmarshal([]byte(rawJSON), &output); err != nil {
        return nil, err
    }

    return &output, nil
}

// Dynamic state output for Buyer Counter
type BuyerCounterOutput struct {
    Decision  string `json:"decision"`
    NewBid    int    `json:"new_bid"`
    Reasoning string `json:"reasoning"`
}

// RunBuyerCounter evaluates the seller's counter-offer
func RunBuyerCounter(sellerOffer int, maxBudget int) (*BuyerCounterOutput, error) {
    systemPrompt := fmt.Sprintf(`
You are an autonomous procurement agent. Your strict maximum budget is ₹%d.
The seller has countered with an offer of ₹%d.

RULES:
1. If the seller's offer is <= your max budget, ACCEPT at their offer price.
2. If their offer > max budget, COUNTER with a middle-ground price that is strictly <= your max budget.
3. If they are way too high and you cannot compromise, REJECT.

OUTPUT FORMAT (JSON only):
{
  "decision": "ACCEPT" | "COUNTER" | "REJECT",
  "new_bid": number,
  "reasoning": "brief explanation"
}
`, maxBudget, sellerOffer)

    rawJSON, err := CallGroq(systemPrompt, "Evaluate the seller's counter-offer.")
    if err != nil {
        return nil, err
    }

    var output BuyerCounterOutput
    if err := json.Unmarshal([]byte(rawJSON), &output); err != nil {
        return nil, err
    }
    return &output, nil
}