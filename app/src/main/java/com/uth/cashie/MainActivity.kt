package com.uth.cashie

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.uth.cashie.adapter.TransactionAdapter
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.databinding.ActivityMainBinding
import com.uth.cashie.model.Transaction
import com.uth.cashie.model.TransactionGroup

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = TransactionAdapter()

    private val allGroups = listOf(
        TransactionGroup(
            dateLabel = "Hôm nay, 16/06",
            dayNet    = 10_000_000L - 85_000L - 45_000L,
            transactions = listOf(
                Transaction(1, "Lương tháng 6",    "Thu nhập",  "💰", "#22CC00", 10_000_000, true,  "08:30"),
                Transaction(2, "Ăn trưa",           "Ăn uống",   "🍜", "#FF8C00",    -85_000, false, "12:15"),
                Transaction(3, "Taxi về nhà",        "Di chuyển", "🚗", "#2196F3",    -45_000, false, "17:30"),
            )
        ),
        TransactionGroup(
            dateLabel = "Hôm qua, 15/06",
            dayNet    = 2_500_000L - 350_000L,
            transactions = listOf(
                Transaction(4, "Siêu thị Coopmart", "Mua sắm",   "🛒", "#E91E63",   -350_000, false, "10:00"),
                Transaction(5, "Freelance design",  "Thu nhập",  "💼", "#22CC00",  2_500_000, true,  "14:00"),
            )
        ),
        TransactionGroup(
            dateLabel = "14/06/2026",
            dayNet    = -(450_000L + 200_000L + 35_000L),
            transactions = listOf(
                Transaction(6, "Tiền điện",          "Hóa đơn",   "⚡", "#FFC107",  -450_000, false, "09:00"),
                Transaction(7, "Tiền internet",      "Hóa đơn",   "📶", "#9C27B0",  -200_000, false, "09:05"),
                Transaction(8, "Café buổi sáng",     "Ăn uống",   "☕", "#795548",   -35_000, false, "07:30"),
            )
        ),
    )

    private val totalIncome  = 12_500_000L
    private val totalExpense =  1_165_000L
    private lateinit var months: Array<String>
    private var monthIndex = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        months = resources.getStringArray(R.array.months)
        setupRecyclerView()
        setupSummary()
        setupMonthNavigation()
        setupFilterChips()
        setupFab()
        setupBottomNav()
        setupAvatar()
    }

    private fun setupAvatar() {
        binding.btnAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter       = adapter
        adapter.submitGroups(allGroups)
    }

    private fun setupSummary() {
        binding.tvBalance.text      = formatVND(totalIncome - totalExpense)
        binding.tvTotalIncome.text  = formatVND(totalIncome)
        binding.tvTotalExpense.text = formatVND(totalExpense)
    }

    private fun setupMonthNavigation() {
        updateMonthLabel()
        binding.btnPrevMonth.setOnClickListener {
            if (monthIndex > 0) { monthIndex--; updateMonthLabel() }
        }
        binding.btnNextMonth.setOnClickListener {
            if (monthIndex < 11) { monthIndex++; updateMonthLabel() }
        }
    }

    private fun updateMonthLabel() {
        binding.tvCurrentMonth.text = getString(R.string.month_year_format, months[monthIndex], 2026)
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filtered = when {
                checkedIds.contains(R.id.chipIncome)  ->
                    allGroups.map { it.copy(transactions = it.transactions.filter { t -> t.isIncome }) }
                        .filter { it.transactions.isNotEmpty() }
                checkedIds.contains(R.id.chipExpense) ->
                    allGroups.map { it.copy(transactions = it.transactions.filter { t -> !t.isIncome }) }
                        .filter { it.transactions.isNotEmpty() }
                else -> allGroups
            }
            adapter.submitGroups(filtered)
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            // TODO: mở màn hình thêm giao dịch
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.itemActiveIndicatorColor =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_container))
        binding.bottomNav.setOnItemSelectedListener { true }
    }
}
