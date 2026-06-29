package com.uth.cashie.database

import android.content.Context
import android.content.SharedPreferences

/**
 * Quản lý session user đang đăng nhập.
 *
 * Dùng SharedPreferences để lưu userId giữa các lần khởi động app.
 * Dùng thay vì truyền userId qua từng màn hình.
 */
object SessionManager {

    private const val PREF_NAME     = "cashie_session"
    private const val KEY_USER_ID   = "current_user_id"
    private const val NO_USER       = -1L

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /** Lưu userId khi đăng nhập thành công */
    fun setCurrentUser(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    /** Lấy userId hiện tại, trả về -1 nếu chưa đăng nhập */
    fun getCurrentUserId(): Long = prefs.getLong(KEY_USER_ID, NO_USER)

    /** Kiểm tra đã đăng nhập chưa */
    fun isLoggedIn(): Boolean = getCurrentUserId() != NO_USER

    /** Xóa session khi đăng xuất */
    fun logout() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }
}
