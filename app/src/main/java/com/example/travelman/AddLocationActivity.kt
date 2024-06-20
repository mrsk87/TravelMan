package com.example.travelman

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.travelman.database.AppDatabase
import com.example.travelman.entity.LocationEntity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val photos = mutableListOf<String>()
    private lateinit var db: AppDatabase
    private var tripId: Int = -1

    @SuppressLint("MissingInflatedId")
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

        db = AppDatabase.getDatabase(this)

        // Get tripId from intent
        tripId = intent.getIntExtra("TRIP_ID", -1)

        etDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnPickLocation.setOnClickListener {
            showMapFragment()
        }

        btnAddPhoto.setOnClickListener {
            checkStoragePermissionAndPickPhoto()
        }

        btnSave.setOnClickListener {
            val type = spinnerType.selectedItem.toString()
            val name = etName.text.toString()
            val description = etDescription.text.toString()
            val date = etDate.text.toString()
            val rating = spinnerRating.selectedItem.toString().toInt()

            if (selectedLatLng != null && name.isNotEmpty() && description.isNotEmpty() && date.isNotEmpty() && rating > 0) {
                val location = LocationEntity(
                    tripId = tripId, // Ensure tripId is properly initialized
                    name = name,
                    type = type,
                    description = description,
                    visitDate = date,
                    rating = rating,
                    latitude = selectedLatLng!!.latitude,
                    longitude = selectedLatLng!!.longitude,
                    photos = photos
                )
                saveLocation(location)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
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
            initializeMap()
        }
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun checkStoragePermissionAndPickPhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            pickPhotoFromGallery()
        }
    }

    private fun pickPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_PHOTO_REQUEST_CODE)
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
                    initializeMap()
                } else {
                    Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    pickPhotoFromGallery()
                } else {
                    Toast.makeText(this, "Permission denied to access storage", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_PHOTO_REQUEST_CODE) {
            data?.data?.let { uri ->
                photos.add(uri.toString())
                Toast.makeText(this, "Foto adicionada", Toast.LENGTH_SHORT).show()
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

    private fun saveLocation(location: LocationEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.locationDao().insert(location)
            runOnUiThread {
                Toast.makeText(this@AddLocationActivity, "Local salvo com sucesso", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val STORAGE_PERMISSION_REQUEST_CODE = 2
        private const val PICK_PHOTO_REQUEST_CODE = 3
    }
}
