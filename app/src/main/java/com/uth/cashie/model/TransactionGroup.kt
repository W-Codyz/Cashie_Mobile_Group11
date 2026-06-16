package com.uth.cashie.model

data class TransactionGroup(
    val dateLabel: String,
    val dayNet: Long,
    val transactions: List<Transaction>
)
