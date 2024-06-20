package com.example.travelman

import android.Manifest
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class AddLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var spinnerRating: Spinner
    private lateinit var btnAddPhoto: ImageButton
    private lateinit var btnPickLocation: Button
    private lateinit var btnSave: Button
    private lateinit var etLocation: EditText
    private lateinit var googleMap: GoogleMap
    private var selectedLatLng: LatLng? = null
    private val photos = mutableListOf<String>() // Simulando com strings de caminho de imagem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Local"

        etName = findViewById(R.id.etName)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        spinnerRating = findViewById(R.id.spinnerRating)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnPickLocation = findViewById(R.id.btnPickLocation)
        btnSave = findViewById(R.id.btnSave)
        etLocation = findViewById(R.id.etLocation)

        // Setup spinner for rating
        val ratingAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.location_ratings,
            android.R.layout.simple_spinner_item
        )
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRating.adapter = ratingAdapter

        etDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnPickLocation.setOnClickListener {
            val intent = Intent(this, PickLocationActivity::class.java)
            startActivityForResult(intent, PICK_LOCATION_REQUEST_CODE)
        }

        btnAddPhoto.setOnClickListener {
            checkStoragePermissionAndPickPhoto()
        }

        btnSave.setOnClickListener {
            val type = "Tipo" // Just a placeholder
            val name = etName.text.toString()
            val description = etDescription.text.toString()
            val date = etDate.text.toString()
            val rating = spinnerRating.selectedItem?.toString()

            Log.d("AddLocationActivity", "Type: $type, Name: $name, Description: $description, Date: $date, Rating: $rating, Latitude: ${selectedLatLng?.latitude}, Longitude: ${selectedLatLng?.longitude}")

            if (!type.isNullOrEmpty() && name.isNotEmpty() && description.isNotEmpty() && date.isNotEmpty() && !rating.isNullOrEmpty() && selectedLatLng != null) {
                val intent = Intent()
                intent.putExtra("TYPE", type)
                intent.putExtra("NAME", name)
                intent.putExtra("DESCRIPTION", description)
                intent.putExtra("DATE", date)
                intent.putExtra("RATING", rating)
                intent.putExtra("LATITUDE", selectedLatLng?.latitude)
                intent.putExtra("LONGITUDE", selectedLatLng?.longitude)
                intent.putStringArrayListExtra("PHOTOS", ArrayList(photos))
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos e escolha uma localização", Toast.LENGTH_SHORT).show()
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

    private fun checkStoragePermissionAndPickPhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            // Permission has already been granted
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
                    // Permission was granted, yay!
                    initializeMap()
                } else {
                    // Permission denied, boo!
                    Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted, yay!
                    pickPhotoFromGallery()
                } else {
                    // Permission denied, boo!
                    Toast.makeText(this, "Permission denied to access storage", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_PHOTO_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        // Adicione o URI da foto à lista de fotos
                        photos.add(uri.toString())
                        Toast.makeText(this, "Foto adicionada", Toast.LENGTH_SHORT).show()
                    }
                }
                PICK_LOCATION_REQUEST_CODE -> {
                    val latitude = data?.getDoubleExtra("LATITUDE", 0.0) ?: 0.0
                    val longitude = data?.getDoubleExtra("LONGITUDE", 0.0) ?: 0.0
                    selectedLatLng = LatLng(latitude, longitude)
                    etLocation.setText("Lat: $latitude, Lng: $longitude")
                }
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
        private const val STORAGE_PERMISSION_REQUEST_CODE = 2
        private const val PICK_PHOTO_REQUEST_CODE = 3
        private const val PICK_LOCATION_REQUEST_CODE = 4
    }
}
