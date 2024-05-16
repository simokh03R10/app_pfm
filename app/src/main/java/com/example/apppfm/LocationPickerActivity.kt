package com.example.apppfm

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.apppfm.databinding.ActivityLocationPickerBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.libraries.places.api.net.PlacesClient

class LocationPickerActivity : AppCompatActivity(),OnMapReadyCallback{

        private lateinit var binding: ActivityLocationPickerBinding

        private companion object {
            //TAG for logs in logcat
            private const val TAG = "LOCATION_PICKER_TAG"
            private const val DEFAULT_ZOOM = 15
        }
        //default zoom for map marker

        private var mMap: GoogleMap? = null

        // Current Place Picker
        private var mPlaceClient: PlacesClient? = null
        private var FusedLocationProviderClient: FusedLocationProviderClient? = null

        // The geographical location where the device is currently located. That is, the last-known location retrieved by the Fused Location Provider. private var mLastKnownLocation: Location? = null
        private var selectedLatitude: Double? = null
        private var selectedLongitude: Double? = null
        private var address =""
        override  fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityLocationPickerBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.donell.visibility = View.GONE
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment mapFragment.getMapAsync(callback: this)
            Places.initialize( applicationContext: this, getString(R.string.my_google_map_api_key))
            mPlaceClient = Places.createClient(context: this)
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient( activity: this)
            val autocompleteSupportMapFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
            val placesList = arrayOf(Place. Field. ID, Place. Field. NAME, Place.Field. ADDRESS, Place. Field.LAT_LNG)
            autocompleteSupportMapFragment.setPlaceFields (listOf(*placesList))

        }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }
}
