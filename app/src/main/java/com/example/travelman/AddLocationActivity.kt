package com.example.travelman

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.withContext
import java.util.*

class AddLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etVisitDate: EditText
    private lateinit var spinnerRating: Spinner
    private lateinit var tvCoordinates: TextView
    private lateinit var btnAddPhoto: ImageButton
    private lateinit var photoContainer: LinearLayout
    private lateinit var btnPickLocation: Button
    private lateinit var btnSave: Button
    private lateinit var googleMap: GoogleMap
    private var selectedLatLng: LatLng? = null
    private val photos = mutableListOf<String>()
    private lateinit var db: AppDatabase
    private var tripId: Int = -1

    companion object {
        private const val PICK_LOCATION_REQUEST_CODE = 1
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
        private const val STORAGE_PERMISSION_REQUEST_CODE = 3
        private const val PICK_PHOTO_REQUEST_CODE = 4
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Local"

        etName = findViewById(R.id.etName)
        etDescription = findViewById(R.id.etDescription)
        etVisitDate = findViewById(R.id.etVisitDate)
        spinnerRating = findViewById(R.id.spinnerRating)
        tvCoordinates = findViewById(R.id.tvCoordinates)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        photoContainer = findViewById(R.id.photoContainer)
        btnPickLocation = findViewById(R.id.btnPickLocation)
        btnSave = findViewById(R.id.btnSave)

        db = AppDatabase.getDatabase(this)

        tripId = intent.getIntExtra("TRIP_ID", -1)

        etVisitDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnPickLocation.setOnClickListener {
            val intent = Intent(this, PickLocationActivity::class.java)
            startActivityForResult(intent, PICK_LOCATION_REQUEST_CODE)
        }

        btnAddPhoto.setOnClickListener {
            checkStoragePermissionAndPickPhotos()
        }

        btnSave.setOnClickListener {
            saveLocation()
        }

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.location_ratings,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRating.adapter = adapter

        checkLocationPermission()
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

    private fun checkStoragePermissionAndPickPhotos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ), STORAGE_PERMISSION_REQUEST_CODE)
            } else {
                pickPhotosFromGallery()
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            pickPhotosFromGallery()
        }
    }

    private fun pickPhotosFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_PHOTO_REQUEST_CODE)
    }

    private fun saveLocation() {
        val name = etName.text?.toString() ?: ""
        val description = etDescription.text?.toString() ?: ""
        val date = etVisitDate.text?.toString() ?: ""
        val rating = spinnerRating.selectedItem?.toString()?.toIntOrNull() ?: 0

        val latitude = selectedLatLng?.latitude
        val longitude = selectedLatLng?.longitude

        // Verifica se pelo menos os campos obrigatórios estão preenchidos
        if (name.isNotEmpty() && description.isNotEmpty() && date.isNotEmpty()) {
            val location = LocationEntity(
                tripId = tripId,
                name = name,
                type = "",
                description = description,
                visitDate = date,
                rating = rating,
                latitude = latitude,
                longitude = longitude,
                photos = photos.toList() // Use uma lista vazia se não houver fotos
            )
            saveLocationToDatabase(location)
        } else {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLocationToDatabase(location: LocationEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.locationDao().insert(location)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddLocationActivity, "Local salvo com sucesso", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_PHOTO_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (data.clipData != null) {
                        val clipData = data.clipData
                        for (i in 0 until clipData!!.itemCount) {
                            val imageUri = clipData.getItemAt(i).uri
                            photos.add(imageUri.toString())
                            addThumbnail(imageUri)
                        }
                    } else {
                        data.data?.let { uri ->
                            photos.add(uri.toString())
                            addThumbnail(uri)
                        }
                    }
                    Toast.makeText(this, "Foto(s) adicionada(s)", Toast.LENGTH_SHORT).show()
                }
            }
            PICK_LOCATION_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val latitude = data.getDoubleExtra("LATITUDE", Double.NaN)
                    val longitude = data.getDoubleExtra("LONGITUDE", Double.NaN)
                    if (!latitude.isNaN() && !longitude.isNaN()) {
                        selectedLatLng = LatLng(latitude, longitude)
                        tvCoordinates.text = "Lat: $latitude, Lng: $longitude"
                    } else {
                        Toast.makeText(this, "Localização inválida", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun addThumbnail(uri: Uri) {
        val imageView = ImageView(this).apply {
            setImageURI(uri)
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                setMargins(8, 8, 8, 8)
            }
        }
        photoContainer.addView(imageView)
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
                    Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    pickPhotosFromGallery()
                } else {
                    Toast.makeText(this, "Permissão de armazenamento negada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
