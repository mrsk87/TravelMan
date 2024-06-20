package com.example.travelman

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PickLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var btnConfirmLocation: Button
    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_location)

        btnConfirmLocation = findViewById(R.id.btnConfirmLocation)
        btnConfirmLocation.setOnClickListener {
            selectedLatLng?.let {
                val intent = Intent().apply {
                    putExtra("LATITUDE", it.latitude)
                    putExtra("LONGITUDE", it.longitude)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        googleMap.setOnMyLocationChangeListener { location ->
            if (selectedLatLng == null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }

        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Localização Selecionada"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            selectedLatLng = latLng
        }
    }
}
