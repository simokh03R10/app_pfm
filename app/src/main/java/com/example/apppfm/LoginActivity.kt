package com.example.apppfm

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apppfm.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialogue_progress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        dialogue_progress = ProgressDialog(this)
        dialogue_progress.setTitle("Connexion")
        dialogue_progress.setCanceledOnTouchOutside(false)

        binding.textviewbacktoregister.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
        binding.btnbacklogin.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }


        binding.loginButton.setOnClickListener {
            validateData()
        }
    }

    private var email = ""
    private var pass = ""

    private fun validateData() {
        email = binding.loginemailfieldTET.text.toString().trim()
        pass = binding.loginpasswordfieldTET.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.loginemailfieldTET.error = "email invalide"
            binding.loginemailfieldTET.requestFocus()

        } else if (pass.isEmpty()) {
            binding.loginpasswordfieldTET.error = "Entrer votre mot de passe"
            binding.loginpasswordfieldTET.requestFocus()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {
        dialogue_progress.setMessage("En cours")
        dialogue_progress.show()
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                dialogue_progress.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {
                dialogue_progress.dismiss()
                Toast.makeText(this, "erreur lors connexion", Toast.LENGTH_SHORT).show()
            }
    }
}
