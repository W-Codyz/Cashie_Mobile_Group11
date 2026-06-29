package com.uth.cashie.stats.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Custom View vẽ biểu đồ Donut Chart bằng Canvas.
 *
 * Cách dùng:
 *   donutChart.setData(segments, centerTitle, centerSubtitle)
 *   donutChart.animateIn()
 */
class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // ─────────────────────────────────────────────────────────────────────────
    // Data model
    // ─────────────────────────────────────────────────────────────────────────

    data class Segment(
        val label: String,      // "Quý 1"
        val value: Float,       // giá trị thực (dùng để tính %)
        val color: Int          // màu của phần
    )

    // ─────────────────────────────────────────────────────────────────────────
    // State
    // ─────────────────────────────────────────────────────────────────────────

    private var segments: List<Segment> = emptyList()
    private var centerTitle: String = ""
    private var centerSubtitle: String = ""

    /** 0f → 360f, dùng cho animation */
    private var sweepProgress: Float = 360f

    // ─────────────────────────────────────────────────────────────────────────
    // Paint objects
    // ─────────────────────────────────────────────────────────────────────────

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val centerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        color = Color.parseColor("#2196F3")
    }

    private val centerSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#888888")
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
        isFakeBoldText = true
    }

    private val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private val oval = RectF()

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    fun setData(
        segments: List<Segment>,
        centerTitle: String = "",
        centerSubtitle: String = ""
    ) {
        this.segments = segments
        this.centerTitle = centerTitle
        this.centerSubtitle = centerSubtitle
        invalidate()
    }

    /** Chạy hiệu ứng vẽ dần từ 0 → 360° */
    fun animateIn(durationMs: Long = 900L) {
        sweepProgress = 0f
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = durationMs
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                sweepProgress = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Drawing
    // ─────────────────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (segments.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val size = min(w, h)

        val strokeWidth = size * 0.18f          // độ dày vành donut
        val radius = (size - strokeWidth) / 2f  // bán kính ngoài
        val cx = w / 2f
        val cy = h / 2f

        arcPaint.strokeWidth = strokeWidth
        oval.set(cx - radius, cy - radius, cx + radius, cy + radius)

        val total = segments.sumOf { it.value.toDouble() }.toFloat()
        if (total == 0f) return

        // Vẽ từng cung
        var startAngle = -90f   // bắt đầu từ đỉnh (12 giờ)
        val sweepPerDegree = sweepProgress / 360f   // tỉ lệ animation

        for (seg in segments) {
            val fraction = seg.value / total
            val fullSweep = fraction * 360f
            val animSweep = fullSweep * sweepPerDegree

            // Vẽ cung
            arcPaint.color = seg.color
            canvas.drawArc(oval, startAngle, animSweep - 1f, false, arcPaint)  // -1f = khe hở nhỏ

            // Label + % ở giữa cung (chỉ hiện khi animation đủ lớn)
            if (sweepProgress >= 300f && fraction > 0.06f) {
                val midAngle = Math.toRadians((startAngle + animSweep / 2).toDouble())
                val labelR = radius    // đặt text trên vành
                val lx = (cx + labelR * cos(midAngle)).toFloat()
                val ly = (cy + labelR * sin(midAngle)).toFloat()

                val percent = (fraction * 100).toInt()

                labelPaint.textSize = size * 0.048f
                percentPaint.textSize = size * 0.038f

                canvas.drawText(seg.label, lx, ly - size * 0.025f, labelPaint)
                canvas.drawText("$percent %", lx, ly + size * 0.04f, percentPaint)
            }

            startAngle += animSweep
        }

        // Vẽ text ở trung tâm
        centerTitlePaint.textSize = size * 0.1f
        centerSubPaint.textSize   = size * 0.065f

        if (centerTitle.isNotBlank()) {
            canvas.drawText(centerTitle, cx, cy - size * 0.04f, centerTitlePaint)
        }
        if (centerSubtitle.isNotBlank()) {
            canvas.drawText(centerSubtitle, cx, cy + size * 0.07f, centerSubPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Mặc định view là hình vuông: chiều cao = chiều rộng
        val w = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(w, w)
    }
}
