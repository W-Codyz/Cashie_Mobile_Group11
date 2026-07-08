package com.uth.cashie.category.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.switchMap
import com.uth.cashie.category.data.CategoryRepository
import com.uth.cashie.category.model.Category
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository,
    private val userId: Long
) : ViewModel() {

    private val _currentTab = MutableLiveData("expense")

    val categories: LiveData<List<Category>> = _currentTab.switchMap { type ->
        repository.getCategoriesByType(userId, type)
    }

    init {
        _currentTab.value = "expense"
    }

    fun switchTab(type: String) {
        _currentTab.value = type
    }

    suspend fun addCategory(category: com.uth.cashie.database.entity.CategoryEntity): Long {
        return repository.insertCategory(category)
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id, userId)
        }
    }
}

class CategoryViewModelFactory(
    private val repository: CategoryRepository,
    private val userId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}