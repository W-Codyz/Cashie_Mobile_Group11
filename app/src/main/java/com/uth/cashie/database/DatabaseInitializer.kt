package com.uth.cashie.database

import com.uth.cashie.database.entity.AppSettingsEntity
import com.uth.cashie.database.entity.CategoryEntity
import com.uth.cashie.database.entity.WalletEntity


/**
 * Khởi tạo dữ liệu mặc định ngay sau khi tạo user.
 *
 * Phải gọi trong cùng 1 transaction:
 *   db.withTransaction { insertUser(); insertDefaultCategories(); insertDefaultSettings() }
 */
object DatabaseInitializer {

    /** Danh mục chi mặc định (is_default = 1) */
    private fun defaultExpenseCategories(userId: Long): List<CategoryEntity> = listOf(
        CategoryEntity(userId = userId, name = "Ăn uống",     nameEn = "Food & Dining",  iconEmoji = "🍜", type = "expense", color = "#FF8C00", isDefault = 1),
        CategoryEntity(userId = userId, name = "Di chuyển",   nameEn = "Transport",      iconEmoji = "🚗", type = "expense", color = "#2196F3", isDefault = 1),
        CategoryEntity(userId = userId, name = "Mua sắm",     nameEn = "Shopping",       iconEmoji = "🛒", type = "expense", color = "#E91E63", isDefault = 1),
        CategoryEntity(userId = userId, name = "Hóa đơn",     nameEn = "Bills",          iconEmoji = "⚡", type = "expense", color = "#FFC107", isDefault = 1),
        CategoryEntity(userId = userId, name = "Giải trí",    nameEn = "Entertainment",  iconEmoji = "🎮", type = "expense", color = "#9C27B0", isDefault = 1),
        CategoryEntity(userId = userId, name = "Sức khỏe",    nameEn = "Health",         iconEmoji = "🏥", type = "expense", color = "#F44336", isDefault = 1),
        CategoryEntity(userId = userId, name = "Giáo dục",    nameEn = "Education",      iconEmoji = "📚", type = "expense", color = "#3F51B5", isDefault = 1),
        CategoryEntity(userId = userId, name = "Tập thể dục", nameEn = "Exercise",       iconEmoji = "🏋", type = "expense", color = "#FF5722", isDefault = 1),
        CategoryEntity(userId = userId, name = "Du lịch",     nameEn = "Travel",         iconEmoji = "✈",  type = "expense", color = "#00BCD4", isDefault = 1),
        CategoryEntity(userId = userId, name = "Quà tặng",    nameEn = "Gifts",          iconEmoji = "🎁", type = "expense", color = "#E91E63", isDefault = 1),
        CategoryEntity(userId = userId, name = "Khác",        nameEn = "Other",          iconEmoji = "💡", type = "expense", color = "#607D8B", isDefault = 1),
    )

    /** Danh mục thu mặc định (is_default = 1) */
    private fun defaultIncomeCategories(userId: Long): List<CategoryEntity> = listOf(
        CategoryEntity(userId = userId, name = "Lương",    nameEn = "Salary",     iconEmoji = "💰", type = "income", color = "#22CC00", isDefault = 1),
        CategoryEntity(userId = userId, name = "Thưởng",   nameEn = "Bonus",      iconEmoji = "🎁", type = "income", color = "#4CAF50", isDefault = 1),
        CategoryEntity(userId = userId, name = "Đầu tư",   nameEn = "Investment", iconEmoji = "📈", type = "income", color = "#00BCD4", isDefault = 1),
        CategoryEntity(userId = userId, name = "Thu nhập", nameEn = "Income",     iconEmoji = "💼", type = "income", color = "#8BC34A", isDefault = 1),
        CategoryEntity(userId = userId, name = "Khác",     nameEn = "Other",      iconEmoji = "💡", type = "income", color = "#607D8B", isDefault = 1),
    )

    /** Ví mặc định */
    private fun defaultWallets(userId: Long): List<WalletEntity> = listOf(
        WalletEntity(userId = userId, name = "Tiền mặt", type = "cash", balance = 0.0, iconEmoji = "💵", isDefault = 1),
        WalletEntity(userId = userId, name = "Tài khoản ngân hàng", type = "bank", balance = 0.0, iconEmoji = "🏦", isDefault = 0)
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
        db.walletDao().insertAll(defaultWallets(userId))
        db.appSettingsDao().insert(defaultSettings(userId))
    }
}
