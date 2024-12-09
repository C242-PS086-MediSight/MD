package com.example.medisight.ui.page.maps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.medisight.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.medisight.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(-8.538514, 115.347533)
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.setMinZoomPreference(2f)
        mMap.setMaxZoomPreference(21f)
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true

        mMap.addMarker(
            MarkerOptions().position(defaultLocation).title("Marker at specified location")
        )

        val locations = listOf(
            LatLng(-8.3546995, 114.6216513),
            LatLng(-8.3622305, 114.6487981),
            LatLng(-8.3551462, 114.6221143),
            LatLng(-8.5786598, 115.1829684),
            LatLng(-8.722516, 115.1753431),
            LatLng(-8.6002936, 115.1729521),
            LatLng(-8.1128565, 115.1085487),
            LatLng(-8.1105531, 115.0882372),
            LatLng(-8.1198073, 115.0924781),
            LatLng(-8.5391532, 115.13221),
            LatLng(-8.5306917, 115.089883),
            LatLng(-8.5399496, 115.1275654),
            LatLng(-8.5447867, 115.1296247),
            LatLng(-8.6499936, 115.2339861),
            LatLng(-8.6650585, 115.2070722),
            LatLng(-8.4566039, 115.3521651),
            LatLng(-8.5008465, 115.3497323),
            LatLng(-8.4542872, 115.3534496),
            LatLng(-8.5372626, 115.3225423),
            LatLng(-8.5385225, 115.3475342),
            LatLng(-8.5421215, 115.3262667)
        )

        locations.forEach { location ->
            mMap.addMarker(MarkerOptions().position(location))
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Current Location")
                    )
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }
}