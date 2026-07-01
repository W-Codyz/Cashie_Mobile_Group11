package com.uth.cashie

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uth.cashie.databinding.ActivityLoginBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.util.PasswordUtils
import android.util.Log

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Chuyển sang màn hình đăng ký
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Xử lý đăng nhập
        binding.btnLogin.setOnClickListener {

            val username = binding.edtUsername.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tài khoản", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userDao = CashieDatabase.getInstance(this@LoginActivity).userDao()

            lifecycleScope.launch {

                // In tất cả user đang có trong database
                val allUsers = userDao.getAll()
                allUsers.forEach {
                    android.util.Log.d("LOGIN", "DB Username = '${it.username}'")
                }

                // In username người dùng vừa nhập
                android.util.Log.d("LOGIN", "Username nhập = '$username'")

                val user = userDao.getByUsername(username)

                if (user == null) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Không tìm thấy tài khoản",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val passwordHash = PasswordUtils.hash(password)

                if (user.passwordHash == passwordHash) {
                    Toast.makeText(this@LoginActivity, "Đăng nhập thành công", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Sai mật khẩu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
