package com.uth.cashie

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.DatabaseInitializer
import com.uth.cashie.database.SessionManager
import com.uth.cashie.database.entity.UserEntity
import com.uth.cashie.database.util.PasswordUtils
import com.uth.cashie.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val db by lazy { CashieDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.tvLoginLink.setOnClickListener { finish() }
    }

    private fun applyTheme() {
        ThemeManager.applyThemeToWindow(this, binding.appBarLayout,
            binding.tvToolbarTitle, binding.btnBack)
        ThemeManager.applyToGradientCard(binding.registerHeaderCard)
        ThemeManager.applyToButton(binding.btnRegister)
        binding.tvLoginLink.setTextColor(ThemeManager.getThemeColorInt())
        listOf(binding.tilFullName, binding.tilUsername, binding.tilPassword, binding.tilConfirmPassword)
            .forEach { ThemeManager.applyToTextInput(it) }
    }

    private fun attemptRegister() {
        val fullName = binding.tilFullName.editText?.text?.toString()?.trim() ?: ""
        val username = binding.tilUsername.editText?.text?.toString()?.trim() ?: ""
        val password = binding.tilPassword.editText?.text?.toString() ?: ""
        val confirm  = binding.tilConfirmPassword.editText?.text?.toString() ?: ""

        // Reset lỗi cũ
        binding.tilFullName.error        = null
        binding.tilUsername.error        = null
        binding.tilPassword.error        = null
        binding.tilConfirmPassword.error = null

        // Validate
        var hasError = false
        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Vui lòng nhập họ và tên"
            hasError = true
        }
        if (username.isEmpty()) {
            binding.tilUsername.error = "Vui lòng nhập tên đăng nhập"
            hasError = true
        } else if (username.length < 4) {
            binding.tilUsername.error = "Tên đăng nhập phải từ 4 ký tự"
            hasError = true
        }
        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_too_short)
            hasError = true
        }
        if (password != confirm) {
            binding.tilConfirmPassword.error = getString(R.string.error_password_mismatch)
            hasError = true
        }
        if (hasError) return

        lifecycleScope.launch {
            // Kiểm tra username đã tồn tại chưa
            val exists = withContext(Dispatchers.IO) {
                db.userDao().usernameExists(username) > 0
            }
            if (exists) {
                binding.tilUsername.error = "Tên đăng nhập đã được sử dụng"
                return@launch
            }

            // Tạo user mới
            val newUser = UserEntity(
                username     = username,
                fullName     = fullName,
                passwordHash = PasswordUtils.hash(password)
            )

            val userId = withContext(Dispatchers.IO) {
                val id = db.userDao().insert(newUser)
                // Tạo danh mục mặc định + app settings cho user mới
                DatabaseInitializer.setup(id, db)
                id
            }

            // Lưu session
            SessionManager.setCurrentUser(userId)

            // Sang SetupActivity để thiết lập ban đầu
            val intent = Intent(this@RegisterActivity, SetupActivity::class.java).apply {
                putExtra("fullName", fullName)
            }
            startActivity(intent)
            finish()
        }
    }
}
