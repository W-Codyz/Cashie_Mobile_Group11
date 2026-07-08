package com.uth.cashie

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.google.android.material.appbar.AppBarLayout
import java.util.Locale

/**
 * Quản lý màu chủ đề và ngôn ngữ của app.
 *
 * - Màu được lưu vào SharedPreferences (key: PREF_THEME_COLOR) dưới dạng hex string (#RRGGBB).
 * - Ngôn ngữ lưu dưới dạng locale code: "vi" hoặc "en".
 * - apply*() methods tô màu trực tiếp lên View bằng code, không cần restart Activity.
 */
object ThemeManager {

    private const val PREF_NAME        = "cashie_theme"
    private const val KEY_COLOR        = "theme_color"
    private const val KEY_LANGUAGE     = "language"
    const val DEFAULT_COLOR            = "#22CC00"   // green_primary mặc định
    const val DEFAULT_LANGUAGE         = "vi"

    /** Danh sách 11 màu cho color picker (9 màu + đen + trắng) */
    val THEME_COLORS = listOf(
        "#22CC00",  // Xanh lá (mặc định)
        "#2196F3",  // Xanh dương
        "#9C27B0",  // Tím
        "#FF9800",  // Cam
        "#F44336",  // Đỏ
        "#E91E63",  // Hồng
        "#795548",  // Nâu
        "#3F51B5",  // Indigo
        "#009688",  // Teal
        "#1A1A1A",  // Đen
        "#F5F5F5",  // Trắng
    )

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        if (!::prefs.isInitialized) {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
        return prefs
    }

    /**
     * Bọc context với locale đã lưu. Gọi trong attachBaseContext() của mọi Activity/Application
     * để locale được áp dụng đúng ngay từ đầu, không cần restart.
     */
    fun wrapContext(context: Context): Context {
        val lang = getPrefs(context).getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    // ── Màu ──────────────────────────────────────────────────────────────────

    fun getThemeColor(): String =
        prefs.getString(KEY_COLOR, DEFAULT_COLOR) ?: DEFAULT_COLOR

    fun setThemeColor(hexColor: String) {
        prefs.edit().putString(KEY_COLOR, hexColor).apply()
    }

    /** Trả về Color int từ hex đã lưu */
    fun getThemeColorInt(): Int = Color.parseColor(getThemeColor())

    /**
     * Tính màu container (nhạt ~15% opacity) để dùng làm nền nhạt.
     * Ví dụ: green_primary → green_container (#DCF7D8)
     */
    fun getContainerColor(): Int {
        val base = getThemeColorInt()
        return Color.argb(38, Color.red(base), Color.green(base), Color.blue(base))
    }

    /**
     * Màu container đặc (không transparent) dùng cho nền icon circle.
     * Pha màu theme với trắng ở tỉ lệ 15%.
     */
    fun getSolidContainerColor(): Int {
        val base = getThemeColorInt()
        val alpha = 0.15f
        return Color.rgb(
            (Color.red(base) * alpha + 255 * (1 - alpha)).toInt().coerceIn(0, 255),
            (Color.green(base) * alpha + 255 * (1 - alpha)).toInt().coerceIn(0, 255),
            (Color.blue(base) * alpha + 255 * (1 - alpha)).toInt().coerceIn(0, 255)
        )
    }

    /**
     * Màu chữ phù hợp khi đặt trên nền màu theme.
     * Màu tối → trả về trắng, màu sáng → trả về đen.
     */
    fun getOnThemeColor(): Int =
        if (isColorLight(getThemeColorInt())) Color.parseColor("#1A1A1A") else Color.WHITE

    /**
     * Tính màu tối hơn chủ đề (~20%) để làm gradient/dark variant.
     */
    fun getDarkColor(): Int {
        val base = getThemeColorInt()
        val factor = 0.8f
        return Color.rgb(
            (Color.red(base)   * factor).toInt().coerceIn(0, 255),
            (Color.green(base) * factor).toInt().coerceIn(0, 255),
            (Color.blue(base)  * factor).toInt().coerceIn(0, 255)
        )
    }

    // ── Ngôn ngữ ─────────────────────────────────────────────────────────────

    fun getLanguage(): String =
        prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    // ── Apply lên View ────────────────────────────────────────────────────────

    /** Tô màu chủ đề lên AppBarLayout (toolbar background) */
    fun applyToAppBar(appBar: AppBarLayout) {
        appBar.setBackgroundColor(getThemeColorInt())
    }

    /**
     * Tô màu toolbar + status bar + tự động đổi màu chữ/icon toolbar cho dễ đọc.
     * @param toolbarTitle TextView tiêu đề trong toolbar (optional)
     * @param toolbarBack  ImageButton nút back (optional)
     */
    fun applyThemeToWindow(
        activity: android.app.Activity,
        appBar: AppBarLayout,
        toolbarTitle: android.widget.TextView? = null,
        toolbarBack: android.widget.ImageButton? = null
    ) {
        val color = getThemeColorInt()
        appBar.setBackgroundColor(color)
        activity.window.statusBarColor = color

        val isLight = isColorLight(color)
        val insetsController = androidx.core.view.WindowCompat
            .getInsetsController(activity.window, activity.window.decorView)
        insetsController.isAppearanceLightStatusBars = isLight

        // Màu chữ/icon trên toolbar
        val onColor = if (isLight) Color.parseColor("#1A1A1A") else Color.WHITE
        toolbarTitle?.setTextColor(onColor)
        toolbarBack?.setColorFilter(onColor)
    }

    /** Kiểm tra màu có "sáng" không (luminance > 0.5) */
    private fun isColorLight(color: Int): Boolean {
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0
        val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
        return luminance > 0.5
    }

    /**
     * Apply theme color lên TextInputLayout:
     * - boxStroke (border) theo focused/default state
     * - hint text color
     * Gọi hàm này thay vì set từng thuộc tính riêng.
     */
    fun applyToTextInput(til: com.google.android.material.textfield.TextInputLayout) {
        val colorInt = getThemeColorInt()
        val colorList = android.content.res.ColorStateList.valueOf(colorInt)
        val strokeStateList = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(-android.R.attr.state_focused)
            ),
            intArrayOf(colorInt, android.graphics.Color.parseColor("#BDBDBD"))
        )
        til.setBoxStrokeColorStateList(strokeStateList)
        til.hintTextColor = colorList
        til.defaultHintTextColor = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_focused),
                intArrayOf()
            ),
            intArrayOf(colorInt, android.graphics.Color.parseColor("#888888"))
        )
    }

    /**
     * Tô màu lên Button — set cả background lẫn backgroundTintList để
     * override Material button tint từ theme.
     */
    fun applyToButton(button: View) {
        val colorInt = getThemeColorInt()
        val colorList = android.content.res.ColorStateList.valueOf(colorInt)
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(colorInt)
            cornerRadius = 12f * button.resources.displayMetrics.density
        }
        button.background = bg
        // Override Material backgroundTint nếu là MaterialButton
        if (button is com.google.android.material.button.MaterialButton) {
            button.backgroundTintList = colorList
        } else if (button is android.widget.Button) {
            button.backgroundTintList = colorList
        }
    }

    /**
     * Tô màu lên gradient card (bg_balance_card.xml — dùng cho balance card, profile header).
     * Tạo gradient từ màu tối → màu chủ đề → màu sáng hơn.
     */
    fun applyToGradientCard(view: View) {
        val dark   = getDarkColor()
        val main   = getThemeColorInt()
        val light  = getLightColor()
        val grad = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(dark, main, light)
        ).apply {
            cornerRadius = 22f * view.resources.displayMetrics.density
        }
        view.background = grad
    }

    private fun getLightColor(): Int {
        val base = getThemeColorInt()
        val factor = 1.15f
        return Color.rgb(
            (Color.red(base)   * factor).toInt().coerceIn(0, 255),
            (Color.green(base) * factor).toInt().coerceIn(0, 255),
            (Color.blue(base)  * factor).toInt().coerceIn(0, 255)
        )
    }
}
