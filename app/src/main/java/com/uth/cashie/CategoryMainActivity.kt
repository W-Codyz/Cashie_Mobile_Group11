package com.uth.cashie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class CategoryMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ✅ Sửa: dùng layout activity_category_main thay vì activity_main
        setContentView(R.layout.activity_category_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // (Tùy chọn) Nếu có toolbar, bạn có thể setup ở đây
    }
}