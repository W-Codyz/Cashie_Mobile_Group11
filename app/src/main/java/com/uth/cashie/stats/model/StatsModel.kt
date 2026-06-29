package com.uth.cashie.stats.model

/**
 * Tổng quan tài chính theo tháng
 */
data class MonthlySummary(
    val month: Int,
    val year: Int,
    val totalIncome: Long,
    val totalExpense: Long,
    val balance: Long = totalIncome - totalExpense,
    val totalTransactions: Int,
    val highestExpense: Long,
    val highestIncome: Long,
    val avgDailyExpense: Long
)

/**
 * Thống kê theo danh mục (biểu đồ tròn / danh sách)
 */
data class CategoryStat(
    val categoryName: String,
    val emoji: String,
    val colorHex: String,
    val totalAmount: Long,
    val transactionCount: Int,
    val percentage: Float
)

/**
 * Thu chi theo từng ngày trong tháng (biểu đồ cột)
 */
data class DailyAmount(
    val day: Int,
    val income: Long,
    val expense: Long,
    val net: Long = income - expense
)

/**
 * Xu hướng thu/chi theo tháng trong năm
 */
data class MonthlyTrend(
    val month: Int,
    val label: String,
    val income: Long,
    val expense: Long
)

/**
 * Thống kê chi tiêu theo quý
 */
data class QuarterlyStat(
    val quarter: Int,       // 1–4
    val label: String,      // "Quý 1"
    val expense: Long,
    val income: Long
)

/**
 * So sánh với tháng trước
 */
data class MonthComparison(
    val incomeChange: Long,
    val expenseChange: Long,
    val incomeChangePercent: Float,
    val expenseChangePercent: Float
)

/**
 * Toàn bộ dữ liệu thống kê trả về cho UI
 */
data class StatsResult(
    val summary: MonthlySummary,
    val expenseByCategory: List<CategoryStat>,
    val incomeByCategory: List<CategoryStat>,
    val dailyAmounts: List<DailyAmount>,
    val monthlyTrends: List<MonthlyTrend>,
    val quarterlyStats: List<QuarterlyStat>,
    val comparison: MonthComparison?
)
