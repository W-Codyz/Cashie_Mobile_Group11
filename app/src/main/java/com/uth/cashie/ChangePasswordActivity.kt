package com.uth.cashie

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.util.PasswordUtils
import com.uth.cashie.databinding.ActivityChangePasswordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.uth.cashie.database.SessionManager

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val db     by lazy { CashieDatabase.getInstance(this) }
    private val userId by lazy { SessionManager.getCurrentUserId() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnChangePassword.setOnClickListener { attemptChangePassword() }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    // ── Áp dụng màu chủ đề ───────────────────────────────────────────────────
    private fun applyTheme() {
        ThemeManager.applyThemeToWindow(this, binding.appBarLayout,
            binding.tvToolbarTitle, binding.btnBack)
        ThemeManager.applyToButton(binding.btnChangePassword)
        ThemeManager.applyToTextInput(binding.tilCurrentPassword)
        ThemeManager.applyToTextInput(binding.tilNewPassword)
        ThemeManager.applyToTextInput(binding.tilConfirmNewPassword)
    }

    private fun attemptChangePassword() {
        val current = binding.etCurrentPassword.text?.toString() ?: ""
        val newPw   = binding.etNewPassword.text?.toString() ?: ""
        val confirm = binding.etConfirmNewPassword.text?.toString() ?: ""

        // Reset errors
        binding.tilCurrentPassword.error    = null
        binding.tilNewPassword.error        = null
        binding.tilConfirmNewPassword.error = null

        // Validate
        if (current.isEmpty()) {
            binding.tilCurrentPassword.error = getString(R.string.error_current_password_empty)
            return
        }
        if (newPw.length < 6) {
            binding.tilNewPassword.error = getString(R.string.error_password_too_short)
            return
        }
        if (newPw != confirm) {
            binding.tilConfirmNewPassword.error = getString(R.string.error_password_mismatch)
            return
        }

        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { db.userDao().getById(userId) }
            user ?: return@launch

            if (!PasswordUtils.verify(current, user.passwordHash)) {
                binding.tilCurrentPassword.error = getString(R.string.error_wrong_password)
                return@launch
            }

            val newHash = PasswordUtils.hash(newPw)
            withContext(Dispatchers.IO) {
                db.userDao().update(user.copy(passwordHash = newHash))
            }

            Toast.makeText(
                this@ChangePasswordActivity,
                getString(R.string.toast_password_changed),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
