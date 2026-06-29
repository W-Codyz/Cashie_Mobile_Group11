package com.uth.cashie.database.dao

import androidx.room.*
import com.uth.cashie.database.entity.SavedAccountEntity

@Dao
interface SavedAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: SavedAccountEntity): Long

    @Query("SELECT * FROM saved_accounts ORDER BY last_login_at DESC")
    suspend fun getAll(): List<SavedAccountEntity>

    @Query("SELECT * FROM saved_accounts WHERE remember_login = 1 LIMIT 1")
    suspend fun getRemembered(): SavedAccountEntity?

    @Query("SELECT * FROM saved_accounts WHERE user_id = :userId LIMIT 1")
    suspend fun getByUserId(userId: Long): SavedAccountEntity?

    /** Cập nhật thời gian đăng nhập mỗi khi user login */
    @Query("UPDATE saved_accounts SET last_login_at = :timestamp WHERE user_id = :userId")
    suspend fun updateLastLogin(userId: Long, timestamp: Long = System.currentTimeMillis())

    /** Khi đăng xuất và không tích 'Nhớ đăng nhập': reset về 0 nhưng giữ bản ghi */
    @Query("UPDATE saved_accounts SET remember_login = :value WHERE user_id = :userId")
    suspend fun updateRememberLogin(userId: Long, value: Int)

    @Query("DELETE FROM saved_accounts WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: Long)
}
