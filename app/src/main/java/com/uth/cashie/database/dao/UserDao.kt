package com.uth.cashie.database.dao

import androidx.room.*
import com.uth.cashie.database.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    /** Dùng để xác thực đăng nhập */
    @Query("SELECT * FROM users WHERE username = :username AND password_hash = :passwordHash AND is_active = 1 LIMIT 1")
    suspend fun login(username: String, passwordHash: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun usernameExists(username: String): Int

    /** Tính số dư hiện tại: initial_balance + tổng thu - tổng chi */
    @Query("""
        SELECT u.initial_balance 
             + COALESCE((SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'income'), 0)
             - COALESCE((SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'expense'), 0)
        FROM users u WHERE u.id = :userId
    """)
    suspend fun getCurrentBalance(userId: Long): Double

    @Query("UPDATE users SET avatar_path = :path WHERE id = :userId")
    suspend fun updateAvatar(userId: Long, path: String)

    @Query("UPDATE users SET full_name = :fullName, email = :email WHERE id = :userId")
    suspend fun updateProfile(userId: Long, fullName: String, email: String?)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: Long)
}
