package com.uth.cashie.category.model
data class Category(
    val id: Int,
    val name: String,
    val icon: Int,
    val color: Int = android.R.color.transparent   // mặc định trong suốt
)