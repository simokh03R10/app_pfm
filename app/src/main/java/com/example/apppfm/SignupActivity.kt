package com.example.apppfm

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apppfm.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.Locale

class SignupActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivitySignupBinding
    private lateinit var dialogue_progress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        dialogue_progress = ProgressDialog(this)
        dialogue_progress.setTitle("Inscription")
        dialogue_progress.setCanceledOnTouchOutside(false)

        binding.textviewlogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.btnbacksignup.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
        binding.textviewbacktologin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


        binding.signupButton.setOnClickListener {
            validatedata()
        }
    }

    private fun validatedata() {
        val email = binding.signupemailfieldTET.text.toString().trim()
        val pass = binding.signuppasswordfieldTET.text.toString().trim()
        val cpass = binding.signupconfirmpassfieldTET.text.toString().trim()
        val fname = binding.signupfullnamefieldTET.text.toString().trim()

        if (pass != cpass) {
            binding.signuppasswordfieldTET.error = "les mots de passes ne sont pas identiques"
            binding.signupconfirmpassfieldTET.error = "les mots de passes ne sont pas identiques"
            binding.signuppasswordfieldTET.requestFocus()

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signupemailfieldTET.error = "email n'est pas valide"
            binding.signupemailfieldTET.requestFocus()

        } else if (fname.isEmpty()) {
            binding.signupfullnamefieldTET.error = "remplir le nom complet svp"
            binding.signupfullnamefieldTET.requestFocus()

        } else {
            registerUser(email, pass, fname)
        }
    }

    private fun registerUser(email: String, pass: String, fname: String) {
        dialogue_progress.setMessage("Création de votre compte")
        dialogue_progress.show()

        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                modifierinfoutilisateur(authResult.user?.email, authResult.user?.uid, pass, fname)
            }.addOnFailureListener { e ->
                dialogue_progress.dismiss()
                Toast.makeText(this, "Erreur lors de l'inscription: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun modifierinfoutilisateur(email: String?, uid: String?, pass: String, fname: String) {
        dialogue_progress.setMessage("Sauvegarde de données")
        val hashmap_de_donnes = HashMap<String, Any>()
        hashmap_de_donnes["nom_utilisateur"] = fname
        hashmap_de_donnes["numero_de_tele"] = ""
        hashmap_de_donnes["uid"] = uid ?: ""
        hashmap_de_donnes["email_utilisateur"] = email ?: ""
        hashmap_de_donnes["profile_image_utilisateur"] = ""
        hashmap_de_donnes["password_utilisateur"] = pass
        hashmap_de_donnes["ville_utilisateur"] = ""
        hashmap_de_donnes["location_utilisateur"] = ""
        hashmap_de_donnes["nombres_pubs_utilisateur"] = ""
        val dateFormat = SimpleDateFormat("dd--MM--yyyy", Locale.getDefault())
        val dateAujourdhui = dateFormat.format(Date())

        hashmap_de_donnes["registred_at"] = dateAujourdhui.toString()
        hashmap_de_donnes["en_ligne"] = true

        val reference = FirebaseDatabase.getInstance().getReference("Utilisateurs")
        uid?.let {
            reference.child(it)
                .setValue(hashmap_de_donnes)
                .addOnSuccessListener {
                    dialogue_progress.dismiss()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }.addOnFailureListener {
                    dialogue_progress.dismiss()
                    Toast.makeText(this, "Erreur lors de la sauvegarde des données", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
