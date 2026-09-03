package main

import (
	"sync"
)

type AIMemory struct {
	bank  map[string][]GroqMessage
	mutex sync.RWMutex
}

var aiMemory = AIMemory{
	bank: make(map[string][]GroqMessage),
}

func (m *AIMemory) GetHistory(conversationID string, systemPrompt string) []GroqMessage {
	m.mutex.Lock()
	defer m.mutex.Unlock()

	history, exists := m.bank[conversationID]
	if !exists {
		initialHistory := []GroqMessage{{Role: "system", Content: systemPrompt}}
		m.bank[conversationID] = initialHistory
		return m.copySlice(initialHistory)
	}

	return m.copySlice(history)
}

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
		pruned = append(pruned, m.bank[conversationID][0])
		pruned = append(pruned, m.bank[conversationID][historyLength-10:]...)
		m.bank[conversationID] = pruned
	}

	return m.copySlice(m.bank[conversationID])
}

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
}