package com.uth.cashie

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.uth.cashie.database.SessionManager
import com.uth.cashie.databinding.ActivitySplashBinding


class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SessionManager.init(this)

        Handler(Looper.getMainLooper()).postDelayed({

            if (SessionManager.isRememberLogin() &&
                SessionManager.isLoggedIn()
            ) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish()

        }, 1500)
    }
}