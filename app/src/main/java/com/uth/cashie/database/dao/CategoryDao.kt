package com.uth.cashie.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.uth.cashie.database.entity.CategoryEntity

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    /** Mọi query phải có WHERE user_id = ? để tránh lẫn dữ liệu */
    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY is_default DESC, name ASC")
    fun getAllByUser(userId: Long): LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE user_id = :userId AND type = :type ORDER BY is_default DESC, name ASC")
    fun getByType(userId: Long, type: String): LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE user_id = :userId AND type = :type ORDER BY is_default DESC, name ASC")
    suspend fun getByTypeOnce(userId: Long, type: String): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id AND user_id = :userId LIMIT 1")
    suspend fun getById(id: Long, userId: Long): CategoryEntity?

    /** is_default = 1: chỉ cho đổi tên và màu, không cho xóa */
    @Query("DELETE FROM categories WHERE id = :id AND user_id = :userId AND is_default = 0")
    suspend fun deleteById(id: Long, userId: Long): Int

    @Query("UPDATE categories SET name = :name, color = :color WHERE id = :id AND user_id = :userId")
    suspend fun updateNameAndColor(id: Long, userId: Long, name: String, color: String?)
}
