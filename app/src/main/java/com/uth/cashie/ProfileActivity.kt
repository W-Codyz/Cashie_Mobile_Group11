package com.uth.cashie

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uth.cashie.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Toolbar "Sửa" → EditProfileActivity
        binding.btnEdit.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Menu: Chỉnh sửa thông tin → EditProfileActivity
        binding.menuEditInfo.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.menuChangePassword.setOnClickListener {
            // TODO: navigate to ChangePasswordActivity
        }

        binding.menuCurrency.setOnClickListener {
            // TODO: navigate to currency settings
        }

        binding.menuStats.setOnClickListener {
            // Coming soon — no action
        }

        binding.menuLogout.setOnClickListener {
            // TODO: logout logic
        }
    }
}
