package com.uth.cashie.database.dao

import androidx.room.*
import com.uth.cashie.database.entity.AppSettingsEntity

@Dao
interface AppSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: AppSettingsEntity): Long

    @Update
    suspend fun update(settings: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE user_id = :userId LIMIT 1")
    suspend fun getByUserId(userId: Long): AppSettingsEntity?

    @Query("UPDATE app_settings SET theme_color = :color WHERE user_id = :userId")
    suspend fun updateThemeColor(userId: Long, color: String)

    @Query("UPDATE app_settings SET app_icon_id = :iconId WHERE user_id = :userId")
    suspend fun updateAppIcon(userId: Long, iconId: Int)

    @Query("UPDATE app_settings SET notification_on = :value WHERE user_id = :userId")
    suspend fun updateNotification(userId: Long, value: Int)

    /** language chỉ nhận 'vi' hoặc 'en' – validate ở tầng Repository trước khi gọi */
    @Query("UPDATE app_settings SET language = :language WHERE user_id = :userId")
    suspend fun updateLanguage(userId: Long, language: String)
}
