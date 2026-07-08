package com.uth.cashie

import android.app.Application
import android.content.Context
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager

/**
 * Application class – khởi tạo các singleton khi app khởi động.
 */
class CashieApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(ThemeManager.wrapContext(base))
    }

    override fun onCreate() {
        super.onCreate()
        // Khởi tạo SessionManager để dùng getCurrentUserId() từ bất kỳ đâu
        SessionManager.init(this)
        // Khởi tạo ThemeManager để dùng màu chủ đề từ bất kỳ đâu
        ThemeManager.init(this)
        // Khởi tạo IconManager để lưu icon đang chọn
        IconManager.init(this)
        // Khởi động Room Database (lazy, chỉ tạo file khi query đầu tiên)
        CashieDatabase.getInstance(this)
        // Khởi tạo TransactionRepository với database
        com.uth.cashie.data.TransactionRepository.init(this)
    }
}
