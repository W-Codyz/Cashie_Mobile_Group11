package com.uth.cashie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uth.cashie.databinding.ActivityRegisterBinding
import android.widget.Toast
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.entity.UserEntity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.uth.cashie.database.DatabaseInitializer
import com.uth.cashie.util.PasswordUtils

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            Toast.makeText(
                this,
                "Nút đăng ký đã được bấm",
                Toast.LENGTH_SHORT
            ).show()

            val username = binding.edtUsername.editText?.text.toString().trim()
            val fullName = binding.edtFullName.editText?.text.toString().trim()
            val password = binding.edtPassword.editText?.text.toString().trim()
            val confirmPassword = binding.edtConfirmPassword.editText?.text.toString().trim()

            if (username.isEmpty() ||
                fullName.isEmpty() ||
                password.isEmpty() ||
                confirmPassword.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Vui lòng nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(
                    this,
                    "Mật khẩu xác nhận không khớp",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {

                val db = CashieDatabase.getInstance(this@RegisterActivity)

                val existed =
                    db.userDao().getByUsername(username)

                if (existed != null) {

                    Toast.makeText(
                        this@RegisterActivity,
                        "Tên đăng nhập đã tồn tại",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                val user = UserEntity(
                    username = username,
                    fullName = fullName,
                    passwordHash = PasswordUtils.hash(password)
                )

                val userId =
                    db.userDao().insert(user)

                DatabaseInitializer.setup(
                    userId,
                    db
                )

                Toast.makeText(
                    this@RegisterActivity,
                    "Đăng ký thành công",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }
        binding.tvLoginLink.setOnClickListener {
            // TODO: navigate to LoginActivity
            finish()
        }
    }

}
