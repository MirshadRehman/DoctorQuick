package com.example.doctorquick

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.doctorquick.auth.WelcomeActivity
import com.example.doctorquick.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}
