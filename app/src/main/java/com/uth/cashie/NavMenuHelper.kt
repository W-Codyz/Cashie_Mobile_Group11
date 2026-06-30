package com.uth.cashie

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uth.cashie.databinding.DialogNavigationBinding

enum class NavScreen { TRANSACTIONS, CATEGORIES, STATS, PROFILE, SETTINGS }

/**
 * Shows the slide-from-left navigation panel with the correct item highlighted.
 *
 * @param activeScreen  which screen is currently active (sets row highlight + indicator + bold label)
 * @param isRootScreen  true for MainActivity — keeps it in the back stack instead of finishing it
 */
fun AppCompatActivity.showNavMenu(
    activeScreen: NavScreen,
    isRootScreen: Boolean = false
) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)

    val navBinding = DialogNavigationBinding.inflate(layoutInflater)
    dialog.setContentView(navBinding.root)

    dialog.window?.apply {
        val screenWidth = resources.displayMetrics.widthPixels
        setLayout((screenWidth * 0.72).toInt(), WindowManager.LayoutParams.MATCH_PARENT)
        setGravity(Gravity.START)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        attributes.dimAmount = 0.45f
        setWindowAnimations(R.style.NavDialogAnimation)
    }

    // Header gradient follows current theme
    ThemeManager.applyToGradientCard(navBinding.navHeader)

    val themeColor = ThemeManager.getThemeColorInt()
    val dp = resources.displayMetrics.density

    // Active indicator — vertical pill in theme color
    val indicatorDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(themeColor)
        cornerRadius = 4f * dp
    }

    // Active row background — soft semi-transparent tint (~20 % opacity)
    val highlightColor = Color.argb(50, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor))
    val highlightDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(highlightColor)
        cornerRadius = 12f * dp
    }

    // All indicators hidden, all labels normal (views are fresh per dialog inflation)
    listOf(
        navBinding.navIndicatorTransactions,
        navBinding.navIndicatorCategories,
        navBinding.navIndicatorStats,
        navBinding.navIndicatorProfile,
        navBinding.navIndicatorSettings
    ).forEach { it.visibility = View.GONE }

    listOf(
        navBinding.navLabelTransactions,
        navBinding.navLabelCategories,
        navBinding.navLabelStats,
        navBinding.navLabelProfile,
        navBinding.navLabelSettings
    ).forEach {
        it.setTypeface(null, Typeface.NORMAL)
        it.setTextColor(resources.getColor(R.color.text_primary, theme))
    }

    // Highlight the active screen
    val (activeIndicator, activeLabel, activeRow) = when (activeScreen) {
        NavScreen.TRANSACTIONS -> Triple(
            navBinding.navIndicatorTransactions,
            navBinding.navLabelTransactions,
            navBinding.navItemTransactions as View
        )
        NavScreen.CATEGORIES -> Triple(
            navBinding.navIndicatorCategories,
            navBinding.navLabelCategories,
            navBinding.navItemCategories as View
        )
        NavScreen.STATS -> Triple(
            navBinding.navIndicatorStats,
            navBinding.navLabelStats,
            navBinding.navItemStats as View
        )
        NavScreen.PROFILE -> Triple(
            navBinding.navIndicatorProfile,
            navBinding.navLabelProfile,
            navBinding.navItemProfile as View
        )
        NavScreen.SETTINGS -> Triple(
            navBinding.navIndicatorSettings,
            navBinding.navLabelSettings,
            navBinding.navItemSettings as View
        )
    }

    activeRow.background = highlightDrawable
    activeIndicator.background = indicatorDrawable
    activeIndicator.visibility = View.VISIBLE
    activeLabel.setTypeface(null, Typeface.BOLD)
    activeLabel.setTextColor(themeColor)

    // Navigate to a peer screen with scale+fade transition
    fun navigate(intent: Intent) {
        dialog.dismiss()
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.nav_enter, R.anim.nav_exit)
        if (!isRootScreen) finish()
    }

    navBinding.navItemTransactions.setOnClickListener {
        if (activeScreen == NavScreen.TRANSACTIONS) { dialog.dismiss(); return@setOnClickListener }
        navigate(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
    }
    navBinding.navItemCategories.setOnClickListener {
        if (activeScreen == NavScreen.CATEGORIES) { dialog.dismiss(); return@setOnClickListener }
        navigate(Intent(this, CategoryMainActivity::class.java))
    }
    navBinding.navItemStats.setOnClickListener {
        if (activeScreen == NavScreen.STATS) { dialog.dismiss(); return@setOnClickListener }
        navigate(Intent(this, StatsActivity::class.java))
    }
    navBinding.navItemProfile.setOnClickListener {
        if (activeScreen == NavScreen.PROFILE) { dialog.dismiss(); return@setOnClickListener }
        navigate(Intent(this, ProfileActivity::class.java))
    }
    navBinding.navItemSettings.setOnClickListener {
        if (activeScreen == NavScreen.SETTINGS) { dialog.dismiss(); return@setOnClickListener }
        navigate(Intent(this, SettingActivity::class.java))
    }
    // About: no overridePendingTransition → default Android slide-in (signals a new/separate screen)
    navBinding.navItemAbout.setOnClickListener {
        dialog.dismiss()
        startActivity(Intent(this, AboutActivity::class.java))
    }

    dialog.show()
}

/**
 * Wires up a floating BottomNavigationView with correct selection state and
 * single-instance navigation (no stacking of peer activities).
 *
 * @param bottomNav   the BottomNavigationView to configure
 * @param selectedId  the menu item ID that represents the current screen
 */
fun AppCompatActivity.setupBottomNav(bottomNav: BottomNavigationView, selectedId: Int) {
    val themeColor = ThemeManager.getThemeColorInt()

    bottomNav.selectedItemId = selectedId
    bottomNav.itemActiveIndicatorColor = ColorStateList.valueOf(ThemeManager.getContainerColor())
    bottomNav.itemIconTintList = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
        intArrayOf(themeColor, Color.parseColor("#888888"))
    )
    bottomNav.itemTextColor = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
        intArrayOf(themeColor, Color.parseColor("#888888"))
    )

    // Time-based debounce: ignore taps within 400 ms of the last navigation
    var lastTapMs = 0L

    bottomNav.setOnItemSelectedListener { item ->
        if (item.itemId == selectedId) return@setOnItemSelectedListener true

        val now = System.currentTimeMillis()
        if (now - lastTapMs < 400L) return@setOnItemSelectedListener false

        val intent = when (item.itemId) {
            R.id.nav_home -> Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            R.id.nav_categories -> Intent(this, CategoryMainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            R.id.nav_stats      -> Intent(this, StatsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            R.id.nav_profile    -> Intent(this, ProfileActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            R.id.nav_settings   -> Intent(this, SettingActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            else -> return@setOnItemSelectedListener false
        }

        lastTapMs = now
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.tab_enter, R.anim.tab_exit)
        if (selectedId != R.id.nav_home) finish()
        true
    }
}
