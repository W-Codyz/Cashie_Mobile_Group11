package com.uth.cashie

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.uth.cashie.ThemeManager
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.databinding.ActivityStatsBinding
import com.uth.cashie.stats.model.QuarterlyStat
import com.uth.cashie.stats.model.StatsResult
import com.uth.cashie.stats.ui.StatsViewModel
import com.uth.cashie.stats.view.DonutChartView

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private val viewModel: StatsViewModel by viewModels()

    // Màu 4 quý – khớp với legend trong XML
    private val quarterColors = listOf(
        Color.parseColor("#A5D6A7"),   // Q1 – xanh lá nhạt
        Color.parseColor("#FFF59D"),   // Q2 – vàng nhạt
        Color.parseColor("#FFCC80"),   // Q3 – cam nhạt
        Color.parseColor("#80DEEA")    // Q4 – xanh cyan nhạt
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMenuButton()
        applyTheme()
        setupMonthNavigation()
        setupBottomNav()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Theme
    // ─────────────────────────────────────────────────────────────────────────

    private fun applyTheme() {
        val colorInt  = ThemeManager.getThemeColorInt()
        val colorList = ColorStateList.valueOf(colorInt)
        val container = ThemeManager.getSolidContainerColor()

        // Header title
        binding.tvStatsTitle.setTextColor(colorInt)

        // Balance card background + text
        binding.cardBalance.setCardBackgroundColor(container)
        binding.tvStatsBalance.setTextColor(colorInt)

        // Income value
        binding.tvStatsIncome.setTextColor(colorInt)

        // Progress bars (category bars)
        listOf(binding.barCategory1, binding.barCategory2, binding.barCategory3)
            .forEach { bar -> bar.setBackgroundColor(colorInt) }

        // Progress bar backgrounds (container color)
        listOf(binding.barBg1, binding.barBg2, binding.barBg3)
            .forEach { bg -> bg.setBackgroundColor(container) }

        // Weekly chart bars (tall = theme, short = lighter)
        listOf(binding.barT2, binding.barT3, binding.barT4, binding.barT5, binding.barT6, binding.barT7, binding.barCn)
            .forEach { bar -> bar.setBackgroundColor(colorInt) }

        // Bottom nav
        binding.bottomNav.itemActiveIndicatorColor =
            ColorStateList.valueOf(ThemeManager.getContainerColor())
        binding.bottomNav.itemIconTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(colorInt, android.graphics.Color.parseColor("#888888"))
        )
        binding.bottomNav.itemTextColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(colorInt, android.graphics.Color.parseColor("#888888"))
        )
        binding.cardBottomNav.strokeColor = colorInt

        // ProgressBar spinner
        binding.progressBar.indeterminateTintList = colorList
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observers
    // ─────────────────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        // Loading
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        // Error
        viewModel.error.observe(this) { msg ->
            if (!msg.isNullOrBlank()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Month/Year label
        viewModel.month.observe(this) { updateMonthLabel() }
        viewModel.year.observe(this)  { updateMonthLabel() }

        // Stats data
        viewModel.statsResult.observe(this) { result ->
            bindSummary(result)
            bindComparison(result)
            bindDonutChart(result.quarterlyStats)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bind helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun bindSummary(result: StatsResult) {
        val s = result.summary
        binding.tvStatsIncome.text       = formatVND(s.totalIncome)
        binding.tvStatsExpense.text      = formatVND(s.totalExpense)
        binding.tvStatsBalance.text      = formatVND(s.balance)
        binding.tvTotalTransactions.text = s.totalTransactions.toString()
        binding.tvHighestExpense.text    = formatVND(s.highestExpense)
    }

    private fun bindComparison(result: StatsResult) {
        result.comparison?.let { cmp ->
            val expSign = if (cmp.expenseChange >= 0) "+" else ""
            val incSign = if (cmp.incomeChange  >= 0) "+" else ""

            binding.tvExpenseChange.text =
                "$expSign${"%.1f".format(cmp.expenseChangePercent)}% so tháng trước"
            binding.tvIncomeChange.text  =
                "$incSign${"%.1f".format(cmp.incomeChangePercent)}% so tháng trước"

            binding.tvExpenseChange.setTextColor(
                if (cmp.expenseChange > 0) Color.parseColor("#F44336")
                else Color.parseColor("#4CAF50")
            )
            binding.tvIncomeChange.setTextColor(
                if (cmp.incomeChange >= 0) Color.parseColor("#4CAF50")
                else Color.parseColor("#F44336")
            )
        }
    }

    private fun bindDonutChart(quarters: List<QuarterlyStat>) {
        // Chỉ đưa vào biểu đồ những quý có chi tiêu > 0
        val segments = quarters
            .filter { it.expense > 0 }
            .mapIndexed { i, q ->
                DonutChartView.Segment(
                    label = q.label,
                    value = q.expense.toFloat(),
                    color = quarterColors[q.quarter - 1]
                )
            }

        binding.donutChartQuarterly.setData(
            segments       = segments,
            centerTitle    = "Chi tiêu",
            centerSubtitle = "Cả năm"
        )
        binding.donutChartQuarterly.animateIn()

        // Legend – hiển thị số tiền thực từng quý
        val legendViews = listOf(
            binding.tvLegendQ1,
            binding.tvLegendQ2,
            binding.tvLegendQ3,
            binding.tvLegendQ4
        )
        quarters.forEachIndexed { i, q ->
            legendViews.getOrNull(i)?.text =
                if (q.expense > 0) formatVND(q.expense) else "Chưa có"
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Menu button
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupMenuButton() {
        binding.btnMenu.setOnClickListener { showNavMenu(NavScreen.STATS) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Month navigation
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener { viewModel.prevMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
    }

    private fun updateMonthLabel() {
        val m      = viewModel.month.value ?: return
        val y      = viewModel.year.value  ?: return
        val months = resources.getStringArray(R.array.months)
        binding.tvCurrentMonth.text =
            getString(R.string.month_year_format, months.getOrElse(m - 1) { "T$m" }, y)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bottom Navigation
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupBottomNav() {
        setupBottomNav(binding.bottomNav, R.id.nav_stats)
    }
}
