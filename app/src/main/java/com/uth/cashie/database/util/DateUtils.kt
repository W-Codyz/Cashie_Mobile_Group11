package com.uth.cashie.database.util

import java.util.Calendar

/**
 * Tiện ích tính khoảng thời gian dạng Unix timestamp ms.
 * Dùng cho query BETWEEN :startMs AND :endMs trong TransactionDao.
 */
object DateUtils {

    /** Lấy timestamp đầu tháng (00:00:00.000 ngày 1) */
    fun startOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** Lấy timestamp cuối tháng (23:59:59.999 ngày cuối) */
    fun endOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return cal.timeInMillis
    }

    /** Lấy timestamp đầu năm */
    fun startOfYear(year: Int): Long = startOfMonth(year, 1)

    /** Lấy timestamp cuối năm */
    fun endOfYear(year: Int): Long = endOfMonth(year, 12)

    /** Lấy timestamp đầu quý (Q1=T1, Q2=T4, Q3=T7, Q4=T10) */
    fun startOfQuarter(year: Int, quarter: Int): Long {
        val month = (quarter - 1) * 3 + 1
        return startOfMonth(year, month)
    }

    /** Lấy timestamp cuối quý */
    fun endOfQuarter(year: Int, quarter: Int): Long {
        val month = quarter * 3
        return endOfMonth(year, month)
    }

    /** Lấy timestamp đầu ngày */
    fun startOfDay(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** Lấy timestamp cuối ngày */
    fun endOfDay(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}
