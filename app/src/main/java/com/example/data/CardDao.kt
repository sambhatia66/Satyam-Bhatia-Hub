package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY createdAt DESC, id DESC")
    fun observeAllCards(): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Query("DELETE FROM cards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: Long)
}

@Dao
interface VariantDao {
    @Query("SELECT * FROM variants ORDER BY isCustom DESC, createdAt DESC, displayName ASC")
    fun observeAllVariants(): Flow<List<VariantEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVariantIfAbsent(variant: VariantEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVariantsIfAbsent(variants: List<VariantEntity>)

    @Query("SELECT * FROM variants WHERE normalizedKey = :key LIMIT 1")
    suspend fun findByKey(key: String): VariantEntity?
}
