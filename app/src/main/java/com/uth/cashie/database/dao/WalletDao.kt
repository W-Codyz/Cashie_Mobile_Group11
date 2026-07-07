package com.uth.cashie.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.uth.cashie.database.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Insert
    suspend fun insert(wallet: WalletEntity): Long

    @Insert
    suspend fun insertAll(wallets: List<WalletEntity>)

    @Query("SELECT * FROM wallets WHERE user_id = :userId ORDER BY is_default DESC, name ASC")
    fun getAllByUser(userId: Long): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE user_id = :userId ORDER BY is_default DESC, name ASC")
    suspend fun getAllByUserOnce(userId: Long): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE id = :id AND user_id = :userId LIMIT 1")
    suspend fun getById(id: Long, userId: Long): WalletEntity?
}
