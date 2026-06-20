package com.uth.cashie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uth.cashie.databinding.ActivitySetupBinding

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding

    /** true = VND selected, false = USD selected */
    private var isVndSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCurrencySelection()

        binding.btnStartUsing.setOnClickListener {
            // TODO: save setup and navigate to MainActivity
        }
    }

    private fun setupCurrencySelection() {
        updateCurrencyUi()

        binding.cardVnd.setOnClickListener {
            isVndSelected = true
            updateCurrencyUi()
        }
        binding.radioVnd.setOnClickListener {
            isVndSelected = true
            updateCurrencyUi()
        }
        binding.cardUsd.setOnClickListener {
            isVndSelected = false
            updateCurrencyUi()
        }
        binding.radioUsd.setOnClickListener {
            isVndSelected = false
            updateCurrencyUi()
        }
    }

    private fun updateCurrencyUi() {
        if (isVndSelected) {
            binding.cardVnd.setBackgroundResource(R.drawable.bg_currency_card_selected)
            binding.cardUsd.setBackgroundResource(R.drawable.bg_currency_card_default)
            binding.radioVnd.isChecked = true
            binding.radioUsd.isChecked = false
            binding.tilBalance.suffixText = getString(R.string.setup_suffix_vnd)
        } else {
            binding.cardVnd.setBackgroundResource(R.drawable.bg_currency_card_default)
            binding.cardUsd.setBackgroundResource(R.drawable.bg_currency_card_selected)
            binding.radioVnd.isChecked = false
            binding.radioUsd.isChecked = true
            binding.tilBalance.suffixText = getString(R.string.setup_suffix_usd)
        }
    }
}
