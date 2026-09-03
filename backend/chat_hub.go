package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"sync"
	"time"

	"github.com/gorilla/websocket"
)

type ChatMessage struct {
	ConversationID string `json:"merchantId"`
	SenderID       string `json:"sender"`
	Text           string `json:"message"`
}

type Client struct {
	Hub            *ChatHub
	Conn           *websocket.Conn
	Send           chan ChatMessage
	ConversationID string
	UserID         string
}

type ChatHub struct {
	Rooms      map[string]map[*Client]bool
	Broadcast  chan ChatMessage
	Register   chan *Client
	Unregister chan *Client
	Mutex      sync.Mutex
}

var (
	// Track if the Merchant manually turned off their AI
	merchantAIToggle = make(map[string]bool)
	toggleMutex      sync.RWMutex
)

func NewChatHub() *ChatHub {
	return &ChatHub{
		Rooms:      make(map[string]map[*Client]bool),
		Broadcast:  make(chan ChatMessage),
		Register:   make(chan *Client),
		Unregister: make(chan *Client),
	}
}

func (h *ChatHub) Run() {
	for {
		select {
		case client := <-h.Register:
			h.Mutex.Lock()
			if h.Rooms[client.ConversationID] == nil {
				h.Rooms[client.ConversationID] = make(map[*Client]bool)
			}
			h.Rooms[client.ConversationID][client] = true
			h.Mutex.Unlock()
			fmt.Printf("🔌 User %s joined conversation %s\n", client.UserID, client.ConversationID)

		case client := <-h.Unregister:
			h.Mutex.Lock()
			if _, ok := h.Rooms[client.ConversationID][client]; ok {
				delete(h.Rooms[client.ConversationID], client)
				close(client.Send)
				if len(h.Rooms[client.ConversationID]) == 0 {
					delete(h.Rooms, client.ConversationID)
				}
			}
			h.Mutex.Unlock()
			fmt.Printf("🔌 User %s left conversation %s\n", client.UserID, client.ConversationID)

		case message := <-h.Broadcast:
			// 1. Broadcast message to the room securely
			h.Mutex.Lock()
			for client := range h.Rooms[message.ConversationID] {
				if client.UserID != message.SenderID {
					select {
					case client.Send <- message:
					default:
						close(client.Send)
						delete(h.Rooms[message.ConversationID], client)
					}
				}
			}
			h.Mutex.Unlock()

			// 2. Check if the AI Auto-Pilot is paused by the merchant
			toggleMutex.RLock()
			isAIOff := merchantAIToggle[message.ConversationID]
			toggleMutex.RUnlock()

			// 3. Trigger AI only if sender is buyer AND AI is active
			if isBuyer(message.SenderID) && !isAIOff {
				go h.TriggerMerchantAI(message)
			} else if isBuyer(message.SenderID) && isAIOff {
				fmt.Printf("⏸️ AI is manually paused for %s.\n", message.ConversationID)
			}
		}
	}
}

func (h *ChatHub) TriggerMerchantAI(incomingMsg ChatMessage) {
	catalogMutex.RLock()
	var targetMerchant *Merchant
	for _, m := range merchantCatalog {
		if m.ID == incomingMsg.ConversationID {
			targetMerchant = &m
			break
		}
	}
	catalogMutex.RUnlock()

	var aiText string

	if targetMerchant == nil {
		aiText = "Error: Merchant profile not found."
	} else {
		aiText = generateSmartAIResponse(incomingMsg.ConversationID, incomingMsg.Text, *targetMerchant)
	}

	aiResponse := ChatMessage{
		ConversationID: incomingMsg.ConversationID,
		SenderID:       "MERCHANT",
		Text:           aiText,
	}

	fmt.Printf("🤖 Smart AI Agent generated response for %s\n", incomingMsg.ConversationID)
	h.Broadcast <- aiResponse
}

func generateSmartAIResponse(conversationID string, userText string, m Merchant) string {
    apiKey := os.Getenv("GROQ_API_KEY")
    if apiKey == "" {
        return "I am offline. (Missing GROQ_API_KEY)"
    }

    // 1. Setup Prompt
    merchantPrompt := fmt.Sprintf(`You are %s, a professional providing %s. Your goal is to negotiate a service price with a customer and maximize profit.
STRICT RULES:
1. NEVER give DIY advice.
2. ACT NATURAL: Never break character or say you are an AI. 
3. PATIENCE: If the user hasn't stated their problem, ask what they need help with first.
4. ANCHOR PRICING: Once they state the problem, ALWAYS start by quoting a price 20%% to 30%% HIGHER than your standard Base Rate of ₹%d. Never reveal this base rate to the customer.
5. NEGOTIATION (2 ROUNDS MAX): 
   - Round 1: If they push back on your initial high price, make a small concession. 
   - Round 2: If they counter again, immediately offer your absolute Final Price (never below the floor rate of ₹%d) and firmly state it is your final offer.
6. CLOSING: When you both agree on a final number, say EXACTLY: "Great, I can do it for ₹[The Agreed Amount]. Please click the 'Accept Deal' button at the top of your screen."
7. CONTACT: Your phone number is %s. Give it out only after agreeing on the price.`, m.Name, m.Service, m.BaseRate, m.FloorRate, m.Phone)

    // 2. Save user message and get updated context
    aiMemory.GetHistory(conversationID, merchantPrompt) 
    aiMemory.SaveMessage(conversationID, "user", userText)
    currentHistory := aiMemory.GetHistory(conversationID, merchantPrompt)

    // 3. Setup Groq Request
    payload := GroqRequestPayload{
        Model:       "openai/gpt-oss-20b",
        Messages:    currentHistory,
        Temperature: 0.6, 
    }

    payloadBytes, _ := json.Marshal(payload)
    req, _ := http.NewRequest("POST", "https://api.groq.com/openai/v1/chat/completions", bytes.NewBuffer(payloadBytes))
    req.Header.Set("Authorization", "Bearer "+apiKey)
    req.Header.Set("Content-Type", "application/json")

    client := &http.Client{Timeout: 10 * time.Second}
    resp, err := client.Do(req)

    // 4. DEMO DAY FALLBACK: Triggers if API key is invalid or Groq goes down
    if err != nil || resp.StatusCode != 200 {
        // 🛑 DEBUG FIX: Print exact error to Ubuntu terminal
        if resp != nil {
            bodyBytes, _ := ioutil.ReadAll(resp.Body)
            fmt.Printf("⚠️ GROQ API ERROR [%d]: %s\n", resp.StatusCode, string(bodyBytes))
        } else if err != nil {
            fmt.Printf("⚠️ NETWORK ERROR: %v\n", err)
        }

        fmt.Println("⚠️ API failed, triggering Demo Day Fallback...")
        fallbackPrice := m.BaseRate + (m.BaseRate * 25 / 100)
        fallbackText := fmt.Sprintf("I can definitely fix that for you. Based on my rates, I can do it for ₹%d. Does that work?", fallbackPrice)
        
        // 🛑 MEMORY FIX: Save fallback to memory so it doesn't repeat!
        aiMemory.SaveMessage(conversationID, "assistant", fallbackText)
        return fallbackText
    }
    defer resp.Body.Close()

    var groqResp GroqResponsePayload
    bodyBytes, _ := ioutil.ReadAll(resp.Body)
    json.Unmarshal(bodyBytes, &groqResp)

    if len(groqResp.Choices) > 0 {
        aiReply := groqResp.Choices[0].Message.Content
        aiMemory.SaveMessage(conversationID, "assistant", aiReply)
        return aiReply
    }

    return "I didn't quite catch that."
}

func isBuyer(senderID string) bool {
    return senderID != "MERCHANT" && senderID != "AI"
}

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool { return true },
}

func ServeWs(hub *ChatHub, w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println(err)
		return
	}

	conversationID := r.URL.Query().Get("conversation_id")
	userID := r.URL.Query().Get("user_id")

	client := &Client{
		Hub:            hub,
		Conn:           conn,
		Send:           make(chan ChatMessage, 256),
		ConversationID: conversationID,
		UserID:         userID,
	}
	client.Hub.Register <- client

	go client.writePump()
	go client.readPump()
}

func (c *Client) readPump() {
	defer func() {
		c.Hub.Unregister <- c
		c.Conn.Close()
	}()
	for {
		_, messageData, err := c.Conn.ReadMessage()
		if err != nil {
			break
		}

		var incoming struct {
			ConversationID string `json:"merchantId"` // FIXED JSON TAG
			SenderID       string `json:"sender"`     // FIXED JSON TAG
			Text           string `json:"message"`    // FIXED JSON TAG
		}

		if err := json.Unmarshal(messageData, &incoming); err == nil {
			msg := ChatMessage{
				ConversationID: incoming.ConversationID,
				SenderID:       incoming.SenderID,
				Text:           incoming.Text,
			}
			fmt.Printf("💬 Message in %s from %s: %s\n", msg.ConversationID, msg.SenderID, msg.Text)
			c.Hub.Broadcast <- msg
		}
	}
}

func (c *Client) writePump() {
	defer c.Conn.Close()
	for {
		select {
		case message, ok := <-c.Send:
			if !ok {
				c.Conn.WriteMessage(websocket.CloseMessage, []byte{})
				return
			}
			c.Conn.WriteJSON(message)
		}
	}
}