package main

import (
	"fmt"
	"sync"
)

// AIMemory safely encapsulates the chat history map and its mutex
type AIMemory struct {
	bank  map[string][]GroqMessage
	mutex sync.RWMutex
}

// Global instance of the memory bank
var aiMemory = AIMemory{
	bank: make(map[string][]GroqMessage),
}

// GetHistory fetches the rolling history. If it's a new chat, it initializes and SAVES the system prompt.
func (m *AIMemory) GetHistory(conversationID string, systemPrompt string) []GroqMessage {
	m.mutex.Lock()
	defer m.mutex.Unlock()

	history, exists := m.bank[conversationID]
	if !exists {
		initialHistory := []GroqMessage{{Role: "system", Content: systemPrompt}}
		m.bank[conversationID] = initialHistory
		fmt.Printf("🧠 [Memory] Initialized new AI context for room: %s\n", conversationID)
		return m.copySlice(initialHistory)
	}

	return m.copySlice(history)
}

// SaveMessage appends a new message and cleanly prunes the context window
func (m *AIMemory) SaveMessage(conversationID string, role string, content string) []GroqMessage {
	m.mutex.Lock()
	defer m.mutex.Unlock()

	m.bank[conversationID] = append(m.bank[conversationID], GroqMessage{
		Role:    role,
		Content: content,
	})

	historyLength := len(m.bank[conversationID])
	if historyLength > 11 {
		pruned := make([]GroqMessage, 0, 11)
		pruned = append(pruned, m.bank[conversationID][0])                    // Keep System Prompt
		pruned = append(pruned, m.bank[conversationID][historyLength-10:]...) // Get last 10
		m.bank[conversationID] = pruned
		fmt.Printf("✂️ [Memory] Pruned history for %s to prevent context overflow.\n", conversationID)
	} else {
		fmt.Printf("💾 [Memory] Saved '%s' message in %s (Total msgs: %d)\n", role, conversationID, historyLength)
	}

	return m.copySlice(m.bank[conversationID])
}

// Helper function to return a fresh copy of the slice for thread-safety
func (m *AIMemory) copySlice(original []GroqMessage) []GroqMessage {
	copied := make([]GroqMessage, len(original))
	copy(copied, original)
	return copied
}

// Optional: Clear memory after checkout is complete
func (m *AIMemory) ClearHistory(conversationID string) {
	m.mutex.Lock()
	defer m.mutex.Unlock()
	delete(m.bank, conversationID)
	fmt.Printf("🧹 [Memory] Wiped context for room: %s\n", conversationID)
}