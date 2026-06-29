package com.uth.cashie.model

data class Transaction(
    val id: Int,
    val title: String,
    val category: String,
    val emoji: String,
    val iconColorHex: String,
    val amount: Long,
    val isIncome: Boolean,
    val time: String,
    /** Ngày giao dịch dạng "yyyy-MM-dd", mặc định rỗng */
    val date: String = ""
)
