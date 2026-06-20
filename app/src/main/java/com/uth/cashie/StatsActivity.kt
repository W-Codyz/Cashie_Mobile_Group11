package com.uth.cashie

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.databinding.ActivityStatsBinding

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private lateinit var months: Array<String>
    private var monthIndex = 5

    private val totalIncome = 12_500_000L
    private val totalExpense = 1_165_000L
    private val totalTransactions = 8
    private val highestExpense = 450_000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        months = resources.getStringArray(R.array.months)
        setupSummary()
        setupMonthNavigation()
        setupBottomNav()
    }

    private fun setupSummary() {
        binding.tvStatsIncome.text = formatVND(totalIncome)
        binding.tvStatsExpense.text = formatVND(totalExpense)
        binding.tvStatsBalance.text = formatVND(totalIncome - totalExpense)
        binding.tvTotalTransactions.text = totalTransactions.toString()
        binding.tvHighestExpense.text = formatVND(highestExpense)
    }

    private fun setupMonthNavigation() {
        updateMonthLabel()
        binding.btnPrevMonth.setOnClickListener {
            if (monthIndex > 0) {
                monthIndex--
                updateMonthLabel()
            }
        }
        binding.btnNextMonth.setOnClickListener {
            if (monthIndex < 11) {
                monthIndex++
                updateMonthLabel()
            }
        }
    }

    private fun updateMonthLabel() {
        binding.tvCurrentMonth.text = getString(R.string.month_year_format, months[monthIndex], 2026)
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_stats
        binding.bottomNav.itemActiveIndicatorColor =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_container))
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_stats -> true
                R.id.nav_categories -> {
                    // TODO: mở màn hình categories
                    true
                }
                R.id.nav_settings -> {
                    // TODO: mở màn hình settings
                    true
                }
                else -> false
            }
        }
    }
}
