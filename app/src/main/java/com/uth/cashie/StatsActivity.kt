package com.uth.cashie

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.databinding.ActivityStatsBinding
import com.uth.cashie.stats.adapter.CategoryStatAdapter
import com.uth.cashie.stats.model.QuarterlyStat
import com.uth.cashie.stats.model.StatsResult
import com.uth.cashie.stats.report.ExcelExporter
import com.uth.cashie.stats.report.ReportExporter
import com.uth.cashie.stats.ui.StatsViewModel
import com.uth.cashie.stats.view.DonutChartView
import com.uth.cashie.database.SessionManager
import com.uth.cashie.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private val viewModel: StatsViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "✅ Quyền đã được cấp!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "⚠️ Vui lòng cấp quyền để xuất file!", Toast.LENGTH_LONG).show()
        }
    }

    private val expenseAdapter = CategoryStatAdapter()
    private val incomeAdapter  = CategoryStatAdapter()

    private val quarterColors = listOf(
        Color.parseColor("#A5D6A7"),
        Color.parseColor("#FFF59D"),
        Color.parseColor("#FFCC80"),
        Color.parseColor("#80DEEA")
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        setupBarChart()
        setupMenuButton()
        setupMonthNavigation()
        setupExportButtons()
        setupBottomNav()
        applyTheme()
        observeViewModel()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        onNewIntentBottomNav(intent, binding.bubbleNav, R.id.nav_stats)
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        viewModel.loadStats()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupRecyclerViews() {
        binding.rvExpenseCategory.apply {
            layoutManager = LinearLayoutManager(this@StatsActivity)
            adapter = expenseAdapter
            isNestedScrollingEnabled = false
        }
        binding.rvIncomeCategory.apply {
            layoutManager = LinearLayoutManager(this@StatsActivity)
            adapter = incomeAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled      = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawValueAboveBar(false)
            setNoDataText("Chưa có dữ liệu")

            xAxis.apply {
                position         = XAxis.XAxisPosition.BOTTOM
                granularity      = 1f
                setDrawGridLines(false)
                textSize         = 10f
                textColor        = Color.parseColor("#888888")
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor    = Color.parseColor("#F0F0F0")
                axisMinimum  = 0f
                textSize     = 9f
                textColor    = Color.parseColor("#888888")
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = when {
                        value >= 1_000_000f -> "${"%.0f".format(value / 1_000_000f)}M"
                        value >= 1_000f     -> "${"%.0f".format(value / 1_000f)}K"
                        else                -> value.toInt().toString()
                    }
                })
            }
            axisRight.isEnabled = false
        }
    }

    private fun setupMenuButton() {
        binding.btnMenu.setOnClickListener { showNavMenu(NavScreen.STATS) }
    }

    private fun setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener { viewModel.prevMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
        binding.tvCurrentMonth.setOnClickListener { showMonthPickerDialog() }
    }

    private fun showMonthPickerDialog() {
        val monthNames = resources.getStringArray(R.array.months)
        val currentMonth = viewModel.month.value ?: return
        val currentYear  = viewModel.year.value  ?: return

        val monthPicker = android.widget.NumberPicker(this).apply {
            minValue = 0; maxValue = 11
            displayedValues = monthNames
            value = currentMonth - 1
            wrapSelectorWheel = false
        }

        val yearPicker = android.widget.NumberPicker(this).apply {
            minValue = 2020; maxValue = 2030
            value = currentYear
            wrapSelectorWheel = false
        }

        val row = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            addView(monthPicker)
            addView(yearPicker)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.pick_month_title))
            .setView(row)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.setMonth(monthPicker.value + 1, yearPicker.value)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show().also { d ->
                val c = ThemeManager.getThemeColorInt()
                d.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(c)
                d.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(c)
            }
    }

    private fun setupBottomNav() {
        setupBottomNav(binding.bubbleNav, R.id.nav_stats)
    }

    private fun setupExportButtons() {
        binding.btnExportPdf.setOnClickListener   { checkPermissionAndExport { exportPdf() } }
        binding.btnExportExcel.setOnClickListener { checkPermissionAndExport { exportCsv() } }
    }

    private fun checkPermissionAndExport(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            onPermissionGranted()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Theme
    // ─────────────────────────────────────────────────────────────────────────

    private fun applyTheme() {
        val colorInt  = ThemeManager.getThemeColorInt()
        val colorList = ColorStateList.valueOf(colorInt)

        binding.tvStatsTitle.setTextColor(colorInt)
        binding.cardBalance.setCardBackgroundColor(ThemeManager.getSolidContainerColor())
        binding.tvStatsBalance.setTextColor(colorInt)
        binding.tvStatsIncome.setTextColor(colorInt)
        binding.progressBar.indeterminateTintList = colorList
        binding.btnExportPdf.setStrokeColor(colorList)
        binding.btnExportPdf.setTextColor(colorInt)
        binding.btnExportExcel.backgroundTintList = colorList

        binding.bubbleNav.updateThemeColor(colorInt)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observers
    // ─────────────────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) { msg ->
            if (!msg.isNullOrBlank()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        viewModel.month.observe(this) { updateMonthLabel() }
        viewModel.year.observe(this)  { updateMonthLabel() }
        viewModel.statsResult.observe(this) { result ->
            bindSummary(result)
            bindComparison(result)
            bindCategoryLists(result)
            bindBarChart(result)
            bindDonutChart(result.quarterlyStats)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bind: Summary
    // ─────────────────────────────────────────────────────────────────────────

    private fun bindSummary(result: StatsResult) {
        val s = result.summary
        val currency = SessionManager.getCurrency()
        binding.tvStatsIncome.text       = formatVND(s.totalIncome, currency)
        binding.tvStatsExpense.text      = formatVND(s.totalExpense, currency)
        binding.tvStatsBalance.text      = formatVND(s.balance, currency)
        binding.tvTotalTransactions.text = s.totalTransactions.toString()
        binding.tvHighestExpense.text    = formatVND(s.highestExpense, currency)
        binding.tvAvgDailyExpense.text   = formatVND(s.avgDailyExpense, currency)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bind: Comparison
    // ─────────────────────────────────────────────────────────────────────────

    private fun bindComparison(result: StatsResult) {
        result.comparison?.let { cmp ->
            val expSign = if (cmp.expenseChange >= 0) "+" else ""
            val incSign = if (cmp.incomeChange  >= 0) "+" else ""
            binding.tvExpenseChange.text = "$expSign${"%.1f".format(cmp.expenseChangePercent)}% so tháng trước"
            binding.tvIncomeChange.text  = "$incSign${"%.1f".format(cmp.incomeChangePercent)}% so tháng trước"
            binding.tvExpenseChange.setTextColor(
                if (cmp.expenseChange > 0) Color.parseColor("#F44336") else Color.parseColor("#4CAF50")
            )
            binding.tvIncomeChange.setTextColor(
                if (cmp.incomeChange >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            )
        } ?: run {
            binding.tvExpenseChange.text = ""
            binding.tvIncomeChange.text  = ""
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bind: Category lists
    // ─────────────────────────────────────────────────────────────────────────

    private fun bindCategoryLists(result: StatsResult) {
        if (result.expenseByCategory.isEmpty()) {
            binding.tvEmptyExpense.visibility    = View.VISIBLE
            binding.rvExpenseCategory.visibility = View.GONE
        } else {
            binding.tvEmptyExpense.visibility    = View.GONE
            binding.rvExpenseCategory.visibility = View.VISIBLE
            expenseAdapter.submitList(result.expenseByCategory)
        }
        if (result.incomeByCategory.isEmpty()) {
            binding.tvEmptyIncome.visibility    = View.VISIBLE
            binding.rvIncomeCategory.visibility = View.GONE
        } else {
            binding.tvEmptyIncome.visibility    = View.GONE
            binding.rvIncomeCategory.visibility = View.VISIBLE
            incomeAdapter.submitList(result.incomeByCategory)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bind: Bar chart (thu/chi 12 tháng)
    // ─────────────────────────────────────────────────────────────────────────

    private fun bindBarChart(result: StatsResult) {
        val trends = result.monthlyTrends
        if (trends.isEmpty()) { binding.barChart.clear(); return }

        val labels = listOf("T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12")

        // Mỗi tháng dùng 2 đơn vị trên trục X: income ở vị trí chẵn, expense ở chẵn+0.85
        val incomeEntries  = trends.mapIndexed { i, t -> BarEntry(i * 2f,        t.income.toFloat()) }
        val expenseEntries = trends.mapIndexed { i, t -> BarEntry(i * 2f + 0.85f, t.expense.toFloat()) }

        val incomeSet = BarDataSet(incomeEntries, "Thu nhập").apply {
            color = Color.parseColor("#22CC00")
            setDrawValues(false)
        }
        val expenseSet = BarDataSet(expenseEntries, "Chi tiêu").apply {
            color = Color.parseColor("#FF4444")
            setDrawValues(false)
        }

        // Tạo mảng nhãn: nhãn tháng ở vị trí chẵn, chuỗi rỗng ở vị trí lẻ
        val xLabels = Array(trends.size * 2) { i ->
            if (i % 2 == 0) labels.getOrElse(i / 2) { "" } else ""
        }

        binding.barChart.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
            xAxis.labelCount     = xLabels.size
            xAxis.axisMinimum    = -0.5f
            xAxis.axisMaximum    = (trends.size * 2).toFloat() - 0.5f
            data = BarData(incomeSet, expenseSet).apply { barWidth = 0.8f }
            animateY(700)
            invalidate()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bind: Donut chart (chi tiêu theo quý)
    // ─────────────────────────────────────────────────────────────────────────

    private fun bindDonutChart(quarters: List<QuarterlyStat>) {
        val segments = quarters
            .filter { it.expense > 0 }
            .map { q ->
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
        if (segments.isNotEmpty()) binding.donutChartQuarterly.animateIn()

        val currency = SessionManager.getCurrency()
        val legendViews = listOf(
            binding.tvLegendQ1, binding.tvLegendQ2,
            binding.tvLegendQ3, binding.tvLegendQ4
        )
        quarters.forEachIndexed { i, q ->
            legendViews.getOrNull(i)?.text =
                if (q.expense > 0) formatVND(q.expense, currency) else "Chưa có"
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Month label
    // ─────────────────────────────────────────────────────────────────────────

    private fun updateMonthLabel() {
        val m      = viewModel.month.value ?: return
        val y      = viewModel.year.value  ?: return
        val months = resources.getStringArray(R.array.months)
        binding.tvCurrentMonth.text =
            getString(R.string.month_year_format, months.getOrElse(m - 1) { "T$m" }, y)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Export PDF
    // ─────────────────────────────────────────────────────────────────────────

    private fun exportPdf() {
        val result = viewModel.statsResult.value ?: run {
            Toast.makeText(this, "Chưa có dữ liệu để xuất", Toast.LENGTH_SHORT).show()
            return
        }
        val m = viewModel.month.value ?: return
        val y = viewModel.year.value  ?: return

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val resultPair = ReportExporter.exportPdf(
                context = this@StatsActivity,
                result  = result,
                month   = m,
                year    = y
            )
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (resultPair != null) {
                    val (fileName, fileUri) = resultPair
                    Toast.makeText(
                        this@StatsActivity,
                        "✅ Đã lưu: $fileName (Downloads)",
                        Toast.LENGTH_LONG
                    ).show()
                    shareFile(fileUri, "application/pdf")
                } else {
                    Toast.makeText(this@StatsActivity, "❌ Xuất PDF thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Export CSV
    // ─────────────────────────────────────────────────────────────────────────

    private fun exportCsv() {
        val result = viewModel.statsResult.value ?: run {
            Toast.makeText(this, "Chưa có dữ liệu để xuất", Toast.LENGTH_SHORT).show()
            return
        }
        val m = viewModel.month.value ?: return
        val y = viewModel.year.value  ?: return

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val resultPair = ExcelExporter.exportXlsx(
                context = this@StatsActivity,
                result  = result,
                month   = m,
                year    = y
            )
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (resultPair != null) {
                    val (fileName, fileUri) = resultPair
                    Toast.makeText(
                        this@StatsActivity,
                        "✅ Đã lưu: $fileName (Downloads)",
                        Toast.LENGTH_LONG
                    ).show()
                    shareFile(fileUri, "text/csv")
                } else {
                    Toast.makeText(this@StatsActivity, "❌ Xuất CSV thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Share File
    // ─────────────────────────────────────────────────────────────────────────

    private fun shareFile(fileUri: Uri, mimeType: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ báo cáo"))
    }
}
