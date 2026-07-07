package com.uth.cashie.stats.report

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.database.SessionManager
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
     * @return Pair(tên file đã lưu vào Downloads, Uri của file) hoặc null nếu thất bại.
     */
    fun exportXlsx(context: Context, result: StatsResult, month: Int, year: Int): Pair<String, Uri>? {
        val fileName = "Cashie_BaoCao_T${month}_${year}.csv"
        var fileUri: Uri? = null
        try {
            val (out, uri) = openOutputStream(context, fileName)
            fileUri = uri
            if (out == null) return null
            // UTF-8 BOM – giúp Excel/Windows hiển thị đúng tiếng Việt
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            val writer = PrintWriter(OutputStreamWriter(out, Charsets.UTF_8))
            writeCsv(writer, result, month, year)
            writer.flush()
            out.close()

            if (fileUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cv = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                context.contentResolver.update(fileUri, cv, null, null)
            }

            if (fileUri != null) {
                scanFile(context, fileUri)
            }

            return if (fileUri != null) Pair(fileName, fileUri) else null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun scanFile(context: Context, uri: Uri) {
        try {
            context.contentResolver.notifyChange(uri, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CSV builder
    // ─────────────────────────────────────────────────────────────────────────

    private fun writeCsv(w: PrintWriter, r: StatsResult, month: Int, year: Int) {
        val s = r.summary
        val currency = SessionManager.getCurrency()

        // ── Tổng quan ──
        w.println("=== TỔNG QUAN THÁNG $month / $year ===")
        w.println("Mục,Giá trị")
        w.println("Thu nhập,${formatVND(s.totalIncome, currency)}")
        w.println("Chi tiêu,${formatVND(s.totalExpense, currency)}")
        w.println("Số dư,${formatVND(s.balance, currency)}")
        w.println("Số giao dịch,${s.totalTransactions}")
        w.println("Chi tiêu cao nhất,${formatVND(s.highestExpense, currency)}")
        w.println("TB chi/ngày,${formatVND(s.avgDailyExpense, currency)}")

        r.comparison?.let { cmp ->
            val iSign = if (cmp.incomeChange  >= 0) "+" else ""
            val eSign = if (cmp.expenseChange >= 0) "+" else ""
            w.println("Thay đổi thu nhập,$iSign${formatVND(cmp.incomeChange, currency)} (${"%.1f".format(cmp.incomeChangePercent)}%)")
            w.println("Thay đổi chi tiêu,$eSign${formatVND(cmp.expenseChange, currency)} (${"%.1f".format(cmp.expenseChangePercent)}%)")
        }

        w.println()

        // ── Chi theo danh mục ──
        w.println("=== CHI TIÊU THEO DANH MỤC ===")
        w.println("Danh mục,Emoji,Số tiền,Số GD,Tỉ lệ %")
        r.expenseByCategory.forEach { cat ->
            w.println("${cat.categoryName},${cat.emoji},${formatVND(cat.totalAmount, currency)},${cat.transactionCount},${"%.1f".format(cat.percentage)}%")
        }

        w.println()

        // ── Thu theo danh mục ──
        w.println("=== THU NHẬP THEO DANH MỤC ===")
        w.println("Danh mục,Emoji,Số tiền,Số GD,Tỉ lệ %")
        r.incomeByCategory.forEach { cat ->
            w.println("${cat.categoryName},${cat.emoji},${formatVND(cat.totalAmount, currency)},${cat.transactionCount},${"%.1f".format(cat.percentage)}%")
        }

        w.println()

        // ── Xu hướng theo tháng ──
        w.println("=== XU HƯỚNG THÁNG - NĂM $year ===")
        w.println("Tháng,Thu nhập,Chi tiêu,Số dư")
        r.monthlyTrends.forEach { t ->
            w.println("${t.label},${formatVND(t.income, currency)},${formatVND(t.expense, currency)},${formatVND(t.income - t.expense, currency)}")
        }

        w.println()

        // ── Chi theo quý ──
        w.println("=== CHI TIÊU THEO QUÝ - NĂM $year ===")
        w.println("Quý,Thu nhập,Chi tiêu")
        r.quarterlyStats.forEach { q ->
            w.println("${q.label},${formatVND(q.income, currency)},${formatVND(q.expense, currency)}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // File I/O
    // ─────────────────────────────────────────────────────────────────────────

    private fun openOutputStream(context: Context, fileName: String): Pair<OutputStream?, Uri?> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cv = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.IS_PENDING, 1)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.DATE_ADDED, System.currentTimeMillis() / 1000)
            }
            val uri = context.contentResolver
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv) ?: return Pair(null, null)
            val stream = context.contentResolver.openOutputStream(uri) ?: return Pair(null, null)
            Pair(stream, uri)
        } else {
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            dir.mkdirs()
            val file = File(dir, fileName)
            Pair(FileOutputStream(file), Uri.fromFile(file))
        }
    }
}
