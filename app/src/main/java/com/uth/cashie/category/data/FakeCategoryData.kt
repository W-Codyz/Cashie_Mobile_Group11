package com.uth.cashie.category.data

import com.uth.cashie.category.model.Category

object FakeCategoryData {

    fun getCategories(type: String): MutableList<Category> {
        return when (type) {
            "expense" -> expense()
            "income"  -> income()
            else      -> mutableListOf()
        }
    }

    fun expense(): MutableList<Category> {
        return mutableListOf(
            Category(1, "Sức khỏe", android.R.drawable.ic_menu_agenda, android.R.color.holo_red_light),
            Category(2, "Giải trí", android.R.drawable.ic_menu_camera, android.R.color.holo_blue_light),
            Category(3, "Trang chủ", android.R.drawable.ic_menu_manage, android.R.color.holo_orange_light),
            Category(4, "Cafe", android.R.drawable.ic_menu_edit, android.R.color.holo_purple),
            Category(5, "Giáo dục", android.R.drawable.ic_menu_info_details, android.R.color.holo_blue_dark),
            Category(6, "Quà tặng", android.R.drawable.ic_menu_gallery, android.R.color.holo_red_dark),
            Category(7, "Tạp phẩm", android.R.drawable.ic_menu_add, android.R.color.holo_green_light),
            Category(8, "Gia đình", android.R.drawable.ic_menu_myplaces, android.R.color.holo_blue_bright),
            Category(9, "Tập thể dục", android.R.drawable.ic_menu_directions, android.R.color.holo_orange_dark),
            Category(10, "Di chuyển", android.R.drawable.ic_menu_more, android.R.color.holo_blue_light),
            Category(11, "Khác", android.R.drawable.ic_menu_more, android.R.color.holo_green_dark),
            Category(12, "Tạo", android.R.drawable.ic_menu_add, android.R.color.holo_blue_bright)
        )
    }

    fun income(): MutableList<Category> {
        return mutableListOf(
            Category(101, "Lương", android.R.drawable.ic_menu_save, android.R.color.holo_green_dark),
            Category(102, "Thưởng", android.R.drawable.ic_menu_help, android.R.color.holo_purple),
            Category(103, "Đầu tư", android.R.drawable.ic_menu_today, android.R.color.holo_blue_light),
            Category(104, "Khác", android.R.drawable.ic_menu_more, android.R.color.holo_orange_light)
        )
    }
}