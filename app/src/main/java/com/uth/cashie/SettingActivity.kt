package com.uth.cashie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uth.cashie.databinding.ActivitySettingBinding
import android.content.Intent

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

            finishAffinity()
        }
    }
}