package com.uth.cashie.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    val name: String,
    val type: String, // "cash" or "bank" or "other"
    val balance: Double = 0.0,
    @ColumnInfo(name = "icon_emoji") val iconEmoji: String = "💰",
    @ColumnInfo(name = "is_default") val isDefault: Int = 0
)
