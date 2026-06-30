package com.uth.cashie.model

/** Danh mục dùng trong form thêm/sửa giao dịch (emoji-based). */
data class EmojiCategory(
    val name: String,
    val emoji: String,
    val colorHex: String
) {
    companion object {
        val EXPENSE = listOf(
            EmojiCategory("Ăn uống",     "🍜", "#FF8C00"),
            EmojiCategory("Di chuyển",   "🚗", "#2196F3"),
            EmojiCategory("Mua sắm",     "🛒", "#E91E63"),
            EmojiCategory("Hóa đơn",     "⚡", "#FFC107"),
            EmojiCategory("Giải trí",    "🎮", "#9C27B0"),
            EmojiCategory("Sức khỏe",    "🏥", "#F44336"),
            EmojiCategory("Giáo dục",    "📚", "#3F51B5"),
            EmojiCategory("Tập thể dục", "🏋", "#FF5722"),
            EmojiCategory("Du lịch",     "✈",  "#00BCD4"),
            EmojiCategory("Quà tặng",    "🎁", "#E91E63"),
            EmojiCategory("Khác",        "💡", "#607D8B"),
        )

        val INCOME = listOf(
            EmojiCategory("Lương",    "💰", "#22CC00"),
            EmojiCategory("Thưởng",   "🎁", "#4CAF50"),
            EmojiCategory("Đầu tư",   "📈", "#00BCD4"),
            EmojiCategory("Thu nhập", "💼", "#8BC34A"),
            EmojiCategory("Khác",     "💡", "#607D8B"),
        )
    }
}
