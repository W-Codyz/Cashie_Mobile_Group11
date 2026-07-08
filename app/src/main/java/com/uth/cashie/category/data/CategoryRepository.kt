package com.uth.cashie.category.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.uth.cashie.ThemeManager
import com.uth.cashie.category.model.Category
import com.uth.cashie.category.utils.EmojiToIconMapper
import com.uth.cashie.database.dao.CategoryDao
import com.uth.cashie.database.entity.CategoryEntity
import android.graphics.Color

class CategoryRepository(private val categoryDao: CategoryDao) {

    private fun CategoryEntity.displayName(): String =
        if (ThemeManager.getLanguage() == "en" && !nameEn.isNullOrBlank()) nameEn else name

    fun getCategoriesByType(userId: Long, type: String): LiveData<List<Category>> {
        return categoryDao.getByType(userId, type).map { entities ->
            entities.map { entity ->
                Category(
                    id = entity.id.toInt(),
                    name = entity.displayName(),
                    icon = EmojiToIconMapper.getIconForEmoji(entity.iconEmoji ?: ""),
                    color = try { Color.parseColor(entity.color ?: "#FF000000") } catch (e: Exception) { Color.BLACK },
                    emoji = entity.iconEmoji ?: "",
                    isDefault = entity.isDefault == 1
                )
            }
        }
    }

    suspend fun getCategoriesByTypeOrderedByUsage(userId: Long, type: String): List<com.uth.cashie.model.EmojiCategory> {
        return categoryDao.getByTypeOrderedByUsage(userId, type).map { entity ->
            com.uth.cashie.model.EmojiCategory(
                name = entity.displayName(),
                emoji = entity.iconEmoji ?: "💡",
                colorHex = entity.color ?: "#888888"
            )
        }
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insert(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(id: Long, userId: Long): Int {
        return categoryDao.deleteById(id, userId)
    }
}