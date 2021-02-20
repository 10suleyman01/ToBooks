package com.suleyman.tobooks.ui.activity.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.suleyman.tobooks.databinding.ActivityLoginSplashBinding
import com.suleyman.tobooks.ui.activity.login.LoginActivity

class LoginSplashActivity : AppCompatActivity() {

    private var _binding: ActivityLoginSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBeginUse.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}