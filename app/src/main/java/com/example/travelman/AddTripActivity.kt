package com.example.travelman

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class AddTripActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var etTripName: EditText
    private lateinit var etCountry: EditText
    private lateinit var etCity: EditText
    private lateinit var etVisitDate: EditText
    private lateinit var btnPickLocation: Button
    private lateinit var btnAddTrip: Button
    private lateinit var googleMap: GoogleMap
    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trip)

        etTripName = findViewById(R.id.etTripName)
        etCountry = findViewById(R.id.etCountry)
        etCity = findViewById(R.id.etCity)
        etVisitDate = findViewById(R.id.etVisitDate)
        btnPickLocation = findViewById(R.id.btnPickLocation)
        btnAddTrip = findViewById(R.id.btnAddTrip)

        etVisitDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnPickLocation.setOnClickListener {
            showMapFragment()
        }

        btnAddTrip.setOnClickListener {
            val tripName = etTripName.text.toString()
            val country = etCountry.text.toString()
            val city = etCity.text.toString()
            val visitDate = etVisitDate.text.toString()

            if (tripName.isNotEmpty() && country.isNotEmpty() && city.isNotEmpty() && visitDate.isNotEmpty() && selectedLatLng != null) {
                val resultIntent = Intent()
                resultIntent.putExtra("TRIP_NAME", tripName)
                resultIntent.putExtra("COUNTRY", country)
                resultIntent.putExtra("CITY", city)
                resultIntent.putExtra("VISIT_DATE", visitDate)
                resultIntent.putExtra("LATITUDE", selectedLatLng!!.latitude)
                resultIntent.putExtra("LONGITUDE", selectedLatLng!!.longitude)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos e escolha uma localização", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            etVisitDate.setText(selectedDate)
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
    }
}
