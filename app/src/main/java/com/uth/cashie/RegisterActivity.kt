package com.uth.cashie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uth.cashie.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            // TODO: validate & register logic
        }

        binding.tvLoginLink.setOnClickListener {
            // TODO: navigate to LoginActivity
            finish()
        }
    }
}
