package com.uth.cashie.stats.report

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.stats.model.StatsResult
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Xuất báo cáo thống kê tháng dưới dạng PDF.
 * Dùng Android PdfDocument (built-in) — không cần thư viện bên ngoài.
 */
object ReportExporter {

    // ── Màu sắc ──────────────────────────────────────────────────────────────
    private val COLOR_PRIMARY   = Color.parseColor("#22CC00")
    private val COLOR_HEADER_BG = Color.parseColor("#E8F5E9")
    private val COLOR_EXPENSE   = Color.parseColor("#FF4444")
    private val COLOR_TEXT      = Color.parseColor("#1A1A1A")
    private val COLOR_MUTED     = Color.parseColor("#888888")
    private val COLOR_DIVIDER   = Color.parseColor("#E0E0E0")

    // ── Kích thước trang A4 ───────────────────────────────────────────────────
    private const val PAGE_WIDTH  = 595   // pt ≈ A4 width
    private const val PAGE_HEIGHT = 842   // pt ≈ A4 height
    private const val MARGIN      = 40f

    /**
     * Tạo file PDF từ [StatsResult].
     *
     * @return  đường dẫn file đã lưu, hoặc null nếu có lỗi.
     */
    fun exportPdf(context: Context, result: StatsResult, month: Int, year: Int): String? {
        val doc = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page     = doc.startPage(pageInfo)
            val canvas   = page.canvas

            drawReport(canvas, result, month, year)

            doc.finishPage(page)

            val fileName = "Cashie_BaoCao_T${month}_${year}.pdf"
            val outStream = openOutputStream(context, fileName) ?: return null
            doc.writeTo(outStream)
            outStream.flush()
            outStream.close()

            return fileName
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            doc.close()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Drawing helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawReport(canvas: Canvas, result: StatsResult, month: Int, year: Int) {
        var y = MARGIN

        // ── Header ──────────────────────────────────────────────────────────
        val headerPaint = Paint().apply { color = COLOR_HEADER_BG }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 80f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize  = 22f
            color     = COLOR_PRIMARY
            typeface  = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📊 BÁO CÁO TÀI CHÍNH", MARGIN, 36f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f
            color    = COLOR_MUTED
        }
        canvas.drawText("Tháng $month / $year   •   Cashie App", MARGIN, 58f, subPaint)
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            color    = COLOR_MUTED
        }
        val today = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val dateText = "Xuất ngày: $today"
        val dtWidth = datePaint.measureText(dateText)
        canvas.drawText(dateText, PAGE_WIDTH - MARGIN - dtWidth, 58f, datePaint)

        y = 96f

        // ── Tổng quan ────────────────────────────────────────────────────────
        y = drawSectionTitle(canvas, "TỔNG QUAN THÁNG", y)

        val s = result.summary
        val rows = listOf(
            Triple("Thu nhập",     formatVND(s.totalIncome),   COLOR_PRIMARY),
            Triple("Chi tiêu",     formatVND(s.totalExpense),  COLOR_EXPENSE),
            Triple("Số dư",        formatVND(s.balance),       if (s.balance >= 0) COLOR_PRIMARY else COLOR_EXPENSE),
            Triple("Giao dịch",    "${s.totalTransactions} lần",  COLOR_TEXT),
            Triple("Chi cao nhất", formatVND(s.highestExpense),COLOR_EXPENSE),
            Triple("TB ngày/chi",  formatVND(s.avgDailyExpense),COLOR_TEXT)
        )
        for ((label, value, valColor) in rows) {
            y = drawKeyValue(canvas, label, value, y, valColor)
        }

        y += 12f
        drawDivider(canvas, y); y += 16f

        // ── So sánh tháng trước ───────────────────────────────────────────────
        result.comparison?.let { cmp ->
            y = drawSectionTitle(canvas, "SO SÁNH VỚI THÁNG TRƯỚC", y)
            val sign  = { v: Long -> if (v >= 0) "+" else "" }
            val color = { v: Long -> if (v >= 0) COLOR_PRIMARY else COLOR_EXPENSE }
            y = drawKeyValue(canvas, "Thay đổi thu nhập",
                "${sign(cmp.incomeChange)}${formatVND(cmp.incomeChange)}  (${"%.1f".format(cmp.incomeChangePercent)}%)",
                y, color(cmp.incomeChange))
            y = drawKeyValue(canvas, "Thay đổi chi tiêu",
                "${sign(cmp.expenseChange)}${formatVND(cmp.expenseChange)}  (${"%.1f".format(cmp.expenseChangePercent)}%)",
                y, if (cmp.expenseChange > 0) COLOR_EXPENSE else COLOR_PRIMARY)
            y += 12f
            drawDivider(canvas, y); y += 16f
        }

        // ── Chi theo danh mục ────────────────────────────────────────────────
        if (result.expenseByCategory.isNotEmpty()) {
            y = drawSectionTitle(canvas, "CHI TIÊU THEO DANH MỤC", y)
            for (cat in result.expenseByCategory.take(8)) {
                val label = "${cat.emoji} ${cat.categoryName}"
                val value = "${formatVND(cat.totalAmount)}  (${"%.1f".format(cat.percentage)}%)"
                y = drawKeyValue(canvas, label, value, y, COLOR_EXPENSE)
                // Thanh tiến trình mini
                drawMiniBar(canvas, cat.percentage, y - 4f)
                y += 4f
            }
            y += 12f
            drawDivider(canvas, y); y += 16f
        }

        // ── Thu theo danh mục ────────────────────────────────────────────────
        if (result.incomeByCategory.isNotEmpty()) {
            y = drawSectionTitle(canvas, "THU NHẬP THEO DANH MỤC", y)
            for (cat in result.incomeByCategory.take(6)) {
                val label = "${cat.emoji} ${cat.categoryName}"
                val value = "${formatVND(cat.totalAmount)}  (${"%.1f".format(cat.percentage)}%)"
                y = drawKeyValue(canvas, label, value, y, COLOR_PRIMARY)
            }
            y += 12f
            drawDivider(canvas, y); y += 16f
        }

        // ── Xu hướng theo quý ────────────────────────────────────────────────
        y = drawSectionTitle(canvas, "XU HƯỚNG CHI TIÊU THEO QUÝ - NĂM $year", y)
        for (q in result.quarterlyStats) {
            y = drawKeyValue(canvas, q.label,
                "Chi: ${formatVND(q.expense)}   Thu: ${formatVND(q.income)}", y, COLOR_TEXT)
        }

        // ── Footer ────────────────────────────────────────────────────────────
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
            color    = COLOR_MUTED
        }
        canvas.drawText("© Cashie – Mobile Group 11", MARGIN,
            (PAGE_HEIGHT - 20).toFloat(), footerPaint)
    }

    private fun drawSectionTitle(canvas: Canvas, title: String, y: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize  = 13f
            color     = COLOR_PRIMARY
            typeface  = Typeface.DEFAULT_BOLD
        }
        // Nền highlight
        val bgPaint = Paint().apply { color = COLOR_HEADER_BG }
        canvas.drawRect(MARGIN - 4f, y - 14f, PAGE_WIDTH - MARGIN + 4f, y + 4f, bgPaint)
        canvas.drawText(title, MARGIN, y, paint)
        return y + 18f
    }

    private fun drawKeyValue(canvas: Canvas, key: String, value: String,
                             y: Float, valueColor: Int): Float {
        val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            color    = COLOR_TEXT
        }
        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize  = 12f
            color     = valueColor
            typeface  = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(key, MARGIN, y, keyPaint)
        val vw = valPaint.measureText(value)
        canvas.drawText(value, PAGE_WIDTH - MARGIN - vw, y, valPaint)
        return y + 18f
    }

    private fun drawDivider(canvas: Canvas, y: Float) {
        val paint = Paint().apply { color = COLOR_DIVIDER; strokeWidth = 0.8f }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
    }

    private fun drawMiniBar(canvas: Canvas, percent: Float, y: Float) {
        val maxW = PAGE_WIDTH - MARGIN * 2
        val bgPaint  = Paint().apply { color = Color.parseColor("#DCF7D8") }
        val fillPaint = Paint().apply { color = COLOR_EXPENSE }
        canvas.drawRect(MARGIN, y, MARGIN + maxW, y + 3f, bgPaint)
        canvas.drawRect(MARGIN, y, MARGIN + maxW * percent / 100f, y + 3f, fillPaint)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // File I/O
    // ─────────────────────────────────────────────────────────────────────────

    private fun openOutputStream(context: Context, fileName: String): OutputStream? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ → dùng MediaStore (Downloads)
            val cv = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = context.contentResolver
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv) ?: return null
            cv.clear()
            cv.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, cv, null, null)
            context.contentResolver.openOutputStream(uri)
        } else {
            // Android 9 trở xuống → ghi thẳng vào Downloads
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            dir.mkdirs()
            FileOutputStream(File(dir, fileName))
        }
    }
}
