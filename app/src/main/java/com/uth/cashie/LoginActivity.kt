package com.uth.cashie

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.databinding.ActivityLoginBinding
import com.uth.cashie.util.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val db by lazy { CashieDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SessionManager.isLoggedIn()) {
            restoreSettingsAndLaunch()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun applyTheme() {
        val colorInt = ThemeManager.getThemeColorInt()
        binding.tvAppName.setTextColor(colorInt)
        binding.tvRegister.setTextColor(colorInt)
        ThemeManager.applyToButton(binding.btnLogin)
        ThemeManager.applyToTextInput(binding.tilUsername)
        ThemeManager.applyToTextInput(binding.tilPassword)
    }

    private fun attemptLogin() {

        val username = binding.edtUsername.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tài khoản", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {

            val user = withContext(Dispatchers.IO) {
                db.userDao().getByUsername(username)
            }

            if (user == null) {
                Toast.makeText(
                    this@LoginActivity,
                    "Không tìm thấy tài khoản",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            if (user.passwordHash != PasswordUtils.hash(password)) {
                Toast.makeText(
                    this@LoginActivity,
                    "Sai mật khẩu",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            SessionManager.setCurrentUser(user.id)

            val settings = withContext(Dispatchers.IO) {
                db.appSettingsDao().getByUserId(user.id)
            }

            settings?.let {
                ThemeManager.setThemeColor(it.themeColor)
                ThemeManager.setLanguage(it.language)
                applyLocale(it.language)
            }

            Toast.makeText(
                this@LoginActivity,
                "Đăng nhập thành công",
                Toast.LENGTH_SHORT
            ).show()

            goToMain()
        }
    }

    private fun restoreSettingsAndLaunch() {
        lifecycleScope.launch {

            val settings = withContext(Dispatchers.IO) {
                db.appSettingsDao().getByUserId(SessionManager.getCurrentUserId())
            }

            settings?.let {
                ThemeManager.setThemeColor(it.themeColor)
                ThemeManager.setLanguage(it.language)
                applyLocale(it.language)
            }

            goToMain()
        }
    }

    private fun goToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    private fun applyLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }
}