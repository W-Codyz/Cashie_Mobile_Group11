package com.uth.cashie.database

import android.content.Context
import android.content.SharedPreferences
import com.uth.cashie.database.SessionManager

object SessionManager {

    private const val PREF_NAME = "cashie_session"

    private const val KEY_USER_ID = "current_user_id"
    private const val KEY_REMEMBER = "remember_login"
    private const val KEY_CURRENCY = "current_currency"

    private const val NO_USER = -1L
    private const val DEFAULT_CURRENCY = "VND"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setCurrentUser(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun getCurrentUserId(): Long =
        prefs.getLong(KEY_USER_ID, NO_USER)

    fun isLoggedIn(): Boolean =
        getCurrentUserId() != NO_USER

    // ====== THÊM MỚI ======

    fun setRememberLogin(value: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER, value).apply()
    }

    fun isRememberLogin(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER, false)
    }

    fun setCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(): String {
        return prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    }

    // ======================

    fun logout() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_REMEMBER)
            .apply()
    }
}