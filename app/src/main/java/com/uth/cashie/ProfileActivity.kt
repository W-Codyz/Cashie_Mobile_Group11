package com.uth.cashie

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.databinding.ActivityProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import com.uth.cashie.database.SessionManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val db by lazy { CashieDatabase.getInstance(this) }
    private val userId by lazy { SessionManager.getCurrentUserId() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()
        setupButtons()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        onNewIntentBottomNav(intent, binding.bubbleNav, R.id.nav_profile)
    }

    override fun onResume() {
        super.onResume()
        // Reload mỗi khi quay lại từ EditProfile
        applyTheme()
        loadProfile()
        loadMonthStats()
    }

    // ── Áp dụng màu chủ đề ───────────────────────────────────────────────────
    private fun applyTheme() {
        val colorInt      = ThemeManager.getThemeColorInt()
        val onThemeColor  = ThemeManager.getOnThemeColor()   // trắng hoặc đen tùy độ sáng
        val containerBg   = ThemeManager.getSolidContainerColor()  // nền nhạt icon circle
        val colorStateList = android.content.res.ColorStateList.valueOf(colorInt)

        // Toolbar + status bar + màu chữ toolbar tự động
        ThemeManager.applyThemeToWindow(this, binding.appBarLayout,
            binding.tvProfileTitle, binding.btnMenu)
        binding.btnEdit.setTextColor(onThemeColor)
        binding.bubbleNav.updateThemeColor(colorInt)

        // Gradient header card
        ThemeManager.applyToGradientCard(binding.profileHeaderCard)

        // Avatar background and tint
        val avatarBg = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(colorInt)
        }
        binding.ivAvatar.background = avatarBg

        // Avatar icon tint — chỉ áp dụng khi chưa có ảnh thật
        // (khi có ảnh thật, imageTintList đã được set null trong loadProfile())
        if (binding.ivAvatar.imageTintList != null) {
            binding.ivAvatar.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
            binding.ivAvatar.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            binding.ivAvatar.setPadding(18, 18, 18, 18)
        }

        // Income stat value — dùng màu theme
        binding.tvStatIncomeValue.setTextColor(ThemeManager.getThemeColorInt())
        val circleBg = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(containerBg)
        }
        listOf(
            binding.iconCircleEditInfo       to binding.iconEditInfo,
            binding.iconCircleChangePassword to binding.iconChangePassword,
            binding.iconCircleCurrency       to binding.iconCurrency,
            binding.iconCircleStats          to binding.iconStats,
        ).forEach { (circle, icon) ->
            circle.background  = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(containerBg)
            }
            icon.imageTintList = colorStateList
        }
    }

    // ── Load thông tin người dùng ─────────────────────────────────────────────
    private fun loadProfile() {
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { db.userDao().getById(userId) }
            user ?: return@launch

            // Tên & username
            binding.tvProfileName.text     = user.fullName
            binding.tvProfileUsername.text = "@${user.username}"
            binding.tvCurrencyBadge.text   = user.currency

            // Avatar: nếu có đường dẫn file thì load, không thì giữ icon mặc định
            if (!user.avatarPath.isNullOrBlank()) {
                val file = File(user.avatarPath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    binding.ivAvatar.setImageBitmap(bmp)
                    // Tắt tint xanh khi có ảnh thật
                    binding.ivAvatar.imageTintList = null
                    binding.ivAvatar.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    binding.ivAvatar.setPadding(0, 0, 0, 0)
                }
            }
        }
    }

    // ── Load thống kê tháng hiện tại ──────────────────────────────────────────
    private fun loadMonthStats() {
        val cal   = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val startMs = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val endMs = cal.timeInMillis

        lifecycleScope.launch {
            val income = withContext(Dispatchers.IO) {
                db.transactionDao().getTotalIncome(userId, startMs, endMs)
            }
            val expense = withContext(Dispatchers.IO) {
                db.transactionDao().getTotalExpense(userId, startMs, endMs)
            }
            val count = withContext(Dispatchers.IO) {
                db.transactionDao().getCount(userId, startMs, endMs)
            }

            val currency = SessionManager.getCurrency()
            binding.tvStatIncomeValue.text      = formatVND(income.toLong(), currency)
            binding.tvStatExpenseValue.text     = formatVND(expense.toLong(), currency)
            binding.tvStatTransactionsValue.text = count.toString()
        }
    }

    // ── Buttons ───────────────────────────────────────────────────────────────
    private fun setupButtons() {
        binding.btnMenu.setOnClickListener { showNavMenu(NavScreen.PROFILE) }
        setupBottomNav(binding.bubbleNav, R.id.nav_profile)

        binding.btnEdit.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        binding.menuEditInfo.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.menuChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        binding.menuCurrency.setOnClickListener {
            showCurrencyDialog()
        }

        binding.menuStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        binding.menuLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    // ── Dialog đổi tiền tệ nhanh ──────────────────────────────────────────────
    private fun showCurrencyDialog() {
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { db.userDao().getById(userId) } ?: return@launch
            val options = arrayOf("VND — Việt Nam Đồng", "USD — US Dollar")
            val current = if (user.currency == "USD") 1 else 0

            AlertDialog.Builder(this@ProfileActivity)
                .setTitle(getString(R.string.profile_menu_currency))
                .setSingleChoiceItems(options, current) { dialog, which ->
                    val currency = if (which == 1) "USD" else "VND"
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.userDao().update(user.copy(currency = currency))
                    }
                    SessionManager.setCurrency(currency)
                    binding.tvCurrencyBadge.text = currency
                    dialog.dismiss()
                    Toast.makeText(this@ProfileActivity, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    // ── Dialog đăng xuất ─────────────────────────────────────────────────────
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.setting_logout_confirm_title))
            .setMessage(getString(R.string.setting_logout_confirm_msg))
            .setPositiveButton(getString(R.string.setting_logout_confirm_yes)) { _, _ ->
                SessionManager.logout()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.setting_logout_confirm_no), null)
            .show()
    }
}
