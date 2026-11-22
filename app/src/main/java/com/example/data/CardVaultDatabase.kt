package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CardEntity::class, VariantEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(CardVaultConverters::class)
abstract class CardVaultDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun variantDao(): VariantDao

    companion object {
        @Volatile
        private var instance: CardVaultDatabase? = null

        fun getInstance(context: Context): CardVaultDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CardVaultDatabase::class.java,
                    "card_vault.db"
                ).build().also { instance = it }
            }
        }
    }
}
