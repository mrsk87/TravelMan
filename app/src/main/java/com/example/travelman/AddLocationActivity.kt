package com.example.travelman

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class AddLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var spinnerType: Spinner
    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var spinnerRating: Spinner
    private lateinit var btnAddPhoto: ImageButton
    private lateinit var btnPickLocation: Button
    private lateinit var btnSave: Button
    private lateinit var googleMap: GoogleMap
    private var selectedLatLng: LatLng? = null
    private val photos = mutableListOf<String>() // Simulando com strings de caminho de imagem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Local"

        spinnerType = findViewById(R.id.spinnerType)
        etName = findViewById(R.id.etName)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        spinnerRating = findViewById(R.id.spinnerRating)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnPickLocation = findViewById(R.id.btnPickLocation)
        btnSave = findViewById(R.id.btnSave)

        etDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnPickLocation.setOnClickListener {
            showMapFragment()
        }

        btnAddPhoto.setOnClickListener {
            // Simulando adição de foto
            photos.add("Foto ${photos.size + 1}")
            Toast.makeText(this, "Foto adicionada", Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            val intent = Intent()
            intent.putExtra("TYPE", spinnerType.selectedItem.toString())
            intent.putExtra("NAME", etName.text.toString())
            intent.putExtra("DESCRIPTION", etDescription.text.toString())
            intent.putExtra("DATE", etDate.text.toString())
            intent.putExtra("RATING", spinnerRating.selectedItem.toString())
            intent.putExtra("LATITUDE", selectedLatLng?.latitude)
            intent.putExtra("LONGITUDE", selectedLatLng?.longitude)
            intent.putStringArrayListExtra("PHOTOS", ArrayList(photos))
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        checkLocationPermission()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            etDate.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showMapFragment() {
        val mapFragment = SupportMapFragment.newInstance()
        mapFragment.getMapAsync(this)

        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Permission has already been granted
            initializeMap()
        }
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted, yay!
                    initializeMap()
                } else {
                    // Permission denied, boo!
                    Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true

            googleMap.setOnMyLocationChangeListener { location ->
                if (selectedLatLng == null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }

        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Localização Selecionada"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            selectedLatLng = latLng
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
