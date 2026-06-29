package com.uth.cashie.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng app_settings – cài đặt giao diện và tuỳ chọn cá nhân.
 * Mỗi user có đúng 1 bản ghi, được tạo ngay khi đăng ký.
 */
@Entity(
    tableName = "app_settings",
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
data class AppSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    /** Màu chủ đề giao diện (mã hex) */
    @ColumnInfo(name = "theme_color")
    val themeColor: String = "#6200EE",

    /** ID icon ứng dụng (ic_launcher_1 đến ic_launcher_9) */
    @ColumnInfo(name = "app_icon_id")
    val appIconId: Int = 1,

    /** 1 = bật thông báo, 0 = tắt */
    @ColumnInfo(name = "notification_on")
    val notificationOn: Int = 1,

    /** Ngôn ngữ hiển thị: 'vi' hoặc 'en' */
    @ColumnInfo(name = "language")
    val language: String = "vi"
)
