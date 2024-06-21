package com.example.travelman

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travelman.database.AppDatabase
import com.example.travelman.entity.TripEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class EditTripActivity : AppCompatActivity() {

    private lateinit var etEditTripName: EditText
    private lateinit var etEditCountry: EditText
    private lateinit var etEditCity: EditText
    private lateinit var etEditDate: EditText
    private lateinit var etCoordinates: EditText
    private lateinit var btnPickLocation: Button
    private lateinit var btnSaveTrip: Button
    private lateinit var db: AppDatabase
    private var tripId: Int = -1
    private var selectedLatLng: Pair<Double, Double>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_trip)

        etEditTripName = findViewById(R.id.etEditTripName)
        etEditCountry = findViewById(R.id.etEditCountry)
        etEditCity = findViewById(R.id.etEditCity)
        etEditDate = findViewById(R.id.etEditDate)
        etCoordinates = findViewById(R.id.etCoordinates)
        btnPickLocation = findViewById(R.id.btnPickLocation)
        btnSaveTrip = findViewById(R.id.btnSaveTrip)
        db = AppDatabase.getDatabase(this)

        // Get tripId from intent
        tripId = intent.getIntExtra("TRIP_ID", -1)

        // Load trip data
        loadTripData(tripId)

        etEditDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnPickLocation.setOnClickListener {
            val intent = Intent(this, PickLocationActivity::class.java)
            startActivityForResult(intent, PICK_LOCATION_REQUEST_CODE)
        }

        btnSaveTrip.setOnClickListener {
            val newTripName = etEditTripName.text.toString()
            val country = etEditCountry.text.toString()
            val city = etEditCity.text.toString()
            val visitDate = etEditDate.text.toString()
            val (latitude, longitude) = selectedLatLng ?: Pair(null, null)

            if (newTripName.isNotEmpty() && country.isNotEmpty() && city.isNotEmpty() && visitDate.isNotEmpty() && latitude != null && longitude != null) {
                val updatedTrip = TripEntity(
                    id = tripId,
                    name = newTripName,
                    country = country,
                    city = city,
                    visitDate = visitDate,
                    latitude = latitude,
                    longitude = longitude
                )
                updateTrip(updatedTrip)
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos e escolha uma localização", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTripData(tripId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val trip = db.tripDao().getTripById(tripId)
            withContext(Dispatchers.Main) {
                trip?.let {
                    etEditTripName.setText(it.name)
                    etEditCountry.setText(it.country)
                    etEditCity.setText(it.city)
                    etEditDate.setText(it.visitDate)
                    it.latitude?.let { lat ->
                        it.longitude?.let { lng ->
                            selectedLatLng = Pair(lat, lng)
                            etCoordinates.setText("$lat, $lng")
                        }
                    }
                }
            }
        }
    }

    private fun updateTrip(trip: TripEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.tripDao().update(trip)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditTripActivity, "Viagem atualizada com sucesso", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
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
            etEditDate.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_LOCATION_REQUEST_CODE) {
            data?.let {
                val latitude = it.getDoubleExtra("LATITUDE", 0.0)
                val longitude = it.getDoubleExtra("LONGITUDE", 0.0)
                selectedLatLng = Pair(latitude, longitude)
                etCoordinates.setText("$latitude, $longitude")
            }
        }
    }

    companion object {
        private const val PICK_LOCATION_REQUEST_CODE = 1
    }
}
