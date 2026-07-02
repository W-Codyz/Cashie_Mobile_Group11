package com.uth.cashie

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

    private var avatarUri: Uri? = null

    private lateinit var binding: ActivityRegisterBinding
    private val db by lazy { CashieDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener { validateAndShowConfirm() }

        binding.tvLoginLink.setOnClickListener { finish() }

        binding.btnChooseImage.setOnClickListener { pickImage.launch("image/*") }
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri
            binding.imgAvatar.setImageURI(uri)
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
        binding.tvLoginLink.setTextColor(ThemeManager.getThemeColorInt())
        listOf(
            binding.edtFullName,
            binding.edtUsername,
            binding.tilEmail,
            binding.edtPassword,
            binding.edtConfirmPassword
        ).forEach { ThemeManager.applyToTextInput(it) }
    }

    /**
     * Bước 1: Validate form, nếu hợp lệ thì hiển thị dialog xác nhận thông tin.
     */
    private fun validateAndShowConfirm() {
        val fullName = binding.edtFullName.editText?.text.toString().trim()
        val username = binding.edtUsername.editText?.text.toString().trim()
        val password = binding.edtPassword.editText?.text.toString()
        val confirm  = binding.edtConfirmPassword.editText?.text.toString()
        val email    = binding.edtEmail.text.toString().trim()

        // Reset errors
        binding.edtFullName.error         = null
        binding.edtUsername.error         = null
        binding.tilEmail.error            = null
        binding.edtPassword.error         = null
        binding.edtConfirmPassword.error  = null

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

        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email không hợp lệ"
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

        showConfirmDialog(fullName, username, email, password)
    }

    /**
     * Bước 2: Hiển thị dialog xác nhận thông tin trước khi đăng ký.
     */
    private fun showConfirmDialog(
        fullName: String,
        username: String,
        email: String,
        password: String
    ) {
        val emailDisplay = if (email.isEmpty()) "(không nhập)" else email
        val avatarDisplay = if (avatarUri != null) "Đã chọn ảnh" else "(không chọn)"

        AlertDialog.Builder(this)
            .setTitle("Xác nhận thông tin đăng ký")
            .setMessage(
                "Họ tên: $fullName\n\n" +
                "Tên đăng nhập: $username\n\n" +
                "Email: $emailDisplay\n\n" +
                "Ảnh đại diện: $avatarDisplay\n\n" +
                "Bạn có chắc muốn tạo tài khoản?"
            )
            .setPositiveButton("Xác nhận") { _, _ ->
                registerUser(fullName, username, email, password)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    /**
     * Bước 3: Thực sự đăng ký — kiểm tra trùng username rồi insert vào DB.
     */
    private fun registerUser(
        fullName: String,
        username: String,
        email: String,
        password: String
    ) {
        lifecycleScope.launch {
            val existed = withContext(Dispatchers.IO) {
                db.userDao().getByUsername(username)
            }

            if (existed != null) {
                binding.edtUsername.error = "Tên đăng nhập đã tồn tại"
                return@launch
            }

            val user = UserEntity(
                username     = username,
                fullName     = fullName,
                email        = email,
                passwordHash = PasswordUtils.hash(password),
                avatar       = avatarUri?.toString()
            )

            val userId = withContext(Dispatchers.IO) {
                val id = db.userDao().insert(user)
                DatabaseInitializer.setup(id, db)
                id
            }

            // Lưu session để SetupActivity biết user nào
            SessionManager.setCurrentUser(userId)

            Toast.makeText(
                this@RegisterActivity,
                "Đăng ký thành công! Hãy thiết lập tài khoản.",
                Toast.LENGTH_SHORT
            ).show()

            // Chuyển sang SetupActivity thay vì LoginActivity
            startActivity(
                Intent(this@RegisterActivity, SetupActivity::class.java).apply {
                    putExtra("fullName", fullName)
                }
            )
            finish()
        }
    }
}
