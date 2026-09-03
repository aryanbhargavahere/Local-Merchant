package com.example.local_merchant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert
    suspend fun insertMessage(chat: ChatEntity)

    // Returns a Flow so the UI updates instantly when a new message is inserted
    @Query("SELECT * FROM chat_history WHERE merchantId = :merchantId ORDER BY timestamp ASC")
    fun getChatHistory(merchantId: String): Flow<List<ChatEntity>>

    @Query("DELETE FROM chat_history WHERE merchantId = :merchantId")
    suspend fun clearHistory(merchantId: String)

    @Query("SELECT * FROM chat_history")
    fun getAllChats(): Flow<List<ChatEntity>>
}