package com.example.travelman

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.travelman.database.AppDatabase
import com.example.travelman.entity.LocationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class EditLocationActivity : AppCompatActivity() {

    private lateinit var spinnerType: Spinner
    private lateinit var etName: EditText
    private lateinit var etLocation: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var spinnerRating: Spinner
    private lateinit var btnAddPhoto: ImageButton
    private lateinit var btnPickLocation: Button
    private lateinit var btnSave: Button
    private lateinit var db: AppDatabase
    private val photos = mutableListOf<String>() // Simulando com strings de caminho de imagem
    private var locationId: Int = -1
    private var selectedLatLng: Pair<Double, Double>? = null

    companion object {
        private const val PICK_LOCATION_REQUEST_CODE = 1
        private const val PICK_PHOTO_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_location)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar Local"

        spinnerType = findViewById(R.id.spinnerType)
        etName = findViewById(R.id.etName)
        etLocation = findViewById(R.id.etLocation)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        spinnerRating = findViewById(R.id.spinnerRating)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnPickLocation = findViewById(R.id.btnPickLocation)
        btnSave = findViewById(R.id.btnSave)

        db = AppDatabase.getDatabase(this)

        // Populate rating spinner with the location_ratings array
        ArrayAdapter.createFromResource(
            this,
            R.array.location_ratings,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRating.adapter = adapter
        }

        // Receber dados passados da atividade anterior
        val type = intent.getStringExtra("TYPE") ?: "Tipo"
        val name = intent.getStringExtra("NAME") ?: "Nome"
        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
        val description = intent.getStringExtra("DESCRIPTION") ?: "Descrição"
        val date = intent.getStringExtra("DATE") ?: "Data"
        val rating = intent.getStringExtra("RATING") ?: "Classificação"
        val receivedPhotos = intent.getStringArrayListExtra("PHOTOS") ?: arrayListOf()
        locationId = intent.getIntExtra("LOCATION_ID", -1)

        selectedLatLng = Pair(latitude, longitude)

        // Preencher os campos com os dados recebidos
        spinnerType.setSelection((spinnerType.adapter as ArrayAdapter<String>).getPosition(type))
        etName.setText(name)
        etLocation.setText("$latitude, $longitude")
        etDescription.setText(description)
        etDate.setText(date)
        spinnerRating.setSelection((spinnerRating.adapter as ArrayAdapter<String>).getPosition(rating))

        photos.addAll(receivedPhotos)

        etDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnPickLocation.setOnClickListener {
            val intent = Intent(this, PickLocationActivity::class.java)
            startActivityForResult(intent, PICK_LOCATION_REQUEST_CODE)
        }

        btnAddPhoto.setOnClickListener {
            pickPhotoFromGallery()
        }

        btnSave.setOnClickListener {
            saveLocation()
        }
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

    private fun pickPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_PHOTO_REQUEST_CODE)
    }

    private fun saveLocation() {
        val type = spinnerType.selectedItem.toString()
        val name = etName.text.toString()
        val description = etDescription.text.toString()
        val date = etDate.text.toString()
        val rating = spinnerRating.selectedItem.toString().toInt()
        val (latitude, longitude) = selectedLatLng ?: Pair(null, null)

        if (latitude != null && longitude != null && name.isNotEmpty() && description.isNotEmpty() && date.isNotEmpty() && rating > 0) {
            val location = LocationEntity(
                id = locationId,
                tripId = -1, // Placeholder, atualize conforme necessário
                name = name,
                type = type,
                description = description,
                visitDate = date,
                rating = rating,
                latitude = latitude,
                longitude = longitude,
                photos = photos
            )
            updateLocation(location)
        } else {
            Toast.makeText(this, "Por favor, preencha todos os campos e escolha uma localização", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocation(location: LocationEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.locationDao().update(location)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditLocationActivity, "Local atualizado com sucesso", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_PHOTO_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImageUri = data.data
                    selectedImageUri?.let {
                        photos.add(it.toString())
                        Toast.makeText(this, "Foto adicionada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            PICK_LOCATION_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val latitude = data.getDoubleExtra("LATITUDE", 0.0)
                    val longitude = data.getDoubleExtra("LONGITUDE", 0.0)
                    selectedLatLng = Pair(latitude, longitude)
                    etLocation.setText("$latitude, $longitude")
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
