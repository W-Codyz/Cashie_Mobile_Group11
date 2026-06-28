package com.uth.cashie.category.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uth.cashie.category.model.Category

class CategoryViewModel : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _currentTab = MutableLiveData("expense")

    init {
        loadCategories("expense")
    }

    fun switchTab(type: String) {
        _currentTab.value = type
        loadCategories(type)
    }

    private fun loadCategories(type: String) {
        // ✅ Truyền tham số color cho từng danh mục (dùng màu có sẵn trong Android)
        val list = if (type == "expense") {
            listOf(
                Category(1, "Ăn uống", android.R.drawable.ic_menu_gallery, android.R.color.holo_red_light),
                Category(2, "Di chuyển", android.R.drawable.ic_menu_directions, android.R.color.holo_blue_dark),
                Category(3, "Mua sắm", android.R.drawable.ic_menu_agenda, android.R.color.holo_green_light),
                Category(4, "Hóa đơn", android.R.drawable.ic_menu_edit, android.R.color.holo_orange_light)
            )
        } else {
            listOf(
                Category(5, "Lương", android.R.drawable.ic_menu_manage, android.R.color.holo_blue_light),
                Category(6, "Đầu tư", android.R.drawable.ic_menu_today, android.R.color.holo_purple),
                Category(7, "Thưởng", android.R.drawable.ic_menu_send, android.R.color.holo_green_dark)
            )
        }
        _categories.value = list
    }
}