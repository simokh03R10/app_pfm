package com.example.apppfm

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.apppfm.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class HomeFragment : Fragment(),LocationListener  {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mContext: Context
    private lateinit var locationManager: LocationManager
    private val PERMISSION_REQUEST_CODE = 100
    private var progressDialog: ProgressDialog? = null


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Si l'utilisateur est connecté, appelez la fonction de rafraîchissement du contenu
            refreshContent()
        }

        binding.locationCv.setOnClickListener {
            locationEnabled();
            showProgressDialog()
            getLocation();
        }
    }
    private fun showProgressDialog() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Determination de votre localisation....")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }
    private fun locationEnabled() {

        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!gpsEnabled && !networkEnabled) {
            AlertDialog.Builder(requireContext())
                .setTitle("Enable GPS Service")
                .setMessage("We need your GPS location to show Near Places around you.")
                .setCancelable(false)
                .setPositiveButton("Enable") { paramDialogInterface, paramInt ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        try {
            locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5f, this)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }


    }

    override fun onLocationChanged(location: Location) {
        try {
            progressDialog?.dismiss()
            val aLocale: Locale = Locale.Builder().setLanguage("en").setScript("Latn").setRegion("RS").build()
            val geocoder = Geocoder(requireContext(),aLocale)
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            // Concaténez le nom de la localité et du pays et affichez-le dans le TextView
            val locality = addresses?.get(0)?.locality
            val countryName = addresses?.get(0)?.countryName
            saveLocationToFirebase(locality, countryName)
            binding.locationTv.text = "$locality, $countryName"
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun saveLocationToFirebase(locality: String?, countryName: String?) {
        // Vérifier si l'utilisateur est connecté à Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Référence à l'utilisateur dans la base de données Firebase
            val userRef = FirebaseDatabase.getInstance().reference.child("Utilisateurs").child(currentUser.uid)

            // Créer un objet pour stocker les informations de localisation
            val locationData = HashMap<String, Any>()
            locationData["location_utilisateur"] = "$locality, $countryName"

            // Mettre à jour le champ location_utilisateur pour l'utilisateur actuel dans Firebase
            userRef.updateChildren(locationData)
                .addOnSuccessListener {
                    // Mise à jour réussie
                    Log.d("Firebase", "Localisation mise à jour avec succès dans Firebase.")
                }
                .addOnFailureListener { e ->
                    // Échec de la mise à jour
                    Log.e("Firebase", "Erreur lors de la mise à jour de la localisation dans Firebase : ${e.message}")
                }
        }
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}
    override fun onResume() {
        super.onResume()
        // Vérifier les autorisations de localisation à nouveau si nécessaire
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }

        // Récupérer la localisation depuis Firebase de manière asynchrone
        getLocationFromFirebase { location ->
            // Mettre à jour l'interface utilisateur avec la nouvelle localisation
            binding.locationTv.text = location
        }
    }
    fun refreshContent() {
        // Affichez une boîte de dialogue de chargement ou un indicateur de chargement si nécessaire
        showProgressDialog()
        progressDialog?.dismiss()
        // Récupérez la localisation depuis Firebase
        getLocationFromFirebase { location ->
            // Mettez à jour l'affichage avec la nouvelle localisation

            binding.locationTv.text = location

            // Masquez la boîte de dialogue de chargement ou l'indicateur de chargement une fois le rafraîchissement terminé

        }
    }

    private fun getLocationFromFirebase(callback: (String) -> Unit) {
        // Vérifier si l'utilisateur est connecté à Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Référence à l'utilisateur dans la base de données Firebase
            val userRef = FirebaseDatabase.getInstance().reference.child("Utilisateurs").child(currentUser.uid)

            // Écouter les modifications de la localisation de l'utilisateur dans Firebase
            userRef.child("location_utilisateur").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Récupérer la localisation de l'utilisateur
                    val location = dataSnapshot.value.toString()
                    // Appeler le callback avec la localisation récupérée depuis Firebase
                    callback(location)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // En cas d'annulation
                    Log.e("Firebase", "Erreur lors de la récupération de la localisation depuis Firebase : ${databaseError.message}")
                }
            })
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Si l'utilisateur est connecté, appelez la fonction de rafraîchissement du contenu
            refreshContent()
        }
    }


}


