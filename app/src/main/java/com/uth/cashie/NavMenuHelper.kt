package com.uth.cashie

import android.app.ActivityOptions
import android.app.Dialog
import androidx.activity.OnBackPressedCallback
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
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

internal const val EXTRA_PREV_NAV_INDEX = "bubble_nav_prev_index"

private val NAV_IDS = listOf(
    R.id.nav_home, R.id.nav_categories, R.id.nav_stats,
    R.id.nav_profile, R.id.nav_settings
)

/**
 * Call from onNewIntent() so the nav bar re-plays its slide animation on each tab revisit.
 * REORDER_TO_FRONT skips onCreate, so this is the only place to re-trigger the animation.
 */
fun AppCompatActivity.onNewIntentBottomNav(
    newIntent: android.content.Intent,
    bubbleNav: BubbleBottomNavView,
    selectedId: Int
) {
    setIntent(newIntent)
    val selectedIndex = NAV_IDS.indexOf(selectedId).coerceAtLeast(0)
    val prevIndex = newIntent.getIntExtra(EXTRA_PREV_NAV_INDEX, selectedIndex)
    if (prevIndex != selectedIndex) {
        bubbleNav.snapTo(prevIndex)
        bubbleNav.post { bubbleNav.selectItem(selectedIndex) }
    }

    // Fade in only the content views — skip the nav bar so it looks persistent across tabs.
    // bubbleNav.parent is the root layout; iterate its direct children and animate all except bubbleNav.
    val root = bubbleNav.parent as? android.view.ViewGroup ?: return
    for (i in 0 until root.childCount) {
        val child = root.getChildAt(i)
        if (child === bubbleNav) continue
        child.alpha = 0f
        child.animate()
            .alpha(1f)
            .setDuration(180)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }
}

/**
 * Wires up a BubbleBottomNavView so that:
 * - The nav bar slides from the PREVIOUS tab (stored in intent extra) to the current tab on entry.
 * - Tapping another tab triggers instant activity swap (no screen slide) — the nav bar itself
 *   provides the slide animation on the destination activity.
 *
 * @param bubbleNav   the BubbleBottomNavView to configure
 * @param selectedId  the menu item ID that represents the current screen
 */
fun AppCompatActivity.setupBottomNav(bubbleNav: BubbleBottomNavView, selectedId: Int) {
    val navIds = NAV_IDS
    val navIcons = listOf(
        R.drawable.ic_home,
        R.drawable.ic_category,
        R.drawable.ic_bar_chart,
        R.drawable.ic_person,
        R.drawable.ic_settings
    )
    val navLabels = listOf(
        getString(R.string.nav_home),
        getString(R.string.nav_categories),
        getString(R.string.nav_stats),
        getString(R.string.nav_profile),
        getString(R.string.nav_settings)
    )

    val selectedIndex = navIds.indexOf(selectedId).coerceAtLeast(0)
    val items = navIcons.zip(navLabels)

    // Read the index that the user came FROM (set by the outgoing activity when navigating here).
    // On first launch (no extra), default to current so no animation plays.
    val prevIndex = intent.getIntExtra(EXTRA_PREV_NAV_INDEX, selectedIndex)

    // Set up the nav bar in the "previous" selected state, then immediately animate into
    // the real selected state — this is what produces the visible slide on arrival.
    var lastTapMs = 0L

    bubbleNav.setup(items, prevIndex, ThemeManager.getThemeColorInt()) { clickedIndex ->
        val now = System.currentTimeMillis()
        if (now - lastTapMs < 400L) return@setup
        lastTapMs = now

        val targetId = navIds[clickedIndex]
        // All tabs: REORDER_TO_FRONT brings the existing instance forward WITHOUT
        //   finishing intermediate activities — no default exit animations, no slide.
        val targetIntent = when (targetId) {
            R.id.nav_home -> Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            R.id.nav_categories -> Intent(this, CategoryMainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            R.id.nav_stats      -> Intent(this, StatsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            R.id.nav_profile    -> Intent(this, ProfileActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            R.id.nav_settings   -> Intent(this, SettingActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            else -> return@setup
        }
        // Tell the destination which tab we came from so it can animate its nav bar.
        targetIntent.putExtra(EXTRA_PREV_NAV_INDEX, selectedIndex)

        // No window animation — content fade is handled per-view in onNewIntentBottomNav
        // so the bottom nav is excluded and stays visually persistent.
        val opts = ActivityOptions.makeCustomAnimation(this, 0, 0)
        startActivity(targetIntent, opts.toBundle())
    }

    // Trigger the nav slide animation: animate from prevIndex → selectedIndex.
    if (prevIndex != selectedIndex) {
        bubbleNav.post { bubbleNav.selectItem(selectedIndex) }
    }

    // Back press handling for all tabs:
    // - Home: minimize the app (moveTaskToBack) so MainActivity is NEVER destroyed.
    //   This ensures REORDER_TO_FRONT always finds existing instances of the other tabs.
    // - Non-home: go to Home with no slide. Fragment manager callbacks registered after
    //   this one take priority (LIFO), so fragment back stacks are still handled correctly.
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (selectedId == R.id.nav_home) {
                moveTaskToBack(true)
            } else {
                startActivity(
                    Intent(this@setupBottomNav, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        .putExtra(EXTRA_PREV_NAV_INDEX, selectedIndex)
                )
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            }
        }
    })
}
