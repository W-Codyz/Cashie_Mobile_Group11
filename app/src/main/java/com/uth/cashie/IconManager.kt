package com.uth.cashie

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Quản lý dynamic app icon.
 *
 * Cơ chế: Lưu index icon đang chọn vào SharedPreferences.
 * Icon picker trong SettingActivity dùng index này để hiển thị viền chọn.
 *
 * Lưu ý quan trọng:
 * - Dynamic icon thực sự (thay đổi icon trên launcher) đòi hỏi activity-alias
 *   với LAUNCHER intent-filter, nhưng cơ chế này gây lỗi PackageManager cache
 *   trên nhiều thiết bị/emulator khi reinstall.
 * - Giải pháp hiện tại: lưu lựa chọn icon vào DB và SharedPreferences,
 *   hiển thị đúng icon được chọn trong UI Settings.
 *   Icon trên launcher giữ nguyên icon mặc định của app.
 */
object IconManager {

    private const val TAG       = "IconManager"
    private const val PREF_NAME = "cashie_icon"
    private const val KEY_INDEX = "selected_icon_index"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /** Số lượng icon có sẵn (0 = mặc định, 1–9 = icon tùy chỉnh) */
    const val ICON_COUNT = 10

    /**
     * Lưu index icon được chọn.
     * @param iconIndex 0 = mặc định, 1–9 = icon tùy chỉnh
     */
    fun setIcon(context: Context, iconIndex: Int) {
        val safeIndex = iconIndex.coerceIn(0, ICON_COUNT - 1)
        try {
            if (::prefs.isInitialized) {
                prefs.edit().putInt(KEY_INDEX, safeIndex).apply()
            } else {
                context.applicationContext
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit().putInt(KEY_INDEX, safeIndex).apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "setIcon failed: ${e.message}")
        }
    }

    /**
     * Trả về index icon đang được chọn.
     * Mặc định 0 nếu chưa chọn lần nào.
     */
    fun getCurrentIconIndex(context: Context): Int {
        return try {
            if (::prefs.isInitialized) {
                prefs.getInt(KEY_INDEX, 0)
            } else {
                context.applicationContext
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .getInt(KEY_INDEX, 0)
            }
        } catch (e: Exception) {
            Log.w(TAG, "getCurrentIconIndex failed: ${e.message}")
            0
        }
    }

    /** Luôn available vì không dùng PackageManager nữa */
    fun isAvailable(context: Context): Boolean = true
}
