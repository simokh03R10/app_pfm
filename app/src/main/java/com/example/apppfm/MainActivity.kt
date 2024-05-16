package com.example.apppfm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apppfm.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            // If user is not logged in, redirect to LoginActivity
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        binding.buttonsellfragement.setOnClickListener(){
            startActivity(Intent(this, CreatepostActivity::class.java))
            finish()
        }

        binding.bottomnavicons.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    binding.textviewtitle.text = "Home"
                    supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment()).commit()
                    true
                }
                R.id.menu_favoris -> {
                    binding.textviewtitle.text = "Mes Favoris"
                    supportFragmentManager.beginTransaction().replace(R.id.container, FavorisFragment()).commit()
                    true
                }

                R.id.menu_chat -> {
                    binding.textviewtitle.text = "Mes Messages"

                    supportFragmentManager.beginTransaction().replace(R.id.container, ChatFragment()).commit()
                    true
                }
                R.id.menu_profile -> {
                    binding.textviewtitle.text = "Mon profil"
                    supportFragmentManager.beginTransaction().replace(R.id.container, ProfileFragment()).commit()
                    true
                }
                else -> false
            }
        }

    }
}
/*
1) Create & Setup the Project
2) Bottom Navigation Menu
3) Bottom Navigation Item Click | Handle Fragment Switch
4) Login Options
5) Connect with Firebase
6) Check the User Login Status

 */