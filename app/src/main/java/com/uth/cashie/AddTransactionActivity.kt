package com.uth.cashie

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.uth.cashie.adapter.EmojiCategoryAdapter
import com.uth.cashie.data.TransactionRepository
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.databinding.ActivityAddTransactionBinding
import com.uth.cashie.model.EmojiCategory
import com.uth.cashie.model.Transaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private val categoryAdapter = EmojiCategoryAdapter { cat -> selectedCategory = cat }

    private var isIncome = true
    private var selectedCategory: EmojiCategory? = null
    private var selectedDateMs: Long = System.currentTimeMillis()
    private var selectedWalletId: Long? = null
    private var selectedWalletName: String = ""
    private var editingId: Int = -1
    private var originalTime: String? = null

    private val storedDateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFmt = SimpleDateFormat("dd 'tháng' MM, yyyy", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingId = intent.getIntExtra(EXTRA_TRANSACTION_ID, -1)

        applyTheme()
        setupCategoryRecyclerView()
        setupToggle()
        setupDatePicker()
        setupWalletPicker()
        setupSaveButton()
        setupBack()

        if (editingId != -1) {
            loadTransactionForEdit()
        } else {
            updateDateDisplay(selectedDateMs)
            switchType(income = false, animate = false)
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        categoryAdapter.notifyDataSetChanged()
    }

    private fun applyTheme() {
        val themeColor = ThemeManager.getThemeColorInt()
        binding.btnSaveTransaction.backgroundTintList = android.content.res.ColorStateList.valueOf(themeColor)
    }

    // ── Category grid ──────────────────────────────────────────────────────────

    private fun setupCategoryRecyclerView() {
        binding.rvCategories.layoutManager = GridLayoutManager(this, 5)
        binding.rvCategories.adapter = categoryAdapter
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val db = CashieDatabase.getInstance(this@AddTransactionActivity)
            val categoryRepo = com.uth.cashie.category.data.CategoryRepository(db.categoryDao())
            val list = categoryRepo.getCategoriesByTypeOrderedByUsage(
                SessionManager.getCurrentUserId(),
                if (isIncome) "income" else "expense"
            )
            categoryAdapter.submitList(list.ifEmpty {
                if (isIncome) EmojiCategory.INCOME else EmojiCategory.EXPENSE
            })
        }
    }

    // ── Type toggle ────────────────────────────────────────────────────────────

    private fun setupToggle() {
        binding.btnToggleIncome.setOnClickListener { switchType(income = true) }
        binding.btnToggleExpense.setOnClickListener { switchType(income = false) }
    }

    private fun switchType(income: Boolean, animate: Boolean = true) {
        isIncome = income
        val themeColor = ThemeManager.getThemeColorInt()

        if (income) {
            binding.btnToggleIncome.setBackgroundResource(R.drawable.bg_button_primary)
            binding.btnToggleIncome.backgroundTintList = android.content.res.ColorStateList.valueOf(themeColor)
            binding.btnToggleIncome.setTextColor(getColor(android.R.color.white))
            binding.btnToggleExpense.setBackgroundResource(android.R.color.transparent)
            binding.btnToggleExpense.setTextColor(android.graphics.Color.parseColor("#888888"))
        } else {
            binding.btnToggleExpense.setBackgroundResource(R.drawable.bg_button_primary)
            binding.btnToggleExpense.backgroundTintList = android.content.res.ColorStateList.valueOf(themeColor)
            binding.btnToggleExpense.setTextColor(getColor(android.R.color.white))
            binding.btnToggleIncome.setBackgroundResource(android.R.color.transparent)
            binding.btnToggleIncome.setTextColor(android.graphics.Color.parseColor("#888888"))
        }
        // Reset category selection when type changes
        selectedCategory = null
        loadCategories()
    }

    // ── Date picker ────────────────────────────────────────────────────────────

    private fun setupDatePicker() {
        binding.layoutPickDate.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMs }
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val picked = Calendar.getInstance().apply { set(year, month, day) }
                selectedDateMs = picked.timeInMillis
                updateDateDisplay(selectedDateMs)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay(ms: Long) {
        binding.tvSelectedDate.text = displayDateFmt.format(Date(ms))
    }

    // ── Wallet picker ────────────────────────────────────────────

    private fun setupWalletPicker() {
        loadWallets()
        binding.layoutPickWallet.setOnClickListener {
            showWalletPickerDialog()
        }
    }

    private fun loadWallets() {
        lifecycleScope.launch {
            val db = CashieDatabase.getInstance(this@AddTransactionActivity)
            val wallets = db.walletDao().getAllByUserOnce(SessionManager.getCurrentUserId())
            if (wallets.isNotEmpty()) {
                val defaultWallet = wallets.firstOrNull { it.isDefault == 1 } ?: wallets.first()
                selectedWalletId = defaultWallet.id
                selectedWalletName = defaultWallet.name
                binding.tvSelectedWallet.text = selectedWalletName
            }
        }
    }

    private fun showWalletPickerDialog() {
        lifecycleScope.launch {
            val db = CashieDatabase.getInstance(this@AddTransactionActivity)
            val wallets = db.walletDao().getAllByUserOnce(SessionManager.getCurrentUserId())
            val walletNames = wallets.map { it.name }.toTypedArray()
            val currentSelectedIndex = wallets.indexOfFirst { it.id == selectedWalletId }
            
            androidx.appcompat.app.AlertDialog.Builder(this@AddTransactionActivity)
                .setTitle("Chọn ví")
                .setSingleChoiceItems(walletNames, if (currentSelectedIndex >= 0) currentSelectedIndex else 0) { dialog, which ->
                    val selectedWallet = wallets[which]
                    selectedWalletId = selectedWallet.id
                    selectedWalletName = selectedWallet.name
                    binding.tvSelectedWallet.text = selectedWalletName
                    dialog.dismiss()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    // ── Save ───────────────────────────────────────────────────────────────────

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener { saveTransaction() }
        binding.btnSaveTransaction.setOnClickListener { saveTransaction() }
    }

    private fun saveTransaction() {
        val rawAmount = binding.etAmount.text.toString()
            .replace(",", "").replace(".", "").trim()
        val amountValue = rawAmount.toLongOrNull() ?: 0L

        if (amountValue <= 0L) {
            Toast.makeText(this, getString(R.string.error_amount_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val cat = selectedCategory
        if (cat == null) {
            Toast.makeText(this, getString(R.string.error_category_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val noteText = binding.etNote.text.toString().trim()
        val title = noteText.ifEmpty { cat.name }
        val finalAmount = if (isIncome) amountValue else -amountValue
        val dateStr = storedDateFmt.format(Date(selectedDateMs))
        val timeStr = if (editingId != -1) originalTime ?: timeFmt.format(Date()) else timeFmt.format(Date())

        lifecycleScope.launch {
            if (editingId != -1) {
                val updated = Transaction(
                    id           = editingId,
                    title        = title,
                    category     = cat.name,
                    emoji        = cat.emoji,
                    iconColorHex = cat.colorHex,
                    amount       = finalAmount,
                    isIncome     = isIncome,
                    time         = timeStr,
                    date         = dateStr,
                    walletId     = selectedWalletId,
                    walletName   = selectedWalletName
                )
                TransactionRepository.update(updated)
                Toast.makeText(this@AddTransactionActivity, getString(R.string.toast_transaction_updated), Toast.LENGTH_SHORT).show()
            } else {
                val newTx = Transaction(
                    id           = 0,
                    title        = title,
                    category     = cat.name,
                    emoji        = cat.emoji,
                    iconColorHex = cat.colorHex,
                    amount       = finalAmount,
                    isIncome     = isIncome,
                    time         = timeStr,
                    date         = dateStr,
                    walletId     = selectedWalletId,
                    walletName   = selectedWalletName
                )
                TransactionRepository.add(newTx)
                Toast.makeText(this@AddTransactionActivity, getString(R.string.toast_transaction_saved), Toast.LENGTH_SHORT).show()
            }

            setResult(RESULT_OK)
            finish()
        }
    }

    // ── Load for edit ──────────────────────────────────────────────────────────

    private fun loadTransactionForEdit() {
        lifecycleScope.launch {
            val tx = TransactionRepository.getById(editingId) ?: run { finish(); return@launch }
            originalTime = tx.time

            switchType(income = tx.isIncome, animate = false)

            // Pre-fill amount (absolute value)
            val absAmount = if (tx.amount < 0) -tx.amount else tx.amount
            binding.etAmount.setText(absAmount.toString())

            // Pre-fill note
            binding.etNote.setText(tx.title)

            // Pre-select date
            runCatching {
                val parsed = storedDateFmt.parse(tx.date)
                if (parsed != null) {
                    selectedDateMs = parsed.time
                    updateDateDisplay(selectedDateMs)
                }
            }

            // Pre-select wallet
            if (tx.walletId != null) {
                selectedWalletId = tx.walletId
                selectedWalletName = tx.walletName ?: ""
                binding.tvSelectedWallet.text = selectedWalletName
            }

            // Pre-select category after list is loaded
            categoryAdapter.selectByName(tx.category)
            selectedCategory = EmojiCategory(tx.category, tx.emoji, tx.iconColorHex)
        }
    }

    // ── Back ───────────────────────────────────────────────────────────────────

    private fun setupBack() {
        binding.btnBack.setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
    }
}
