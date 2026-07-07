package com.uth.cashie

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class BubbleBottomNavView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private data class Item(
        val container: LinearLayout,
        val iconBg: LinearLayout,
        val icon: ImageView,
        val label: TextView
    )

    private val navItems = mutableListOf<Item>()
    private var selectedIndex = 0
    private var themeColor = Color.parseColor("#22CC00")
    private val dp = context.resources.displayMetrics.density
    private val cornerRadius = 40f * dp

    companion object {
        private const val WEIGHT_SELECTED = 3f
        private const val WEIGHT_IDLE = 1f
        private const val ANIM_DURATION = 280L
        // Light theme colors
        private val INACTIVE_CIRCLE = Color.parseColor("#F0F0F0")
        private val INACTIVE_ICON   = Color.parseColor("#9E9E9E")
    }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val padH = (6 * dp).toInt()
        val padV = (6 * dp).toInt()
        setPadding(padH, padV, padH, padV)

        // White pill with subtle border shadow
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE)
            this.cornerRadius = this@BubbleBottomNavView.cornerRadius
            setStroke((1 * dp).toInt(), Color.parseColor("#E0E0E0"))
        }
        elevation = 8 * dp
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }
        clipToOutline = true
    }

    fun setup(
        items: List<Pair<Int, String>>,
        selectedIndex: Int,
        themeColor: Int,
        onItemClick: (index: Int) -> Unit
    ) {
        this.themeColor = themeColor
        this.selectedIndex = selectedIndex
        removeAllViews()
        navItems.clear()

        val circleSize = (44 * dp).toInt()
        val iconPad    = (10 * dp).toInt()

        items.forEachIndexed { idx, (iconRes, label) ->
            val isSelected = idx == selectedIndex

            val iconBg = LinearLayout(context).apply {
                layoutParams = LayoutParams(circleSize, circleSize)
                gravity = Gravity.CENTER
                background = if (isSelected) null else circle(INACTIVE_CIRCLE)
                setPadding(iconPad, iconPad, iconPad, iconPad)
            }

            val icon = ImageView(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                setImageResource(iconRes)
                setColorFilter(if (isSelected) Color.WHITE else INACTIVE_ICON)
            }
            iconBg.addView(icon)

            val labelView = TextView(context).apply {
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    marginStart = (8 * dp).toInt()
                    marginEnd   = (2 * dp).toInt()
                }
                text = label
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                maxLines = 1
                visibility = if (isSelected) VISIBLE else GONE
                alpha = if (isSelected) 1f else 0f
            }

            val container = LinearLayout(context).apply {
                layoutParams = LayoutParams(
                    0,
                    LayoutParams.WRAP_CONTENT,
                    if (isSelected) WEIGHT_SELECTED else WEIGHT_IDLE
                )
                gravity = Gravity.CENTER
                orientation = HORIZONTAL
                val cp = (4 * dp).toInt()
                setPadding(cp, cp, cp, cp)
                background = if (isSelected) pill(themeColor) else null
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    if (idx != this@BubbleBottomNavView.selectedIndex) {
                        onItemClick(idx)
                    }
                }
            }
            container.addView(iconBg)
            container.addView(labelView)
            addView(container)

            navItems.add(Item(container, iconBg, icon, labelView))
        }
    }

    /** Instantly move the selected state to [index] with no animation — call before selectItem() to prime the slide. */
    fun snapTo(index: Int) {
        if (index !in navItems.indices || index == selectedIndex) return

        val old = navItems[selectedIndex]
        (old.container.layoutParams as LayoutParams).weight = WEIGHT_IDLE
        old.container.background = null
        old.iconBg.background = circle(INACTIVE_CIRCLE)
        old.icon.setColorFilter(INACTIVE_ICON)
        old.label.visibility = GONE
        old.label.alpha = 0f

        selectedIndex = index
        val item = navItems[index]
        (item.container.layoutParams as LayoutParams).weight = WEIGHT_SELECTED
        item.container.background = pill(themeColor)
        item.iconBg.background = null
        item.icon.setColorFilter(Color.WHITE)
        item.label.visibility = VISIBLE
        item.label.alpha = 1f

        requestLayout()
    }

    fun selectItem(index: Int) {
        if (index == selectedIndex || index !in navItems.indices) return
        val prev = selectedIndex
        selectedIndex = index
        animateDeselect(navItems[prev], (navItems[prev].container.layoutParams as LayoutParams).weight)
        animateSelect(navItems[index], (navItems[index].container.layoutParams as LayoutParams).weight)
    }

    fun updateThemeColor(color: Int) {
        themeColor = color
        if (selectedIndex in navItems.indices) {
            val item = navItems[selectedIndex]
            item.container.background = pill(color)
            item.iconBg.background = null
            item.icon.setColorFilter(Color.WHITE)
            item.label.setTextColor(Color.WHITE)
        }
    }

    private fun animateSelect(item: Item, fromWeight: Float) {
        item.container.background = pill(themeColor)
        item.iconBg.background = null
        item.icon.setColorFilter(Color.WHITE)
        item.label.setTextColor(Color.WHITE)
        item.label.visibility = VISIBLE
        item.label.alpha = 0f
        item.label.animate()
            .alpha(1f)
            .setDuration(ANIM_DURATION)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
        animateWeight(item.container, fromWeight, WEIGHT_SELECTED)
    }

    private fun animateDeselect(item: Item, fromWeight: Float) {
        item.container.background = null
        item.iconBg.background = circle(INACTIVE_CIRCLE)
        item.icon.setColorFilter(INACTIVE_ICON)
        item.label.animate()
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(FastOutSlowInInterpolator())
            .withEndAction { item.label.visibility = GONE }
            .start()
        animateWeight(item.container, fromWeight, WEIGHT_IDLE)
    }

    private fun animateWeight(view: LinearLayout, from: Float, to: Float) {
        ValueAnimator.ofFloat(from, to).apply {
            duration = ANIM_DURATION
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { anim ->
                val lp = view.layoutParams as LayoutParams
                lp.weight = anim.animatedValue as Float
                view.layoutParams = lp
                requestLayout()
            }
            start()
        }
    }

    private fun circle(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    private fun pill(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = this@BubbleBottomNavView.cornerRadius
    }

    private fun onColor(bg: Int): Int {
        val r = Color.red(bg) / 255.0
        val g = Color.green(bg) / 255.0
        val b = Color.blue(bg) / 255.0
        return if (0.299 * r + 0.587 * g + 0.114 * b > 0.5) Color.BLACK else Color.WHITE
    }
}
