package com.example.local_merchant.data.local.merchant

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantDao {

    // Returns a live stream of the profile. If the DB changes, the UI updates instantly.
    @Query("SELECT * FROM merchant_profile WHERE merchantId = :merchantId LIMIT 1")
    fun getMerchantProfileFlow(merchantId: String): Flow<MerchantEntity?>

    // A direct one-time read (used during background syncs in the Repository)
    @Query("SELECT * FROM merchant_profile WHERE merchantId = :merchantId LIMIT 1")
    suspend fun getMerchantProfileDirect(merchantId: String): MerchantEntity?

    // Inserts a new profile or overwrites the existing one if the ID matches
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: MerchantEntity)

    // Only updates the biometric flag without touching the rest of the profile
    @Query("UPDATE merchant_profile SET isBiometricsEnabled = :isEnabled WHERE merchantId = :merchantId")
    suspend fun updateBiometricFlag(merchantId: String, isEnabled: Boolean)
}