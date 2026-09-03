package com.example.local_merchant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.local_merchant.data.local.merchant.MerchantDao
import com.example.local_merchant.data.local.merchant.MerchantEntity

@Database(
    entities = [ChatEntity::class, MerchantEntity::class], // Added MerchantEntity
    version = 2, // Bumped version to 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun merchantDao(): MerchantDao // Added MerchantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autotrade_db"
                )
                    // Prevents a crash when changing database versions during prototyping
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}