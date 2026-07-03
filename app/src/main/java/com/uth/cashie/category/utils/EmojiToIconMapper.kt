package com.uth.cashie.category.utils

import android.R

object EmojiToIconMapper {
    /**
     * Bản đồ emoji sang drawables có sẵn của Android
     * Sử dụng các icon chuẩn từ android.R.drawable
     */
    private val emojiToDrawableMap = mapOf(
        "🍜" to R.drawable.ic_menu_agenda,      // Ăn uống
        "💰" to R.drawable.ic_menu_edit,        // Tiền bạc
        "🎬" to R.drawable.ic_menu_camera,      // Giải trí
        "🏠" to R.drawable.ic_menu_manage,      // Nhà cửa
        "☕" to R.drawable.ic_menu_add,         // Cafe
        "📚" to R.drawable.ic_menu_info_details, // Giáo dục
        "🎁" to R.drawable.ic_menu_gallery,     // Quà tặng
        "📰" to R.drawable.ic_menu_more,        // Tạp chí
        "👨‍👩‍👧" to R.drawable.ic_menu_myplaces,      // Gia đình
        "🏃" to R.drawable.ic_menu_directions,  // Tập thể dục
        "🚗" to R.drawable.ic_menu_directions,  // Di chuyển
        "✨" to R.drawable.ic_menu_more,        // Khác
        "💵" to R.drawable.ic_menu_save,        // Lương
        "🏆" to R.drawable.ic_menu_help,        // Thưởng
        "📈" to R.drawable.ic_menu_today,       // Đầu tư
        "📞" to R.drawable.ic_menu_send,        // Liên hệ
        "🛒" to R.drawable.ic_menu_agenda,      // Mua sắm
        "🚕" to R.drawable.ic_menu_directions,  // Taxi
        "💳" to R.drawable.ic_menu_edit,        // Thẻ
        "🏥" to R.drawable.ic_menu_agenda,      // Y tế
        "" to R.drawable.ic_menu_more            // Default
    )

    /**
     * Lấy drawable resource từ emoji
     * @param emoji Emoji string (vd: "🍜")
     * @return Drawable resource ID, hoặc icon mặc định nếu không tìm thấy
     */
    fun getIconForEmoji(emoji: String): Int {
        return emojiToDrawableMap[emoji] ?: emojiToDrawableMap[""] ?: R.drawable.ic_menu_more
    }

    /**
     * Lấy danh sách tất cả emoji có sẵn
     */
    fun getAvailableEmojis(): List<String> {
        return emojiToDrawableMap.keys.filter { it.isNotEmpty() }.toList()
    }
}
