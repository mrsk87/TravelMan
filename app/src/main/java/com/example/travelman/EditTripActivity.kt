package com.example.travelman

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class EditTripActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var etEditTripName: EditText
    private lateinit var etEditCountry: EditText
    private lateinit var etEditCity: EditText
    private lateinit var etEditDate: EditText
    private lateinit var btnPickLocation: Button
    private lateinit var btnSaveTrip: Button
    private lateinit var googleMap: GoogleMap
    private var selectedLatLng: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_trip)

        etEditTripName = findViewById(R.id.etEditTripName)
        etEditCountry = findViewById(R.id.etEditCountry)
        etEditCity = findViewById(R.id.etEditCity)
        etEditDate = findViewById(R.id.etEditDate)
        btnPickLocation = findViewById(R.id.btnPickLocation)
        btnSaveTrip = findViewById(R.id.btnSaveTrip)

        etEditDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnPickLocation.setOnClickListener {
            showMapFragment()
        }

        val tripName = intent.getStringExtra("TRIP_NAME")
        val position = intent.getIntExtra("POSITION", -1)

        etEditTripName.setText(tripName)

        btnSaveTrip.setOnClickListener {
            val newTripName = etEditTripName.text.toString()
            val country = etEditCountry.text.toString()
            val city = etEditCity.text.toString()
            val visitDate = etEditDate.text.toString()

            if (newTripName.isNotEmpty() && country.isNotEmpty() && city.isNotEmpty() && visitDate.isNotEmpty() && selectedLatLng != null) {
                val resultIntent = Intent()
                resultIntent.putExtra("NEW_TRIP_NAME", newTripName)
                resultIntent.putExtra("POSITION", position)
                resultIntent.putExtra("VISIT_DATE", visitDate)
                resultIntent.putExtra("LATITUDE", selectedLatLng!!.latitude)
                resultIntent.putExtra("LONGITUDE", selectedLatLng!!.longitude)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos e escolha uma localização", Toast.LENGTH_SHORT).show()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            etEditDate.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showMapFragment() {
        val mapFragment = SupportMapFragment.newInstance()
        mapFragment.getMapAsync(this)

        supportFragmentManager.beginTransaction()
            .add(R.id.mapContainer, mapFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Localização Selecionada"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            selectedLatLng = latLng
        }

        // Verificar permissão de localização
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            moveToCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun moveToCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Você está aqui"))
                selectedLatLng = currentLatLng
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.isMyLocationEnabled = true
                    moveToCurrentLocation()
                }
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
