package com.uth.cashie.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_balances",
    indices = [Index(value = ["user_id", "month", "year"], unique = true)]
)
data class MonthlyBalanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "month") val month: Int,
    @ColumnInfo(name = "year") val year: Int,
    @ColumnInfo(name = "cash_balance") val cashBalance: Long = 0L,
    @ColumnInfo(name = "account_balance") val accountBalance: Long = 0L,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
