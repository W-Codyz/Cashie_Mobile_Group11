package com.uth.cashie.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng users – lưu thông tin tài khoản người dùng.
 * Đây là bảng trung tâm, mọi bảng khác đều tham chiếu đến bảng này.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "full_name")
    val fullName: String,


    @ColumnInfo(name = "email")
    val email: String = "",

    @ColumnInfo(name = "avatar")
    val avatar: String? = null,


    /** Mật khẩu đã hash (SHA-256), không lưu plaintext */
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "avatar_path")
    val avatarPath: String? = null,

    /** Đơn vị tiền tệ, mặc định VND */
    @ColumnInfo(name = "currency")
    val currency: String = "VND",

    /** Số dư ban đầu khai báo khi đăng ký.
     *  Số dư hiện tại = initial_balance + tổng thu - tổng chi (tính động bằng query) */
    @ColumnInfo(name = "initial_balance")
    val initialBalance: Double = 0.0,

    /** Unix timestamp milliseconds */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    /** 1 = đang hoạt động, 0 = bị khóa */
    @ColumnInfo(name = "is_active")
    val isActive: Int = 1

)
