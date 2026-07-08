package com.uth.cashie

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.database.entity.SavedAccountEntity
import com.uth.cashie.databinding.ActivityLoginBinding
import com.uth.cashie.database.util.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val db by lazy { CashieDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()
        loadSavedAccounts()

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Tải danh sách tài khoản đã đăng nhập trên thiết bị để hiển thị gợi ý.
     * Ưu tiên dùng saved_accounts (có avatar, full name), fallback về getAllUsernames().
     */
    private fun loadSavedAccounts() {
        lifecycleScope.launch {
            val savedAccounts = withContext(Dispatchers.IO) {
                db.savedAccountDao().getAll()
            }

            val usernames = if (savedAccounts.isNotEmpty()) {
                savedAccounts.map { it.username }
            } else {
                withContext(Dispatchers.IO) { db.userDao().getAllUsernames() }
            }

            val adapter = ArrayAdapter(
                this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line,
                usernames
            )
            binding.edtUsername.setAdapter(adapter)

            // Nếu có tài khoản đã được nhớ trước đó, điền sẵn username
            val remembered = withContext(Dispatchers.IO) {
                db.savedAccountDao().getRemembered()
            }
            if (remembered != null) {
                binding.edtUsername.setText(remembered.username)
                binding.cbRemember.isChecked = true
            }
        }
    }

    private fun applyTheme() {
        val colorInt = ThemeManager.getThemeColorInt()
        binding.tvAppName.setTextColor(colorInt)
        binding.tvRegister.setTextColor(colorInt)
        binding.cbRemember.setTextColor(colorInt)
        binding.cbRemember.buttonTintList = android.content.res.ColorStateList.valueOf(colorInt)
        ThemeManager.applyToButton(binding.btnLogin)
        ThemeManager.applyToTextInput(binding.tilUsername)
        ThemeManager.applyToTextInput(binding.tilPassword)
    }

    private fun attemptLogin() {
        val username = binding.edtUsername.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()
        val rememberMe = binding.cbRemember.isChecked

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
                Toast.makeText(this@LoginActivity, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (user.passwordHash != PasswordUtils.hash(password)) {
                Toast.makeText(this@LoginActivity, "Sai mật khẩu", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Lưu session
            SessionManager.setCurrentUser(user.id)
            SessionManager.setRememberLogin(rememberMe)

            // Cập nhật bảng saved_accounts
            withContext(Dispatchers.IO) {
                val existing = db.savedAccountDao().getByUserId(user.id)
                if (existing == null) {
                    db.savedAccountDao().insert(
                        SavedAccountEntity(
                            userId        = user.id,
                            username      = user.username,
                            fullName      = user.fullName,
                            avatarPath    = user.avatarPath ?: user.avatar,
                            rememberLogin = if (rememberMe) 1 else 0,
                            lastLoginAt   = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Cập nhật thời gian và trạng thái nhớ đăng nhập
                    db.savedAccountDao().updateRememberLogin(user.id, if (rememberMe) 1 else 0)
                    db.savedAccountDao().updateLastLogin(user.id)
                }

                // Nếu không nhớ thì reset tất cả tài khoản khác về 0
                if (rememberMe) {
                    val all = db.savedAccountDao().getAll()
                    all.filter { it.userId != user.id }.forEach { acc ->
                        db.savedAccountDao().updateRememberLogin(acc.userId, 0)
                    }
                }
            }

            // Khôi phục cài đặt theme và ngôn ngữ
            val settings = withContext(Dispatchers.IO) {
                db.appSettingsDao().getByUserId(user.id)
            }
            settings?.let {
                ThemeManager.setThemeColor(it.themeColor)
                ThemeManager.setLanguage(it.language)
            }

            Toast.makeText(this@LoginActivity, getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show()
            goToMain()
        }
    }

    private fun goToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
