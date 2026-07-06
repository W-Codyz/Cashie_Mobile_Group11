package com.uth.cashie

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log

/**
 * Quản lý dynamic app icon.
 *
 * Cơ chế: Sử dụng activity-alias trong AndroidManifest, enable alias mong muốn
 * và disable các alias khác.
 */
object IconManager {

    private const val TAG       = "IconManager"
    private const val PREF_NAME = "cashie_icon"
    private const val KEY_INDEX = "selected_icon_index"

    private lateinit var prefs: SharedPreferences

    /** Danh sách tên các activity-alias trong AndroidManifest (index 0 = mặc định) */
    private val ALIAS_NAMES = listOf(
        "com.uth.cashie.IconDefault",
        "com.uth.cashie.Icon1",
        "com.uth.cashie.Icon2",
        "com.uth.cashie.Icon3",
        "com.uth.cashie.Icon4",
        "com.uth.cashie.Icon5",
        "com.uth.cashie.Icon6",
        "com.uth.cashie.Icon7",
        "com.uth.cashie.Icon8",
        "com.uth.cashie.Icon9",
    )

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        // Áp dụng icon đã lưu trước đó (nếu có) khi app khởi động
        val savedIndex = getCurrentIconIndex(context)
        applyIcon(context, savedIndex, skipSave = true)
    }

    /** Số lượng icon có sẵn (0 = mặc định, 1–9 = icon tùy chỉnh) */
    const val ICON_COUNT = 10

    /**
     * Lưu index icon được chọn và thay đổi icon app.
     * @param iconIndex 0 = mặc định, 1–9 = icon tùy chỉnh
     */
    fun setIcon(context: Context, iconIndex: Int) {
        val safeIndex = iconIndex.coerceIn(0, ICON_COUNT - 1)
        try {
            applyIcon(context, safeIndex, skipSave = false)
        } catch (e: Exception) {
            Log.e(TAG, "setIcon failed: ${e.message}")
        }
    }

    private fun applyIcon(context: Context, iconIndex: Int, skipSave: Boolean = false) {
        val pm = context.packageManager
        
        // Disable tất cả các alias trước
        ALIAS_NAMES.forEach { aliasName ->
            pm.setComponentEnabledSetting(
                ComponentName(context, aliasName),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        
        // Enable alias mong muốn
        pm.setComponentEnabledSetting(
            ComponentName(context, ALIAS_NAMES[iconIndex]),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        
        // Lưu index vào SharedPreferences (nếu cần)
        if (!skipSave) {
            if (::prefs.isInitialized) {
                prefs.edit().putInt(KEY_INDEX, iconIndex).apply()
            } else {
                context.applicationContext
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit().putInt(KEY_INDEX, iconIndex).apply()
            }
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

    /** Luôn available */
    fun isAvailable(context: Context): Boolean = true
}
