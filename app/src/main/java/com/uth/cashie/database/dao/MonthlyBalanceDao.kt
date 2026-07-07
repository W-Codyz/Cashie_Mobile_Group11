package com.uth.cashie.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uth.cashie.database.entity.MonthlyBalanceEntity

@Dao
interface MonthlyBalanceDao {

    @Query("SELECT * FROM monthly_balances WHERE user_id = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getByMonthYear(userId: Long, month: Int, year: Int): MonthlyBalanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MonthlyBalanceEntity)
}
