package com.example.apppfm

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.apppfm.databinding.FragmentHomeBinding
import com.example.apppfm.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var locationManager: LocationManager


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
        binding.locationCv.setOnClickListener {
            val intent = Intent(requireActivity(), LocationPickerActivity::class.java)
            locationPickerActivityResultLauncher.launch(intent)
        }


    }

    private val locationPickerActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val latitude = data.getDoubleExtra("latitude", 0.8)
                    val longitude = data.getDoubleExtra("longitude", 8.8)
                    val address = data.getStringExtra("address") ?: ""
                    binding.locationTv.setText(address)
                }
            } else {
            }


        }
}