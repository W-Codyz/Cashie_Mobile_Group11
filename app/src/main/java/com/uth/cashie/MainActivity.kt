package com.uth.cashie

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.uth.cashie.ThemeManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.uth.cashie.adapter.TransactionAdapter
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.data.TransactionRepository
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.database.entity.MonthlyBalanceEntity
import com.uth.cashie.databinding.ActivityMainBinding
import com.uth.cashie.model.TransactionGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : BaseActivity() {

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
        setupEditBalance()
        refreshData()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        onNewIntentBottomNav(intent, binding.bubbleNav, R.id.nav_home)
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        refreshData()
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    private fun refreshData() {
        lifecycleScope.launch {
            val txList = TransactionRepository.getByMonth(monthIndex + 1, currentYear)
            currentGroups = TransactionRepository.getGroupedByDate(txList)
            val income   = txList.sumOf { if (it.isIncome) it.amount else 0L }
            val expense  = txList.sumOf { if (!it.isIncome) -it.amount else 0L }
            val opening  = loadOpeningBalance(monthIndex + 1, currentYear)
            updateSummary(income, expense, opening)
            applyFilter()
        }
    }

    private suspend fun loadOpeningBalance(month: Int, year: Int): Long =
        withContext(Dispatchers.IO) {
            val userId = SessionManager.getCurrentUserId()
            val entity = CashieDatabase.getInstance(applicationContext)
                .monthlyBalanceDao()
                .getByMonthYear(userId, month, year)
            (entity?.cashBalance ?: 0L) + (entity?.accountBalance ?: 0L)
        }

    private fun updateSummary(income: Long, expense: Long, openingBalance: Long = 0L) {
        val currency = SessionManager.getCurrency()
        val balance = openingBalance + income - expense
        binding.tvBalance.text = formatVND(balance, currency)
        binding.tvBalance.setTextColor(
            if (balance < 0) android.graphics.Color.RED
            else android.graphics.Color.WHITE
        )
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

    // ── Edit Balance ──────────────────────────────────────────────────────────

    private fun setupEditBalance() {
        binding.btnEditBalance.setOnClickListener { showEditBalanceDialog() }
    }

    private fun showEditBalanceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_balance, null)

        val pickerMonth    = dialogView.findViewById<NumberPicker>(R.id.pickerMonth)
        val pickerYear     = dialogView.findViewById<NumberPicker>(R.id.pickerYear)
        val etCash         = dialogView.findViewById<TextInputEditText>(R.id.etCashBalance)
        val etAccount      = dialogView.findViewById<TextInputEditText>(R.id.etAccountBalance)
        val tilCash        = dialogView.findViewById<TextInputLayout>(R.id.tilCashBalance)
        val tilAccount     = dialogView.findViewById<TextInputLayout>(R.id.tilAccountBalance)
        val tvCashNet      = dialogView.findViewById<TextView>(R.id.tvCashNet)
        val tvCashFinal    = dialogView.findViewById<TextView>(R.id.tvCashFinal)
        val tvAccountNet   = dialogView.findViewById<TextView>(R.id.tvAccountNet)
        val tvAccountFinal = dialogView.findViewById<TextView>(R.id.tvAccountFinal)

        val themeColor     = ThemeManager.getThemeColorInt()
        val themeColorList = ColorStateList.valueOf(themeColor)
        val strokeColors   = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
            intArrayOf(themeColor, android.graphics.Color.parseColor("#BDBDBD"))
        )
        tilCash.setBoxStrokeColorStateList(strokeColors)
        tilCash.hintTextColor = themeColorList
        tilAccount.setBoxStrokeColorStateList(strokeColors)
        tilAccount.hintTextColor = themeColorList

        pickerMonth.apply {
            minValue = 0; maxValue = 11
            displayedValues = resources.getStringArray(R.array.months)
            value = monthIndex
            wrapSelectorWheel = false
        }

        pickerYear.apply {
            minValue = 2020; maxValue = 2035
            value = currentYear
            wrapSelectorWheel = false
        }

        val currency = SessionManager.getCurrency()
        var cashNet  = 0L
        var bankNet  = 0L
        var cashWalletId: Long? = null
        var bankWalletId: Long? = null

        val colorGreen = android.graphics.Color.parseColor("#22CC00")
        val colorRed   = android.graphics.Color.parseColor("#FF4444")
        val colorText  = android.graphics.Color.parseColor("#212121")

        fun updateFinalBalances() {
            val cashOpening    = etCash.text.toString().toLongOrNull() ?: 0L
            val accountOpening = etAccount.text.toString().toLongOrNull() ?: 0L
            val cashFinal      = cashOpening + cashNet
            val accountFinal   = accountOpening + bankNet
            tvCashFinal.text = if (cashFinal < 0L) "-${formatVND(cashFinal, currency)}"
                               else formatVND(cashFinal, currency)
            tvCashFinal.setTextColor(if (cashFinal < 0L) colorRed else colorText)
            tvAccountFinal.text = if (accountFinal < 0L) "-${formatVND(accountFinal, currency)}"
                                  else formatVND(accountFinal, currency)
            tvAccountFinal.setTextColor(if (accountFinal < 0L) colorRed else colorText)
        }

        fun syncFields() {
            lifecycleScope.launch {
                val userId = SessionManager.getCurrentUserId()
                val month  = pickerMonth.value + 1
                val year   = pickerYear.value
                val db     = CashieDatabase.getInstance(applicationContext)

                val entity = withContext(Dispatchers.IO) {
                    db.monthlyBalanceDao().getByMonthYear(userId, month, year)
                }

                // Month range for transaction query
                val cal = Calendar.getInstance()
                cal.set(year, month - 1, 1, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
                val startMs = cal.timeInMillis
                cal.set(year, month - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val endMs = cal.timeInMillis

                cashNet = cashWalletId?.let {
                    withContext(Dispatchers.IO) {
                        db.transactionDao().getNetByWallet(userId, it, startMs, endMs).toLong()
                    }
                } ?: 0L

                bankNet = bankWalletId?.let {
                    withContext(Dispatchers.IO) {
                        db.transactionDao().getNetByWallet(userId, it, startMs, endMs).toLong()
                    }
                } ?: 0L

                tvCashNet.text = if (cashNet >= 0) "+${formatVND(cashNet, currency)}"
                                 else "-${formatVND(-cashNet, currency)}"
                tvCashNet.setTextColor(if (cashNet >= 0) colorGreen else colorRed)
                tvAccountNet.text = if (bankNet >= 0) "+${formatVND(bankNet, currency)}"
                                    else "-${formatVND(-bankNet, currency)}"
                tvAccountNet.setTextColor(if (bankNet >= 0) colorGreen else colorRed)

                val savedCash    = entity?.cashBalance    ?: 0L
                val savedAccount = entity?.accountBalance ?: 0L
                etCash.setText(if (savedCash == 0L) "" else savedCash.toString())
                etAccount.setText(if (savedAccount == 0L) "" else savedAccount.toString())

                updateFinalBalances()
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateFinalBalances() }
        }
        etCash.addTextChangedListener(watcher)
        etAccount.addTextChangedListener(watcher)

        // Load wallet IDs once, then run initial sync
        lifecycleScope.launch {
            val userId  = SessionManager.getCurrentUserId()
            val wallets = withContext(Dispatchers.IO) {
                CashieDatabase.getInstance(applicationContext).walletDao().getAllByUserOnce(userId)
            }
            cashWalletId = wallets.firstOrNull { it.type == "cash" }?.id
            bankWalletId = wallets.firstOrNull { it.type == "bank" }?.id
            syncFields()
        }

        pickerMonth.setOnValueChangedListener { _, _, _ -> syncFields() }
        pickerYear.setOnValueChangedListener  { _, _, _ -> syncFields() }

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_balance_title))
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val cash    = etCash.text.toString().trim().toLongOrNull() ?: 0L
                val account = etAccount.text.toString().trim().toLongOrNull() ?: 0L
                val month   = pickerMonth.value + 1
                val year    = pickerYear.value

                lifecycleScope.launch {
                    val userId = SessionManager.getCurrentUserId()
                    withContext(Dispatchers.IO) {
                        CashieDatabase.getInstance(applicationContext)
                            .monthlyBalanceDao()
                            .upsert(
                                MonthlyBalanceEntity(
                                    userId         = userId,
                                    month          = month,
                                    year           = year,
                                    cashBalance    = cash,
                                    accountBalance = account
                                )
                            )
                    }
                    if (month == monthIndex + 1 && year == currentYear) refreshData()
                    Toast.makeText(this@MainActivity, R.string.toast_balance_saved, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColor)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColor)
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    private fun applyTheme() {
        val colorInt       = ThemeManager.getThemeColorInt()
        val colorStateList = ColorStateList.valueOf(colorInt)

        ThemeManager.applyToGradientCard(binding.balanceCard)

        binding.fab.backgroundTintList = colorStateList

        binding.bubbleNav.updateThemeColor(colorInt)
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
            .show().also { d ->
                val c = ThemeManager.getThemeColorInt()
                d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(c)
                d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(c)
            }
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
        setupBottomNav(binding.bubbleNav, R.id.nav_home)
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
