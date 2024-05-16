package com.example.apppfm

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.apppfm.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupbtnwelcome.setOnClickListener {
            startActivity(Intent(this,SignupActivity::class.java))
            finish()
        }
        binding.signinbtnwelcome.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }

    }
}