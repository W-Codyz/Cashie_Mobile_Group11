package com.uth.cashie.category.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val id: Int,
    val name: String,
    val icon: Int,
    val color: Int = android.R.color.transparent,
    val emoji: String = "",
    val isDefault: Boolean = false
) : Parcelable