package com.uth.cashie

import android.app.Application
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager

/**
 * Application class – khởi tạo các singleton khi app khởi động.
 */
class CashieApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Khởi tạo SessionManager để dùng getCurrentUserId() từ bất kỳ đâu
        SessionManager.init(this)
        // Khởi động Room Database (lazy, chỉ tạo file khi query đầu tiên)
        CashieDatabase.getInstance(this)
    }
}
