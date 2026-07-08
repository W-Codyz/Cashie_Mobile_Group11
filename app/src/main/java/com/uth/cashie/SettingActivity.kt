package com.uth.cashie

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.databinding.ActivitySettingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.uth.cashie.database.SessionManager

class SettingActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingBinding
    private val db by lazy { CashieDatabase.getInstance(this) }
    private val userId by lazy { SessionManager.getCurrentUserId() }

    // Màu đang chọn (chưa lưu)
    private var selectedColor: String = ThemeManager.getThemeColor()
    // Icon đang chọn
    private var selectedIconIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyCurrentTheme()
        setupToolbar()
        setupBottomNav(binding.bubbleNav, R.id.nav_settings)
        loadSettings()
        setupLanguageMenu()
        setupLogout()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        onNewIntentBottomNav(intent, binding.bubbleNav, R.id.nav_settings)
    }

    // ── Apply màu chủ đề lên toolbar ─────────────────────────────────────────
    private fun applyCurrentTheme() {
        val colorInt = ThemeManager.getThemeColorInt()
        // Toolbar trắng, chữ/icon màu theme
        binding.appBarLayout.setBackgroundColor(android.graphics.Color.WHITE)
        window.statusBarColor = android.graphics.Color.WHITE
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true
        binding.tvToolbarTitle.setTextColor(colorInt)
        binding.bubbleNav.updateThemeColor(colorInt)

        // "VI"/"EN" label màu theme
        binding.tvCurrentLanguage.setTextColor(colorInt)

        // Language icon circle background
        val containerBg = ThemeManager.getSolidContainerColor()
        val iconCircleBg = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(containerBg)
        }
        binding.languageIconCircle.background = iconCircleBg
    }

    // ── Toolbar hamburger button ──────────────────────────────────────────────
    private fun setupToolbar() {
        binding.btnMenu.setOnClickListener { showNavMenu(NavScreen.SETTINGS) }
    }

    // ── Load settings từ DB, render color picker & icon picker ───────────────
    private fun loadSettings() {
        lifecycleScope.launch {
            val settings = withContext(Dispatchers.IO) {
                db.appSettingsDao().getByUserId(userId)
            }
            selectedColor     = settings?.themeColor ?: ThemeManager.getThemeColor()
            selectedIconIndex = settings?.appIconId?.minus(1)?.coerceIn(0, 9) ?: 0

            buildColorPicker()
            buildIconPicker()
            updateLanguageLabel(settings?.language ?: ThemeManager.getLanguage())
        }
    }

    // ── Color Picker ──────────────────────────────────────────────────────────
    private fun buildColorPicker() {
        val row     = binding.colorPickerRow
        val dp      = resources.displayMetrics.density
        val size    = (44 * dp).toInt()
        val margin  = (8 * dp).toInt()
        val padding = (3 * dp).toInt()

        row.removeAllViews()

        ThemeManager.THEME_COLORS.forEach { hex ->
            // Container (FrameLayout) để thêm vòng viền chọn
            val container = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, margin, margin, margin)
                }
            }

            // Ô màu bên trong
            val circle = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    (36 * dp).toInt(), (36 * dp).toInt(), Gravity.CENTER
                )
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(hex))
                    if (hex == "#F5F5F5") setStroke((1 * dp).toInt(), Color.LTGRAY)
                }
            }
            container.addView(circle)

            // Vòng viền xanh khi được chọn
            val ring = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.TRANSPARENT)
                    setStroke((3 * dp).toInt(), Color.parseColor(selectedColor))
                }
                visibility = if (hex == selectedColor) View.VISIBLE else View.INVISIBLE
            }
            container.addView(ring)

            container.setOnClickListener {
                selectedColor = hex
                // Refresh ring visibility cho tất cả
                refreshColorSelection(row, hex)
                // Lưu ngay vào DB + ThemeManager
                saveThemeColor(hex)
            }

            row.addView(container)
        }
    }

    private fun refreshColorSelection(row: LinearLayout, selectedHex: String) {
        val dp = resources.displayMetrics.density
        ThemeManager.THEME_COLORS.forEachIndexed { i, hex ->
            val container = row.getChildAt(i) as? FrameLayout ?: return@forEachIndexed
            val ring = container.getChildAt(1) as? View ?: return@forEachIndexed
            ring.visibility = if (hex == selectedHex) View.VISIBLE else View.INVISIBLE
            // Cập nhật màu của ring theo màu đang chọn
            (ring.background as? GradientDrawable)?.setStroke(
                (3 * dp).toInt(), Color.parseColor(selectedHex)
            )
        }
    }

    private fun saveThemeColor(hex: String) {
        ThemeManager.setThemeColor(hex)
        // Apply ngay lên toolbar + status bar + tất cả elements
        applyCurrentTheme()

        lifecycleScope.launch(Dispatchers.IO) {
            db.appSettingsDao().updateThemeColor(userId, hex)
        }
        Toast.makeText(this, getString(R.string.toast_theme_applied), Toast.LENGTH_SHORT).show()
    }

    // ── Icon Picker ───────────────────────────────────────────────────────────
    /** Tên mipmap resource cho từng icon (index 0 = mặc định, 1-9 = icon 1-9) */
    private val iconMipmapNames = listOf(
        "ic_launcher_",   // 0 mặc định
        "ic_launcher_1",
        "ic_launcher_2",
        "ic_launcher_3",
        "ic_launcher_4",
        "ic_launcher_5",
        "ic_launcher_6",
        "ic_launcher_7",
        "ic_launcher_8",
        "ic_launcher_9",
    )

    private fun buildIconPicker() {
        val row    = binding.iconPickerRow
        val dp     = resources.displayMetrics.density
        val size   = (64 * dp).toInt()
        val margin = (8 * dp).toInt()

        row.removeAllViews()

        // Dùng selectedIconIndex đã load từ DB (trong loadSettings)
        // KHÔNG gọi IconManager.getCurrentIconIndex() ở đây để tránh
        // trigger PackageManager khi app chưa sẵn sàng

        iconMipmapNames.forEachIndexed { index, resName ->
            val container = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(size + (8 * dp).toInt(), size + (8 * dp).toInt()).apply {
                    setMargins(margin, margin, margin, margin)
                }
            }

            // ImageView hiển thị icon
            val resId = resources.getIdentifier(resName, "mipmap", packageName)
            val iconView = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
                if (resId != 0) setImageResource(resId)
                else setImageResource(R.mipmap.ic_launcher)
                clipToOutline = true
            }
            container.addView(iconView)

            // Viền chọn
            val ring = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    size + (8 * dp).toInt(), size + (8 * dp).toInt(), Gravity.CENTER
                )
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(Color.TRANSPARENT)
                    setStroke((3 * dp).toInt(), ThemeManager.getThemeColorInt())
                    cornerRadius = 16f * dp
                }
                visibility = if (index == selectedIconIndex) View.VISIBLE else View.INVISIBLE
            }
            container.addView(ring)

            container.setOnClickListener {
                selectedIconIndex = index
                refreshIconSelection(row, index)
                saveAppIcon(index)
            }

            row.addView(container)
        }
    }

    private fun refreshIconSelection(row: LinearLayout, selectedIndex: Int) {
        val dp = resources.displayMetrics.density
        for (i in 0 until row.childCount) {
            val container = row.getChildAt(i) as? FrameLayout ?: continue
            val ring = container.getChildAt(1) as? View ?: continue
            ring.visibility = if (i == selectedIndex) View.VISIBLE else View.INVISIBLE
            (ring.background as? GradientDrawable)?.setStroke(
                (3 * dp).toInt(), ThemeManager.getThemeColorInt()
            )
        }
    }

    private fun saveAppIcon(iconIndex: Int) {
        // Chỉ đổi icon nếu PackageManager đã sẵn sàng
        if (!IconManager.isAvailable(this)) {
            Toast.makeText(this, getString(R.string.toast_icon_applied), Toast.LENGTH_SHORT).show()
            return
        }
        IconManager.setIcon(this, iconIndex)
        val iconId = iconIndex + 1  // DB lưu 1-based
        lifecycleScope.launch(Dispatchers.IO) {
            db.appSettingsDao().updateAppIcon(userId, iconId)
        }
        Toast.makeText(this, getString(R.string.toast_icon_applied), Toast.LENGTH_SHORT).show()
    }

    // ── Ngôn ngữ ──────────────────────────────────────────────────────────────
    private fun setupLanguageMenu() {
        binding.menuLanguage.setOnClickListener {
            val options = arrayOf(
                getString(R.string.setting_language_vi),
                getString(R.string.setting_language_en)
            )
            val currentLang = ThemeManager.getLanguage()
            val checkedItem = if (currentLang == "en") 1 else 0

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.setting_language))
                .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                    val lang = if (which == 1) "en" else "vi"
                    dialog.dismiss()
                    ThemeManager.setLanguage(lang)
                    updateLanguageLabel(lang)

                    lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                db.appSettingsDao().updateLanguage(userId, lang)
                            }
                            startActivity(
                                Intent(this@SettingActivity, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                        } catch (e: Exception) {
                            ThemeManager.setLanguage(currentLang)
                            updateLanguageLabel(currentLang)
                            Toast.makeText(
                                this@SettingActivity,
                                getString(R.string.toast_error_save_settings),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .show()
        }
    }

    private fun updateLanguageLabel(lang: String) {
        binding.tvCurrentLanguage.text = if (lang == "en") "EN" else "VI"
    }

    // ── Đăng xuất ─────────────────────────────────────────────────────────────
    private fun setupLogout() {
        binding.menuLogout.setOnClickListener {
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
}
