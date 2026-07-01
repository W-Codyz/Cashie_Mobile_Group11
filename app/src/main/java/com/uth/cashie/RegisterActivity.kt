package com.uth.cashie

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.DatabaseInitializer
import com.uth.cashie.database.SessionManager
import com.uth.cashie.database.entity.UserEntity
import com.uth.cashie.databinding.ActivityRegisterBinding
import com.uth.cashie.util.PasswordUtils
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

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            attemptRegister()
        }

        binding.tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun applyTheme() {
        ThemeManager.applyThemeToWindow(
            this,
            binding.appBarLayout,
            binding.tvToolbarTitle,
            binding.btnBack
        )

        ThemeManager.applyToGradientCard(binding.registerHeaderCard)
        ThemeManager.applyToButton(binding.btnRegister)

        binding.tvLoginLink.setTextColor(
            ThemeManager.getThemeColorInt()
        )

        listOf(
            binding.edtFullName,
            binding.edtUsername,
            binding.edtPassword,
            binding.edtConfirmPassword
        ).forEach {
            ThemeManager.applyToTextInput(it)
        }
    }

    private fun attemptRegister() {

        val fullName =
            binding.edtFullName.editText?.text.toString().trim()

        val username =
            binding.edtUsername.editText?.text.toString().trim()

        val password =
            binding.edtPassword.editText?.text.toString()

        val confirm =
            binding.edtConfirmPassword.editText?.text.toString()

        binding.edtFullName.error = null
        binding.edtUsername.error = null
        binding.edtPassword.error = null
        binding.edtConfirmPassword.error = null

        var hasError = false

        if (fullName.isEmpty()) {
            binding.edtFullName.error = "Vui lòng nhập họ và tên"
            hasError = true
        }

        if (username.isEmpty()) {
            binding.edtUsername.error = "Vui lòng nhập tên đăng nhập"
            hasError = true
        } else if (username.length < 4) {
            binding.edtUsername.error = "Tên đăng nhập phải từ 4 ký tự"
            hasError = true
        }

        if (password.length < 6) {
            binding.edtPassword.error = "Mật khẩu phải từ 6 ký tự"
            hasError = true
        }

        if (password != confirm) {
            binding.edtConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            hasError = true
        }

        if (hasError) return

        lifecycleScope.launch {

            val existed = withContext(Dispatchers.IO) {
                db.userDao().getByUsername(username)
            }

            if (existed != null) {
                binding.edtUsername.error = "Tên đăng nhập đã tồn tại"
                return@launch
            }

            val user = UserEntity(
                username = username,
                fullName = fullName,
                passwordHash = PasswordUtils.hash(password)
            )

            val userId = withContext(Dispatchers.IO) {
                val id = db.userDao().insert(user)
                DatabaseInitializer.setup(id, db)
                id
            }

            SessionManager.setCurrentUser(userId)

            Toast.makeText(
                this@RegisterActivity,
                "Đăng ký thành công",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(
                this@RegisterActivity,
                SetupActivity::class.java
            )

            intent.putExtra("fullName", fullName)

            startActivity(intent)
            finish()
        }
    }
}