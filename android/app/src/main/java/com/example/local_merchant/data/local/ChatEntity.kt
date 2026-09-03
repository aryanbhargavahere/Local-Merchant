package com.example.local_merchant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_history")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchantId: String,
    val message: String,
    val sender: String, // "USER" or "AI" or "SYSTEM"
    var timestamp: Long = System.currentTimeMillis()
)