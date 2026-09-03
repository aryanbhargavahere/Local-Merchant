package com.example.local_merchant.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create the DataStore singleton safely
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "merchant_session")

class SessionManager(private val context: Context) {

    companion object {
        val MERCHANT_ID_KEY = stringPreferencesKey("merchant_id")
        val MERCHANT_NAME_KEY = stringPreferencesKey("merchant_name")
        val MERCHANT_PHONE_KEY = stringPreferencesKey("merchant_phone")

        val BUYER_ID_KEY = stringPreferencesKey("buyer_id")
        val BUYER_NAME_KEY = stringPreferencesKey("buyer_name")
        val BUYER_PHONE_KEY = stringPreferencesKey("buyer_phone")

        // 🛑 ADDED: Keys for Agent Parameters
        val BASE_RATE_KEY = intPreferencesKey("base_rate")
        val FLOOR_RATE_KEY = intPreferencesKey("floor_rate")
    }

    // --- MERCHANT LOGIC ---
    val merchantIdFlow: Flow<String?> = context.dataStore.data.map { it[MERCHANT_ID_KEY] }
    val merchantNameFlow: Flow<String?> = context.dataStore.data.map { it[MERCHANT_NAME_KEY] }
    val merchantPhoneFlow: Flow<String?> = context.dataStore.data.map { it[MERCHANT_PHONE_KEY] }

    suspend fun saveMerchantDetails(id: String, name: String, phone: String) {
        context.dataStore.edit { preferences ->
            preferences[MERCHANT_ID_KEY] = id
            preferences[MERCHANT_NAME_KEY] = name
            preferences[MERCHANT_PHONE_KEY] = phone
        }
    }

    // --- AGENT RATES LOGIC ---
    // Defaults to 1000 and 800 if not set
    val baseRateFlow: Flow<Int> = context.dataStore.data.map { it[BASE_RATE_KEY] ?: 1000 }
    val floorRateFlow: Flow<Int> = context.dataStore.data.map { it[FLOOR_RATE_KEY] ?: 800 }

    suspend fun saveAgentRates(base: Int, floor: Int) {
        context.dataStore.edit { preferences ->
            preferences[BASE_RATE_KEY] = base
            preferences[FLOOR_RATE_KEY] = floor
        }
    }

    // --- BUYER LOGIC ---
    val buyerIdFlow: Flow<String?> = context.dataStore.data.map { it[BUYER_ID_KEY] }
    val buyerNameFlow: Flow<String?> = context.dataStore.data.map { it[BUYER_NAME_KEY] }
    val buyerPhoneFlow: Flow<String?> = context.dataStore.data.map { it[BUYER_PHONE_KEY] }

    suspend fun saveBuyerDetails(id: String, name: String, phone: String) {
        context.dataStore.edit { preferences ->
            preferences[BUYER_ID_KEY] = id
            preferences[BUYER_NAME_KEY] = name
            preferences[BUYER_PHONE_KEY] = phone
        }
    }

    // --- SESSION CONTROL ---
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}