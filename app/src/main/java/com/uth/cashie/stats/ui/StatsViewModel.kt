package com.uth.cashie.stats.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.cashie.stats.data.StatsRepository
import com.uth.cashie.stats.model.StatsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel cho StatsActivity.
 *
 * Quản lý state tháng/năm hiện tại và expose [StatsResult] qua LiveData.
 * Tính toán chạy trên IO thread để không block UI thread.
 */
class StatsViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────────────────
    // Exposed state
    // ─────────────────────────────────────────────────────────────────────────

    private val _month = MutableLiveData(6)
    val month: LiveData<Int> = _month

    private val _year = MutableLiveData(2026)
    val year: LiveData<Int> = _year

    private val _statsResult = MutableLiveData<StatsResult>()
    val statsResult: LiveData<StatsResult> = _statsResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ─────────────────────────────────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────────────────────────────────

    init {
        loadStats()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public actions
    // ─────────────────────────────────────────────────────────────────────────

    fun prevMonth() {
        val m = _month.value ?: 6
        val y = _year.value  ?: 2026
        if (m == 1) { _month.value = 12; _year.value = y - 1 }
        else        { _month.value = m - 1 }
        loadStats()
    }

    fun nextMonth() {
        val m = _month.value ?: 6
        val y = _year.value  ?: 2026
        if (m == 12) { _month.value = 1; _year.value = y + 1 }
        else         { _month.value = m + 1 }
        loadStats()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadStats() {
        val m = _month.value ?: return
        val y = _year.value  ?: return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    StatsRepository.getStatsForMonth(m, y)
                }
                _statsResult.value = result
            } catch (e: Exception) {
                _error.value = "Lỗi tải dữ liệu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
