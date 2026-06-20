package com.uth.cashie

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uth.cashie.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    /** true = VND selected, false = USD selected */
    private var isVndSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        setupCurrencySelection()

        binding.btnChangePhoto.setOnClickListener {
            // TODO: open image picker
        }

        binding.btnSave.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupCurrencySelection() {
        updateCurrencyUi()

        binding.cardEditVnd.setOnClickListener {
            isVndSelected = true
            updateCurrencyUi()
        }
        binding.radioEditVnd.setOnClickListener {
            isVndSelected = true
            updateCurrencyUi()
        }
        binding.cardEditUsd.setOnClickListener {
            isVndSelected = false
            updateCurrencyUi()
        }
        binding.radioEditUsd.setOnClickListener {
            isVndSelected = false
            updateCurrencyUi()
        }
    }

    private fun updateCurrencyUi() {
        if (isVndSelected) {
            binding.cardEditVnd.setBackgroundResource(R.drawable.bg_currency_card_selected)
            binding.cardEditUsd.setBackgroundResource(R.drawable.bg_currency_card_default)
            binding.radioEditVnd.isChecked = true
            binding.radioEditUsd.isChecked = false
        } else {
            binding.cardEditVnd.setBackgroundResource(R.drawable.bg_currency_card_default)
            binding.cardEditUsd.setBackgroundResource(R.drawable.bg_currency_card_selected)
            binding.radioEditVnd.isChecked = false
            binding.radioEditUsd.isChecked = true
        }
    }
}
