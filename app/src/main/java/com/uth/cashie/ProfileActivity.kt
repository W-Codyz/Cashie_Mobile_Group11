package com.uth.cashie

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.databinding.ActivityProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

class ProfileActivity : BaseActivity() {

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
        val containerBg   = ThemeManager.getSolidContainerColor()  // nền nhạt icon circle
        val colorStateList = android.content.res.ColorStateList.valueOf(colorInt)

        // Toolbar trắng, chữ/icon màu theme
        binding.appBarLayout.setBackgroundColor(android.graphics.Color.WHITE)
        window.statusBarColor = android.graphics.Color.WHITE
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true
        binding.tvProfileTitle.setTextColor(colorInt)
        binding.btnEdit.setTextColor(colorInt)
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
        // loadProfile() sẽ xử lý reset tint nếu có ảnh
        binding.ivAvatar.backgroundTintList = android.content.res.ColorStateList.valueOf(colorInt)
        binding.ivAvatar.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
        binding.ivAvatar.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
        binding.ivAvatar.setPadding(18, 18, 18, 18)

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
            binding.iconCircleConverter      to binding.iconConverter,
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

        binding.menuConverter.setOnClickListener {
            showWalletTransferDialog()
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
            val themeColor = ThemeManager.getThemeColorInt()
            val colorList = android.content.res.ColorStateList.valueOf(themeColor)
            val current = if (user.currency == "USD") 1 else 0

            val radioGroup = android.widget.RadioGroup(this@ProfileActivity).apply {
                orientation = android.widget.RadioGroup.VERTICAL
                setPadding(64, 24, 64, 8)
            }
            arrayOf("VND — Việt Nam Đồng", "USD — US Dollar").forEachIndexed { idx, label ->
                android.widget.RadioButton(this@ProfileActivity).apply {
                    text = label
                    id = idx
                    isChecked = idx == current
                    buttonTintList = colorList
                    setPadding(8, 20, 8, 20)
                    radioGroup.addView(this)
                }
            }

            val dialog = AlertDialog.Builder(this@ProfileActivity)
                .setTitle(getString(R.string.profile_menu_currency))
                .setView(radioGroup)
                .show()

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                val currency = if (checkedId == 1) "USD" else "VND"
                lifecycleScope.launch(Dispatchers.IO) {
                    db.userDao().update(user.copy(currency = currency))
                }
                SessionManager.setCurrency(currency)
                binding.tvCurrencyBadge.text = currency
                dialog.dismiss()
                Toast.makeText(this@ProfileActivity, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Dialog chuyển tiền giữa ví ───────────────────────────────────────────
    private fun showWalletTransferDialog() {
        val dialogView   = layoutInflater.inflate(R.layout.dialog_wallet_transfer, null)
        val tvFromEmoji  = dialogView.findViewById<TextView>(R.id.tvFromEmoji)
        val tvFromName   = dialogView.findViewById<TextView>(R.id.tvFromName)
        val tvFromBal    = dialogView.findViewById<TextView>(R.id.tvFromBalance)
        val tvToEmoji    = dialogView.findViewById<TextView>(R.id.tvToEmoji)
        val tvToName     = dialogView.findViewById<TextView>(R.id.tvToName)
        val tvToBal      = dialogView.findViewById<TextView>(R.id.tvToBalance)
        val btnSwap      = dialogView.findViewById<ImageButton>(R.id.btnSwapWallet)
        val tilAmount    = dialogView.findViewById<TextInputLayout>(R.id.tilTransferAmount)
        val etAmount     = dialogView.findViewById<TextInputEditText>(R.id.etTransferAmount)
        val layoutPreview = dialogView.findViewById<LinearLayout>(R.id.layoutTransferPreview)
        val tvFromAfter  = dialogView.findViewById<TextView>(R.id.tvFromAfter)
        val tvToAfter    = dialogView.findViewById<TextView>(R.id.tvToAfter)
        val layoutFrom   = dialogView.findViewById<LinearLayout>(R.id.layoutFromWallet)
        val layoutTo     = dialogView.findViewById<LinearLayout>(R.id.layoutToWallet)

        val themeColor     = ThemeManager.getThemeColorInt()
        val themeColorList = android.content.res.ColorStateList.valueOf(themeColor)
        val strokeColors   = android.content.res.ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
            intArrayOf(themeColor, Color.parseColor("#BDBDBD"))
        )
        tilAmount.setBoxStrokeColorStateList(strokeColors)
        tilAmount.hintTextColor = themeColorList
        btnSwap.imageTintList = themeColorList

        val currency = SessionManager.getCurrency()

        lifecycleScope.launch {
            val cal = Calendar.getInstance()
            val year  = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            cal.set(year, month - 1, 1, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
            val startMs = cal.timeInMillis
            cal.set(year, month - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endMs = cal.timeInMillis

            val wallets = withContext(Dispatchers.IO) {
                db.walletDao().getAllByUserOnce(userId)
            }
            val cashWallet = wallets.firstOrNull { it.type == "cash" }
            val bankWallet = wallets.firstOrNull { it.type == "bank" }

            val monthEntity = withContext(Dispatchers.IO) {
                db.monthlyBalanceDao().getByMonthYear(userId, month, year)
            }
            val cashOpening = monthEntity?.cashBalance    ?: 0L
            val bankOpening = monthEntity?.accountBalance ?: 0L

            val cashNet = cashWallet?.let {
                withContext(Dispatchers.IO) {
                    db.transactionDao().getNetByWallet(userId, it.id, startMs, endMs).toLong()
                }
            } ?: 0L
            val bankNet = bankWallet?.let {
                withContext(Dispatchers.IO) {
                    db.transactionDao().getNetByWallet(userId, it.id, startMs, endMs).toLong()
                }
            } ?: 0L

            val cashBalance = cashOpening + cashNet
            val bankBalance = bankOpening + bankNet

            // fromIsCash = true: Tiền mặt → NH; false: NH → Tiền mặt
            var fromIsCash = true

            fun emojiBg(color: Int, alpha: Int = 30) = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)))
            }

            fun roundedBg(color: Int, alpha: Int = 18, radius: Float = 12f) =
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = radius * resources.displayMetrics.density
                    setColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)))
                }

            val colorGreen = Color.parseColor("#22CC00")
            val colorRed   = Color.parseColor("#FF4444")

            fun signedBalance(bal: Long): String =
                if (bal < 0) "-${formatVND(bal, currency)}" else formatVND(bal, currency)

            fun bindWallets() {
                val fromEmoji = if (fromIsCash) cashWallet?.iconEmoji ?: "💵" else bankWallet?.iconEmoji ?: "🏦"
                val fromName  = if (fromIsCash) cashWallet?.name ?: "Tiền mặt" else bankWallet?.name ?: "Tài khoản NH"
                val fromBal   = if (fromIsCash) cashBalance else bankBalance
                val toEmoji   = if (fromIsCash) bankWallet?.iconEmoji ?: "🏦" else cashWallet?.iconEmoji ?: "💵"
                val toName    = if (fromIsCash) bankWallet?.name ?: "Tài khoản NH" else cashWallet?.name ?: "Tiền mặt"
                val toBal     = if (fromIsCash) bankBalance else cashBalance

                tvFromEmoji.text = fromEmoji
                tvFromName.text  = fromName
                tvFromBal.text   = "Số dư: ${signedBalance(fromBal)}"
                tvFromBal.setTextColor(if (fromBal < 0) colorRed else colorGreen)
                tvToEmoji.text   = toEmoji
                tvToName.text    = toName
                tvToBal.text     = "Số dư: ${signedBalance(toBal)}"
                tvToBal.setTextColor(if (toBal < 0) colorRed else colorGreen)

                tvFromEmoji.background = emojiBg(themeColor, 35)
                tvToEmoji.background   = emojiBg(Color.GRAY, 25)
                layoutFrom.background  = roundedBg(themeColor, 18)
                layoutTo.background    = roundedBg(Color.GRAY, 8)

                // Cập nhật preview nếu đang nhập
                val amount = etAmount.text.toString().toLongOrNull() ?: 0L
                if (amount > 0) {
                    val newFrom = fromBal - amount
                    val newTo   = toBal + amount
                    tvFromAfter.text = signedBalance(newFrom)
                    tvFromAfter.setTextColor(if (newFrom < 0) colorRed else colorGreen)
                    tvToAfter.text = signedBalance(newTo)
                    tvToAfter.setTextColor(if (newTo < 0) colorRed else colorGreen)
                    layoutPreview.visibility = View.VISIBLE
                } else {
                    layoutPreview.visibility = View.GONE
                }
            }

            bindWallets()

            btnSwap.setOnClickListener {
                fromIsCash = !fromIsCash
                bindWallets()
            }

            etAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) { bindWallets() }
            })

            AlertDialog.Builder(this@ProfileActivity)
                .setTitle(getString(R.string.profile_menu_converter))
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val amount = etAmount.text.toString().toLongOrNull() ?: 0L
                    if (amount <= 0L) {
                        Toast.makeText(this@ProfileActivity, getString(R.string.transfer_error_amount), Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val currentFromBal = if (fromIsCash) cashBalance else bankBalance
                    if (amount > currentFromBal) {
                        Toast.makeText(this@ProfileActivity, getString(R.string.transfer_error_insufficient), Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    lifecycleScope.launch {
                        val newCash = if (fromIsCash) cashOpening - amount else cashOpening + amount
                        val newBank = if (fromIsCash) bankOpening + amount else bankOpening - amount
                        withContext(Dispatchers.IO) {
                            db.monthlyBalanceDao().upsert(
                                com.uth.cashie.database.entity.MonthlyBalanceEntity(
                                    userId         = userId,
                                    month          = month,
                                    year           = year,
                                    cashBalance    = newCash,
                                    accountBalance = newBank
                                )
                            )
                        }
                        Toast.makeText(this@ProfileActivity, getString(R.string.transfer_success), Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
                .also { d ->
                    d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColor)
                    d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColor)
                }
        }
    }

    // ── Dialog đăng xuất ─────────────────────────────────────────────────────
    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(this)
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
        val themeColor = ThemeManager.getThemeColorInt()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(themeColor)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(themeColor)
    }
}
