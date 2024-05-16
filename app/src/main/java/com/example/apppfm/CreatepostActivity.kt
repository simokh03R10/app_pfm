package com.example.apppfm

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.apppfm.databinding.ActivityCreatepostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreatepostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatepostBinding
    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatepostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Enregistrement de la publication...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnbackhome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnaddphoto.setOnClickListener {
            imagePickDialog()
        }

        // Add action to the 'btnsubmit' button
        binding.btnsubmit.setOnClickListener {
            val title = binding.titleEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim()
            val location = binding.locationEditText.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Veuillez saisir un titre et une description.", Toast.LENGTH_SHORT).show()
            } else {
                progressDialog.show()
                uploadPost(title, description, location)
            }
        }
    }

    // Image Selection Dialog
    private fun imagePickDialog() {
        val popmenu = PopupMenu(this, binding.btnaddphoto)
        popmenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popmenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popmenu.show()
        popmenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> pickImageCamera()
                2 -> pickImageGallery()
            }
            true
        }
    }

    // Pick Image from Camera
    private fun pickImageCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image_title")
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image_description")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    // Pick Image from Gallery
    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    // Result Launchers for Camera and Gallery
    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var areAllGranted = true
            for (isGranted in result.values) {
                areAllGranted = areAllGranted && isGranted
            }
            if (areAllGranted) {
                pickImageCamera()
            } else {
                Log.d(ContentValues.TAG, "requestCameraPermissions: Au moins une des permissions est refusée")
                Toast.makeText(this, "Les permissions de la caméra ou du stockage ont été refusées", Toast.LENGTH_LONG).show()
            }
        }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickImageGallery()
            } else {
                Log.d(ContentValues.TAG, "requestStoragePermission: Permission de stockage refusée")
                Toast.makeText(this, "La permission de stockage a été refusée", Toast.LENGTH_LONG).show()
            }
        }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.baseline_add_a_photo_24)
                        .into(binding.uploadedImageView)
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Erreur lors du chargement de l'image: $e")
                }
            } else {
                Toast.makeText(this, "Annulé !", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data
                Log.d(ContentValues.TAG, "URI de l'image sélectionnée: $imageUri")
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.baseline_add_a_photo_24)
                        .into(binding.uploadedImageView)
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Erreur lors du chargement de l'image: $e")
                }
            }
        }

    // Upload Post to Firebase Realtime Database
    private fun uploadPost(title: String, description: String, location: String) {
        if (imageUri != null) {
            // Get the image file from the URI
            val imageRef = storageReference.child("post_images/${System.currentTimeMillis()}")
            val uploadTask = imageRef.putFile(imageUri!!)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Get the image download URL
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    // Create a post object
                    val postId = database.reference.child("posts").push().key // Generate a unique key for the post
                    val post = hashMapOf(
                        "title" to title,
                        "description" to description,
                        "location" to location,
                        "imageUrl" to uri.toString(),
                        "userId" to "userId", // Replace with actual user ID from Firebase Auth
                        "timestamp" to System.currentTimeMillis()
                    )

                    // Add the post to Realtime Database
                    database.reference.child("posts").child(postId!!).setValue(post) // Use the generated key
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Post uploaded successfully with ID: $postId")
                            Toast.makeText(this, "Publication ajoutée avec succès!", Toast.LENGTH_SHORT).show()
                            // Redirect to the home activity or refresh the post list
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                            // Dismiss the ProgressDialog
                            progressDialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            Log.w(ContentValues.TAG, "Error uploading post", e)
                            Toast.makeText(this, "Erreur lors de l'ajout de la publication.", Toast.LENGTH_SHORT).show()
                            // Dismiss the ProgressDialog
                            progressDialog.dismiss()
                        }
                }
            }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error uploading image", e)
                    Toast.makeText(this, "Erreur lors du téléchargement de l'image.", Toast.LENGTH_SHORT).show()
                    // Dismiss the ProgressDialog
                    progressDialog.dismiss()
                }
        } else {
            // Create a post object without an image
            val postId = database.reference.child("posts").push().key // Generate a unique key
            val post = hashMapOf(
                "title" to title,
                "description" to description,
                "location" to location,
                "imageUrl" to "", // Set imageUrl to an empty string if no image is uploaded
                "userId" to "userId", // Replace with actual user ID from Firebase Auth
                "timestamp" to System.currentTimeMillis()
            )

            // Add the post to Realtime Database
            database.reference.child("posts").child(postId!!).setValue(post) // Use the generated key
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "Post uploaded successfully with ID: $postId")
                    Toast.makeText(this, "Publication ajoutée avec succès!", Toast.LENGTH_SHORT).show()
                    // Redirect to the home activity or refresh the post list
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    // Dismiss the ProgressDialog
                    progressDialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error uploading post", e)
                    Toast.makeText(this, "Erreur lors de l'ajout de la publication.", Toast.LENGTH_SHORT).show()
                    // Dismiss the ProgressDialog
                    progressDialog.dismiss()
                }
        }
    }
}