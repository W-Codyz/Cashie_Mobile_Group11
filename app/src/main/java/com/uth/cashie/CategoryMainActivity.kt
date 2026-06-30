package com.uth.cashie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.uth.cashie.databinding.ActivityCategoryMainBinding

class CategoryMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // navController available for future in-fragment navigation
        val navController = navHostFragment.navController

        setupBottomNav(binding.bottomNav, R.id.nav_categories)
    }

    override fun onResume() {
        super.onResume()
        binding.cardBottomNav.strokeColor = ThemeManager.getThemeColorInt()
    }
}
