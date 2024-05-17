package com.example.apppfm


import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import com.example.apppfm.databinding.ActivityLocationPickerBinding

class LocationPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationPickerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    private fun enableUserLocation() {

    }

    private fun setUpMapListeners() {

    }



}









