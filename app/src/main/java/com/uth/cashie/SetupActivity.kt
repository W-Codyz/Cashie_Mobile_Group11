package com.uth.cashie

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.databinding.ActivitySetupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private val db     by lazy { CashieDatabase.getInstance(this) }
    private val userId by lazy { SessionManager.getCurrentUserId() }

    private var selectedCurrency = "VND"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()

        // Hiển thị tên chào mừng nếu được truyền từ RegisterActivity
        val fullName = intent.getStringExtra("fullName") ?: ""
        if (fullName.isNotEmpty()) {
            binding.tvSetupWelcome.text = "Chào mừng, $fullName!"
        }

        setupCurrencySelection()
        binding.btnStartUsing.setOnClickListener { saveSetup() }
    }

    private fun applyTheme() {
        val colorInt = ThemeManager.getThemeColorInt()
        val colorList = android.content.res.ColorStateList.valueOf(colorInt)
        ThemeManager.applyToGradientCard(binding.setupHeaderCard)
        ThemeManager.applyToButton(binding.btnStartUsing)
        binding.radioVnd.buttonTintList = colorList
        binding.radioUsd.buttonTintList = colorList
        binding.tvCurrencySymbolVnd.setTextColor(colorInt)
        ThemeManager.applyToTextInput(binding.tilBalance)
        binding.tilBalance.setSuffixTextColor(colorList)
    }

    // ── Toggle VND / USD ──────────────────────────────────────────────────────
    private fun setupCurrencySelection() {
        updateCurrencyUi()

        binding.cardVnd.setOnClickListener  { selectedCurrency = "VND"; updateCurrencyUi() }
        binding.radioVnd.setOnClickListener { selectedCurrency = "VND"; updateCurrencyUi() }
        binding.cardUsd.setOnClickListener  { selectedCurrency = "USD"; updateCurrencyUi() }
        binding.radioUsd.setOnClickListener { selectedCurrency = "USD"; updateCurrencyUi() }
    }

    private fun updateCurrencyUi() {
        if (selectedCurrency == "VND") {
            binding.cardVnd.setBackgroundResource(R.drawable.bg_currency_card_selected)
            binding.cardUsd.setBackgroundResource(R.drawable.bg_currency_card_default)
            binding.radioVnd.isChecked = true
            binding.radioUsd.isChecked = false
            binding.tilBalance.suffixText = getString(R.string.setup_suffix_vnd)
        } else {
            binding.cardVnd.setBackgroundResource(R.drawable.bg_currency_card_default)
            binding.cardUsd.setBackgroundResource(R.drawable.bg_currency_card_selected)
            binding.radioVnd.isChecked = false
            binding.radioUsd.isChecked = true
            binding.tilBalance.suffixText = getString(R.string.setup_suffix_usd)
        }
    }

    // ── Lưu thiết lập và vào MainActivity ────────────────────────────────────
    private fun saveSetup() {
        val balanceStr = binding.tilBalance.editText?.text?.toString()?.trim() ?: ""
        val balance    = balanceStr.toDoubleOrNull() ?: 0.0

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Cập nhật currency và initial_balance cho user
                val user = db.userDao().getById(userId) ?: return@withContext
                db.userDao().update(
                    user.copy(
                        currency       = selectedCurrency,
                        initialBalance = balance
                    )
                )
                // Cập nhật language mặc định vào app_settings
                db.appSettingsDao().updateLanguage(userId, ThemeManager.getLanguage())
            }

            startActivity(Intent(this@SetupActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}
