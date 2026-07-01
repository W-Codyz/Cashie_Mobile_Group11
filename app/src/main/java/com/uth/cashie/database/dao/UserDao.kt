package com.uth.cashie.database.dao

import androidx.room.*
import com.uth.cashie.database.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun exists(username: String): Int

    @Query("""
        SELECT * FROM users
        WHERE username = :username
        AND password_hash = :passwordHash
        LIMIT 1
    """)
    suspend fun login(
        username: String,
        passwordHash: String
    ): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<UserEntity>
}