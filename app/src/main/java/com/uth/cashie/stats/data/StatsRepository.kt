package com.uth.cashie.stats.data

import com.uth.cashie.data.TransactionRepository
import com.uth.cashie.model.Transaction
import com.uth.cashie.stats.model.*
import kotlin.math.abs

/**
 * Tính toán tất cả số liệu thống kê từ TransactionRepository (Room Database).
 */
object StatsRepository {

    suspend fun getStatsForMonth(month: Int, year: Int): StatsResult {
        val txs = TransactionRepository.getByMonth(month, year)
        return StatsResult(
            summary           = buildSummary(month, year, txs),
            expenseByCategory = buildCategoryStats(txs.filter { !it.isIncome }),
            incomeByCategory  = buildCategoryStats(txs.filter { it.isIncome }),
            dailyAmounts      = buildDailyAmounts(txs),
            monthlyTrends     = buildMonthlyTrends(year),
            quarterlyStats    = buildQuarterlyStats(year),
            comparison        = buildComparison(month, year)
        )
    }

    private fun buildSummary(month: Int, year: Int, txs: List<Transaction>): MonthlySummary {
        val income   = txs.filter { it.isIncome }.sumOf { it.amount }
        val expense  = txs.filter { !it.isIncome }.sumOf { abs(it.amount) }
        val expenses = txs.filter { !it.isIncome }
        val days     = expenses.map { it.date }.distinct().size
        return MonthlySummary(
            month             = month,
            year              = year,
            totalIncome       = income,
            totalExpense      = expense,
            totalTransactions = txs.size,
            highestExpense    = expenses.maxOfOrNull { abs(it.amount) } ?: 0L,
            highestIncome     = txs.filter { it.isIncome }.maxOfOrNull { it.amount } ?: 0L,
            avgDailyExpense   = if (days > 0) expense / days else 0L
        )
    }

    private fun buildCategoryStats(txs: List<Transaction>): List<CategoryStat> {
        if (txs.isEmpty()) return emptyList()
        val total = txs.sumOf { abs(it.amount) }.toFloat()
        return txs.groupBy { it.category }.map { (cat, list) ->
            val amount = list.sumOf { abs(it.amount) }
            CategoryStat(
                categoryName     = cat,
                emoji            = list.first().emoji,
                colorHex         = list.first().iconColorHex,
                totalAmount      = amount,
                transactionCount = list.size,
                percentage       = if (total > 0) (amount / total) * 100f else 0f
            )
        }.sortedByDescending { it.totalAmount }
    }

    private fun buildDailyAmounts(txs: List<Transaction>): List<DailyAmount> =
        txs.groupBy { it.date }.map { (date, list) ->
            val day     = date.split("-").getOrNull(2)?.toIntOrNull() ?: 0
            val income  = list.filter { it.isIncome }.sumOf { it.amount }
            val expense = list.filter { !it.isIncome }.sumOf { abs(it.amount) }
            DailyAmount(day = day, income = income, expense = expense)
        }.sortedBy { it.day }

    private suspend fun buildMonthlyTrends(year: Int): List<MonthlyTrend> {
        val labels = listOf("T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12")
        return (1..12).map { m ->
            val txs     = TransactionRepository.getByMonth(m, year)
            val income  = txs.filter { it.isIncome }.sumOf { it.amount }
            val expense = txs.filter { !it.isIncome }.sumOf { abs(it.amount) }
            MonthlyTrend(month = m, label = labels[m - 1], income = income, expense = expense)
        }
    }

    private suspend fun buildQuarterlyStats(year: Int): List<QuarterlyStat> {
        val quarters = listOf(1..3, 4..6, 7..9, 10..12)
        return quarters.mapIndexed { idx, range ->
            val txs = TransactionRepository.getByYear(year).filter { tx ->
                val m = tx.date.split("-").getOrNull(1)?.toIntOrNull() ?: 0
                m in range
            }
            QuarterlyStat(
                quarter = idx + 1,
                label   = "Quý ${idx + 1}",
                expense = txs.filter { !it.isIncome }.sumOf { abs(it.amount) },
                income  = txs.filter { it.isIncome }.sumOf { it.amount }
            )
        }
    }

    private suspend fun buildComparison(month: Int, year: Int): MonthComparison? {
        val prevMonth = if (month == 1) 12 else month - 1
        val prevYear  = if (month == 1) year - 1 else year
        val prevTxs   = TransactionRepository.getByMonth(prevMonth, prevYear)
        if (prevTxs.isEmpty()) return null

        val curTxs      = TransactionRepository.getByMonth(month, year)
        val curIncome   = curTxs.filter { it.isIncome }.sumOf { it.amount }
        val curExpense  = curTxs.filter { !it.isIncome }.sumOf { abs(it.amount) }
        val prevIncome  = prevTxs.filter { it.isIncome }.sumOf { it.amount }
        val prevExpense = prevTxs.filter { !it.isIncome }.sumOf { abs(it.amount) }

        val dIncome  = curIncome - prevIncome
        val dExpense = curExpense - prevExpense
        return MonthComparison(
            incomeChange         = dIncome,
            expenseChange        = dExpense,
            incomeChangePercent  = if (prevIncome  > 0) dIncome.toFloat()  / prevIncome  * 100f else 0f,
            expenseChangePercent = if (prevExpense > 0) dExpense.toFloat() / prevExpense * 100f else 0f
        )
    }
}
