package com.uth.cashie

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.database.util.PasswordUtils
import com.uth.cashie.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val db by lazy { CashieDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nếu đã đăng nhập (remember login) → vào thẳng MainActivity
        if (SessionManager.isLoggedIn()) {
            restoreSettingsAndLaunch()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()
        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun applyTheme() {
        val colorInt = ThemeManager.getThemeColorInt()
        binding.tvAppName.setTextColor(colorInt)
        binding.txtRegister.setTextColor(colorInt)
        ThemeManager.applyToButton(binding.btnLogin)
        ThemeManager.applyToTextInput(binding.tilUsername)
        ThemeManager.applyToTextInput(binding.tilPassword)
    }

    // ── Đăng nhập ─────────────────────────────────────────────────────────────
    private fun attemptLogin() {
        val username = binding.edtUsername.text?.toString()?.trim() ?: ""
        val password = binding.edtPassword.text?.toString() ?: ""

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                db.userDao().login(username, PasswordUtils.hash(password))
            }

            if (user == null) {
                Toast.makeText(
                    this@LoginActivity,
                    "Tên đăng nhập hoặc mật khẩu không đúng",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Lưu session
            SessionManager.setCurrentUser(user.id)

            // Load và áp settings (màu, ngôn ngữ) trước khi vào app
            val settings = withContext(Dispatchers.IO) {
                db.appSettingsDao().getByUserId(user.id)
            }
            settings?.let { s ->
                ThemeManager.setThemeColor(s.themeColor)
                ThemeManager.setLanguage(s.language)
                applyLocale(s.language)
            }

            goToMain()
        }
    }

    // ── Khôi phục settings nếu đã login trước đó ─────────────────────────────
    private fun restoreSettingsAndLaunch() {
        lifecycleScope.launch {
            val settings = withContext(Dispatchers.IO) {
                db.appSettingsDao().getByUserId(SessionManager.getCurrentUserId())
            }
            settings?.let { s ->
                ThemeManager.setThemeColor(s.themeColor)
                ThemeManager.setLanguage(s.language)
                applyLocale(s.language)
            }
            goToMain()
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    // ── Áp ngôn ngữ ───────────────────────────────────────────────────────────
    private fun applyLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
