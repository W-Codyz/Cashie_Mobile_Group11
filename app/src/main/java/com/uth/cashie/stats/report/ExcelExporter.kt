package com.uth.cashie.stats.report

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.stats.model.StatsResult
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter

/**
 * Xuất báo cáo thống kê dạng CSV (có thể mở bằng Excel / Google Sheets).
 * Không cần thư viện ngoài, tương thích mọi phiên bản Android.
 *
 * File CSV dùng dấu phẩy phân cách, encoding UTF-8 với BOM để Excel nhận đúng tiếng Việt.
 */
object ExcelExporter {

    /**
     * @return tên file đã lưu vào Downloads, hoặc null nếu thất bại.
     */
    fun exportXlsx(context: Context, result: StatsResult, month: Int, year: Int): String? {
        val fileName = "Cashie_BaoCao_T${month}_${year}.csv"
        val out = openOutputStream(context, fileName) ?: return null
        return try {
            // UTF-8 BOM – giúp Excel/Windows hiển thị đúng tiếng Việt
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            val writer = PrintWriter(OutputStreamWriter(out, Charsets.UTF_8))
            writeCsv(writer, result, month, year)
            writer.flush()
            out.close()
            fileName
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CSV builder
    // ─────────────────────────────────────────────────────────────────────────

    private fun writeCsv(w: PrintWriter, r: StatsResult, month: Int, year: Int) {
        val s = r.summary

        // ── Tổng quan ──
        w.println("=== TỔNG QUAN THÁNG $month / $year ===")
        w.println("Mục,Giá trị")
        w.println("Thu nhập,${formatVND(s.totalIncome)}")
        w.println("Chi tiêu,${formatVND(s.totalExpense)}")
        w.println("Số dư,${formatVND(s.balance)}")
        w.println("Số giao dịch,${s.totalTransactions}")
        w.println("Chi tiêu cao nhất,${formatVND(s.highestExpense)}")
        w.println("TB chi/ngày,${formatVND(s.avgDailyExpense)}")

        r.comparison?.let { cmp ->
            val iSign = if (cmp.incomeChange  >= 0) "+" else ""
            val eSign = if (cmp.expenseChange >= 0) "+" else ""
            w.println("Thay đổi thu nhập,$iSign${formatVND(cmp.incomeChange)} (${"%.1f".format(cmp.incomeChangePercent)}%)")
            w.println("Thay đổi chi tiêu,$eSign${formatVND(cmp.expenseChange)} (${"%.1f".format(cmp.expenseChangePercent)}%)")
        }

        w.println()

        // ── Chi theo danh mục ──
        w.println("=== CHI TIÊU THEO DANH MỤC ===")
        w.println("Danh mục,Emoji,Số tiền,Số GD,Tỉ lệ %")
        r.expenseByCategory.forEach { cat ->
            w.println("${cat.categoryName},${cat.emoji},${formatVND(cat.totalAmount)},${cat.transactionCount},${"%.1f".format(cat.percentage)}%")
        }

        w.println()

        // ── Thu theo danh mục ──
        w.println("=== THU NHẬP THEO DANH MỤC ===")
        w.println("Danh mục,Emoji,Số tiền,Số GD,Tỉ lệ %")
        r.incomeByCategory.forEach { cat ->
            w.println("${cat.categoryName},${cat.emoji},${formatVND(cat.totalAmount)},${cat.transactionCount},${"%.1f".format(cat.percentage)}%")
        }

        w.println()

        // ── Xu hướng theo tháng ──
        w.println("=== XU HƯỚNG THÁNG - NĂM $year ===")
        w.println("Tháng,Thu nhập,Chi tiêu,Số dư")
        r.monthlyTrends.forEach { t ->
            w.println("${t.label},${formatVND(t.income)},${formatVND(t.expense)},${formatVND(t.income - t.expense)}")
        }

        w.println()

        // ── Chi theo quý ──
        w.println("=== CHI TIÊU THEO QUÝ - NĂM $year ===")
        w.println("Quý,Thu nhập,Chi tiêu")
        r.quarterlyStats.forEach { q ->
            w.println("${q.label},${formatVND(q.income)},${formatVND(q.expense)}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // File I/O
    // ─────────────────────────────────────────────────────────────────────────

    private fun openOutputStream(context: Context, fileName: String): OutputStream? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cv = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = context.contentResolver
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv) ?: return null
            cv.clear()
            cv.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, cv, null, null)
            context.contentResolver.openOutputStream(uri)
        } else {
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            dir.mkdirs()
            FileOutputStream(File(dir, fileName))
        }
    }
}
