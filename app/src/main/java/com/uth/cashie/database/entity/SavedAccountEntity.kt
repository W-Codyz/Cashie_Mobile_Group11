package com.uth.cashie.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng saved_accounts – lưu tài khoản đã đăng nhập trên thiết bị.
 * Dùng để hiển thị gợi ý đăng nhập nhanh và xử lý "Nhớ thông tin đăng nhập".
 * Không lưu password ở bảng này.
 */
@Entity(
    tableName = "saved_accounts",
    foreignKeys = [
        ForeignKey(
            entity        = UserEntity::class,
            parentColumns = ["id"],
            childColumns  = ["user_id"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])]
)
data class SavedAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "full_name")
    val fullName: String? = null,

    @ColumnInfo(name = "avatar_path")
    val avatarPath: String? = null,

    /** 1 = đã tích 'Nhớ đăng nhập', tự động vào app khi khởi động
     *  0 = không nhớ, nhưng vẫn giữ bản ghi để hiển thị gợi ý */
    @ColumnInfo(name = "remember_login")
    val rememberLogin: Int = 0,

    @ColumnInfo(name = "last_login_at")
    val lastLoginAt: Long = System.currentTimeMillis()
)
