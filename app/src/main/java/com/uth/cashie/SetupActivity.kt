package com.uth.cashie

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.databinding.ActivitySetupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetupActivity : BaseActivity() {

    private lateinit var binding: ActivitySetupBinding
    private val db     by lazy { CashieDatabase.getInstance(this) }
    private val userId by lazy { SessionManager.getCurrentUserId() }

    private var selectedCurrency = "VND"

    /** Ngôn ngữ được chọn — mặc định "vi", cập nhật khi user chọn radio */
    private var selectedLanguage = "vi"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()

        val fullName = intent.getStringExtra("fullName") ?: ""
        if (fullName.isNotEmpty()) {
            binding.tvSetupWelcome.text = "Chào mừng, $fullName!"
        }

        setupCurrencySelection()
        setupLanguageSelection()

        binding.btnStartUsing.setOnClickListener { showConfirmSetupDialog() }
    }

    private fun applyTheme() {
        val colorInt = ThemeManager.getThemeColorInt()
        val colorList = android.content.res.ColorStateList.valueOf(colorInt)
        ThemeManager.applyToGradientCard(binding.setupHeaderCard)
        ThemeManager.applyToButton(binding.btnStartUsing)
        binding.radioVnd.buttonTintList = colorList
        binding.radioUsd.buttonTintList = colorList
        binding.rbVietnamese.buttonTintList = colorList
        binding.rbEnglish.buttonTintList = colorList
        binding.tvCurrencySymbolVnd.setTextColor(colorInt)
        ThemeManager.applyToTextInput(binding.tilBalance)
        binding.tilBalance.setSuffixTextColor(colorList)
    }

    // ── Chọn ngôn ngữ ────────────────────────────────────────────────────────
    private fun setupLanguageSelection() {
        // Đặt trạng thái mặc định
        binding.rbVietnamese.isChecked = true

        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            selectedLanguage = when (checkedId) {
                R.id.rbEnglish    -> "en"
                else              -> "vi"
            }
        }
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

    // ── Dialog xác nhận thiết lập trước khi lưu ──────────────────────────────
    private fun showConfirmSetupDialog() {
        val balanceStr = binding.tilBalance.editText?.text?.toString()?.trim() ?: ""
        val balance    = balanceStr.toDoubleOrNull() ?: 0.0
        val langDisplay = if (selectedLanguage == "en") "English" else "Tiếng Việt"
        val currencyDisplay = selectedCurrency
        val balanceDisplay = if (balance == 0.0) "0" else balance.toLong().toString()

        AlertDialog.Builder(this)
            .setTitle("Xác nhận thiết lập")
            .setMessage(
                "Tiền tệ: $currencyDisplay\n\n" +
                "Ngôn ngữ: $langDisplay\n\n" +
                "Số dư ban đầu: $balanceDisplay $currencyDisplay\n\n" +
                "Bạn có muốn bắt đầu sử dụng Cashie?"
            )
            .setPositiveButton("Bắt đầu") { _, _ ->
                saveSetup(balance)
            }
            .setNegativeButton("Chỉnh sửa lại", null)
            .show()
    }

    // ── Lưu thiết lập và vào MainActivity ────────────────────────────────────
    private fun saveSetup(balance: Double) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val user = db.userDao().getById(userId) ?: return@withContext
                db.userDao().update(
                    user.copy(
                        currency       = selectedCurrency,
                        initialBalance = balance
                    )
                )
                // Lưu ngôn ngữ đã chọn vào app_settings và ThemeManager
                db.appSettingsDao().updateLanguage(userId, selectedLanguage)
            }

            // Lưu ngôn ngữ vào ThemeManager để dùng ngay trong session này
            ThemeManager.setLanguage(selectedLanguage)
            // Lưu tiền tệ vào SessionManager
            SessionManager.setCurrency(selectedCurrency)

            startActivity(Intent(this@SetupActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}
