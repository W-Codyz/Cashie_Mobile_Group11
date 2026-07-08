package com.uth.cashie.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng categories – lưu danh mục thu và chi.
 * Bao gồm danh mục mặc định (is_default=1) và danh mục tự tạo (is_default=0).
 */
@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity        = UserEntity::class,
            parentColumns = ["id"],
            childColumns  = ["user_id"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id", "type"])]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "name_en")
    val nameEn: String? = null,

    /** Emoji đại diện cho danh mục, VD: 🍜, 💰 */
    @ColumnInfo(name = "icon_emoji")
    val iconEmoji: String? = null,

    /** 'income' (thu) hoặc 'expense' (chi) – phải khớp với type trong transactions */
    @ColumnInfo(name = "type")
    val type: String,

    /** Màu hiển thị (mã hex), VD: #FF8C00 */
    @ColumnInfo(name = "color")
    val color: String? = null,

    /** 1 = danh mục có sẵn do app tạo, 0 = do user tạo */
    @ColumnInfo(name = "is_default")
    val isDefault: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
