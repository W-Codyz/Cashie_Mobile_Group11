package com.uth.cashie.database

import com.uth.cashie.database.entity.AppSettingsEntity
import com.uth.cashie.database.entity.CategoryEntity

/**
 * Khởi tạo dữ liệu mặc định ngay sau khi tạo user.
 *
 * Phải gọi trong cùng 1 transaction:
 *   db.withTransaction { insertUser(); insertDefaultCategories(); insertDefaultSettings() }
 */
object DatabaseInitializer {

    /** Danh mục chi mặc định (is_default = 1) */
    private fun defaultExpenseCategories(userId: Long): List<CategoryEntity> = listOf(
        CategoryEntity(userId = userId, name = "Ăn uống",     iconEmoji = "🍜", type = "expense", color = "#FF8C00", isDefault = 1),
        CategoryEntity(userId = userId, name = "Di chuyển",   iconEmoji = "🚗", type = "expense", color = "#2196F3", isDefault = 1),
        CategoryEntity(userId = userId, name = "Mua sắm",     iconEmoji = "🛒", type = "expense", color = "#E91E63", isDefault = 1),
        CategoryEntity(userId = userId, name = "Hóa đơn",     iconEmoji = "⚡", type = "expense", color = "#FFC107", isDefault = 1),
        CategoryEntity(userId = userId, name = "Giải trí",    iconEmoji = "🎮", type = "expense", color = "#9C27B0", isDefault = 1),
        CategoryEntity(userId = userId, name = "Sức khỏe",    iconEmoji = "🏥", type = "expense", color = "#F44336", isDefault = 1),
        CategoryEntity(userId = userId, name = "Giáo dục",    iconEmoji = "📚", type = "expense", color = "#3F51B5", isDefault = 1),
        CategoryEntity(userId = userId, name = "Tập thể dục", iconEmoji = "🏋", type = "expense", color = "#FF5722", isDefault = 1),
        CategoryEntity(userId = userId, name = "Du lịch",     iconEmoji = "✈",  type = "expense", color = "#00BCD4", isDefault = 1),
        CategoryEntity(userId = userId, name = "Quà tặng",    iconEmoji = "🎁", type = "expense", color = "#E91E63", isDefault = 1),
        CategoryEntity(userId = userId, name = "Khác",        iconEmoji = "💡", type = "expense", color = "#607D8B", isDefault = 1),
    )

    /** Danh mục thu mặc định (is_default = 1) */
    private fun defaultIncomeCategories(userId: Long): List<CategoryEntity> = listOf(
        CategoryEntity(userId = userId, name = "Lương",    iconEmoji = "💰", type = "income", color = "#22CC00", isDefault = 1),
        CategoryEntity(userId = userId, name = "Thưởng",   iconEmoji = "🎁", type = "income", color = "#4CAF50", isDefault = 1),
        CategoryEntity(userId = userId, name = "Đầu tư",   iconEmoji = "📈", type = "income", color = "#00BCD4", isDefault = 1),
        CategoryEntity(userId = userId, name = "Thu nhập", iconEmoji = "💼", type = "income", color = "#8BC34A", isDefault = 1),
        CategoryEntity(userId = userId, name = "Khác",     iconEmoji = "💡", type = "income", color = "#607D8B", isDefault = 1),
    )

    /** Cài đặt mặc định cho user mới */
    fun defaultSettings(userId: Long) = AppSettingsEntity(
        userId         = userId,
        themeColor     = "#22CC00",
        appIconId      = 1,
        notificationOn = 1,
        language       = "vi"
    )

    /** Gọi hàm này để lấy danh sách tất cả danh mục mặc định */
    fun defaultCategories(userId: Long): List<CategoryEntity> =
        defaultExpenseCategories(userId) + defaultIncomeCategories(userId)

    /**
     * Khởi tạo toàn bộ dữ liệu sau khi tạo user.
     * Phải gọi trong Room transaction để đảm bảo atomicity.
     *
     * Ví dụ:
     * ```kotlin
     * db.withTransaction {
     *     val userId = userDao.insert(newUser)
     *     categoryDao.insertAll(DatabaseInitializer.defaultCategories(userId))
     *     appSettingsDao.insert(DatabaseInitializer.defaultSettings(userId))
     * }
     * ```
     */
    suspend fun setup(userId: Long, db: CashieDatabase) {
        db.categoryDao().insertAll(defaultCategories(userId))
        db.appSettingsDao().insert(defaultSettings(userId))
    }
}
