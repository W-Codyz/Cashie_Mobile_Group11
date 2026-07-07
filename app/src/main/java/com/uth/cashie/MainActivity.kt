package com.uth.cashie

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uth.cashie.adapter.TransactionAdapter
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.data.TransactionRepository
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.databinding.ActivityMainBinding
import com.uth.cashie.model.TransactionGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = TransactionAdapter { tx -> showTransactionDetail(tx.id) }

    private lateinit var months: Array<String>
    private var monthIndex = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentGroups: List<TransactionGroup> = emptyList()

    // ── Activity result launcher (add/edit transaction) ──────────────────────
    private val transactionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) refreshData()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        months = resources.getStringArray(R.array.months)
        applyTheme()
        setupRecyclerView()
        setupMonthNavigation()
        setupFilterChips()
        setupFab()
        setupMenuButton()
        setupBottomNav()
        setupAvatar()
        setupEditBalanceButton()
        refreshData()
    }

    private fun setupEditBalanceButton() {
        binding.btnEditBalance.setOnClickListener {
            lifecycleScope.launch {
                val initialBalance = withContext(Dispatchers.IO) {
                    val db = CashieDatabase.getInstance(this@MainActivity)
                    db.userDao().getById(SessionManager.getCurrentUserId())?.initialBalance ?: 0.0
                }
                showEditBalanceDialog(initialBalance)
            }
        }
    }

    private fun showEditBalanceDialog(initialBalance: Double) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_balance, null)
        val editText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNewBalance)
        val currency = SessionManager.getCurrency()

        editText.setText(initialBalance.toLong().toString())

        AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa số dư")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newBalance = editText.text.toString().toDoubleOrNull() ?: 0.0
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val db = CashieDatabase.getInstance(this@MainActivity)
                        val user = db.userDao().getById(SessionManager.getCurrentUserId()) ?: return@withContext
                        db.userDao().update(user.copy(initialBalance = newBalance))
                    }
                    refreshData()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNav.selectedItemId = R.id.nav_home
        applyTheme()
        refreshData()
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    private fun refreshData() {
        lifecycleScope.launch {
            val txList = TransactionRepository.getByMonth(monthIndex + 1, currentYear)
            currentGroups = TransactionRepository.getGroupedByDate(txList)
            val initialBalance = withContext(Dispatchers.IO) {
                val db = CashieDatabase.getInstance(this@MainActivity)
                db.userDao().getById(SessionManager.getCurrentUserId())?.initialBalance ?: 0.0
            }
            updateSummary(
                txList.sumOf { if (it.isIncome) it.amount else 0L },
                txList.sumOf { if (!it.isIncome) -it.amount else 0L },
                initialBalance.toLong()
            )
            applyFilter()
        }
    }

    private fun updateSummary(income: Long, expense: Long, initialBalance: Long) {
        val currency = SessionManager.getCurrency()
        binding.tvBalance.text      = formatVND(initialBalance + income - expense, currency)
        binding.tvTotalIncome.text  = formatVND(income, currency)
        binding.tvTotalExpense.text = formatVND(expense, currency)
    }

    private fun applyFilter() {
        val filtered = when (binding.chipGroupFilter.checkedChipId) {
            R.id.chipIncome  -> currentGroups
                .map { it.copy(transactions = it.transactions.filter { t -> t.isIncome }) }
                .filter { it.transactions.isNotEmpty() }
            R.id.chipExpense -> currentGroups
                .map { it.copy(transactions = it.transactions.filter { t -> !t.isIncome }) }
                .filter { it.transactions.isNotEmpty() }
            else             -> currentGroups
        }
        adapter.submitGroups(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    private fun applyTheme() {
        val colorInt       = ThemeManager.getThemeColorInt()
        val colorStateList = ColorStateList.valueOf(colorInt)

        ThemeManager.applyToGradientCard(binding.balanceCard)

        binding.fab.backgroundTintList = colorStateList

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
        binding.tvAppName.setTextColor(colorInt)

        val avatarBg = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(colorInt)
        }
        binding.avatarCircle.background = avatarBg

        val chipCheckedBg = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(colorInt, android.graphics.Color.WHITE)
        )
        val chipCheckedStroke = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(colorInt, android.graphics.Color.parseColor("#E0E0E0"))
        )
        listOf(binding.chipAll, binding.chipIncome, binding.chipExpense).forEach { chip ->
            chip.chipBackgroundColor = chipCheckedBg
            chip.chipStrokeColor     = chipCheckedStroke
        }
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter
    }

    // ── Month navigation ──────────────────────────────────────────────────────

    private fun setupMonthNavigation() {
        updateMonthLabel()

        binding.btnPrevMonth.setOnClickListener {
            if (monthIndex > 0) { monthIndex--; onMonthChanged() }
            else if (currentYear > 2020) { currentYear--; monthIndex = 11; onMonthChanged() }
        }

        binding.btnNextMonth.setOnClickListener {
            if (monthIndex < 11) { monthIndex++; onMonthChanged() }
            else { currentYear++; monthIndex = 0; onMonthChanged() }
        }

        binding.tvCurrentMonth.setOnClickListener { showMonthPickerDialog() }
    }

    private fun onMonthChanged() {
        updateMonthLabel()
        refreshData()
    }

    private fun updateMonthLabel() {
        binding.tvCurrentMonth.text =
            getString(R.string.month_year_format, months[monthIndex], currentYear)
    }

    private fun showMonthPickerDialog() {
        val monthNames = resources.getStringArray(R.array.months)

        val monthPicker = NumberPicker(this).apply {
            minValue = 0; maxValue = 11
            displayedValues = monthNames
            value = monthIndex
            wrapSelectorWheel = false
        }

        val yearPicker = NumberPicker(this).apply {
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

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.pick_month_title))
            .setView(row)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                monthIndex  = monthPicker.value
                currentYear = yearPicker.value
                onMonthChanged()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    // ── Filter chips ──────────────────────────────────────────────────────────

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, _ -> applyFilter() }
    }

    // ── FAB ───────────────────────────────────────────────────────────────────

    private fun setupFab() {
        binding.fab.setOnClickListener {
            transactionLauncher.launch(Intent(this, AddTransactionActivity::class.java))
        }
    }

    // ── Hamburger menu ────────────────────────────────────────────────────────

    private fun setupMenuButton() {
        binding.btnMenu.setOnClickListener { showNavigationMenu() }
    }

    private fun showNavigationMenu() {
        showNavMenu(NavScreen.TRANSACTIONS, isRootScreen = true)
    }

    // ── Transaction detail bottom sheet ───────────────────────────────────────

    private fun showTransactionDetail(transactionId: Int) {
        if (supportFragmentManager.findFragmentByTag("detail") != null) return

        TransactionDetailSheet.newInstance(transactionId).apply {
            onEditRequested = { id ->
                val intent = Intent(this@MainActivity, AddTransactionActivity::class.java)
                    .putExtra(AddTransactionActivity.EXTRA_TRANSACTION_ID, id)
                transactionLauncher.launch(intent)
            }
            onDeleted = { refreshData() }
        }.show(supportFragmentManager, "detail")
    }

    // ── Bottom nav ────────────────────────────────────────────────────────────

    private fun setupBottomNav() {
        setupBottomNav(binding.bottomNav, R.id.nav_home)
    }

    // ── Avatar ────────────────────────────────────────────────────────────────

    private fun setupAvatar() {
        binding.btnAvatar.setOnClickListener {
            startActivity(
                Intent(this, ProfileActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.tab_enter, R.anim.tab_exit)
        }
    }
}
