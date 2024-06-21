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

class AddTripActivity : AppCompatActivity() {

    private lateinit var etTripName: EditText
    private lateinit var etCountry: EditText
    private lateinit var etCity: EditText
    private lateinit var etVisitDate: EditText
    private lateinit var etCoordinates: EditText
    private lateinit var btnPickLocation: Button
    private lateinit var btnAddTrip: Button
    private lateinit var db: AppDatabase
    private var selectedLatLng: Pair<Double, Double>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trip)

        etTripName = findViewById(R.id.etTripName)
        etCountry = findViewById(R.id.etCountry)
        etCity = findViewById(R.id.etCity)
        etVisitDate = findViewById(R.id.etVisitDate)
        etCoordinates = findViewById(R.id.etCoordinates)
        btnPickLocation = findViewById(R.id.btnPickLocation)
        btnAddTrip = findViewById(R.id.btnAddTrip)

        db = AppDatabase.getDatabase(this)

        etVisitDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnPickLocation.setOnClickListener {
            val intent = Intent(this, PickLocationActivity::class.java)
            startActivityForResult(intent, PICK_LOCATION_REQUEST_CODE)
        }

        btnAddTrip.setOnClickListener {
            val tripName = etTripName.text.toString()
            val country = etCountry.text.toString()
            val city = etCity.text.toString()
            val visitDate = etVisitDate.text.toString()
            val coordinates = selectedLatLng

            if (tripName.isNotEmpty() && country.isNotEmpty() && city.isNotEmpty() && visitDate.isNotEmpty() && coordinates != null) {
                val trip = TripEntity(
                    name = tripName,
                    country = country,
                    city = city,
                    visitDate = visitDate,
                    latitude = coordinates.first,
                    longitude = coordinates.second
                )
                saveTrip(trip)
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
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

    private fun saveTrip(trip: TripEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.tripDao().insert(trip)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddTripActivity, "Viagem salva com sucesso", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_LOCATION_REQUEST_CODE) {
            val latitude = data?.getDoubleExtra("LATITUDE", 0.0)
            val longitude = data?.getDoubleExtra("LONGITUDE", 0.0)
            if (latitude != null && longitude != null) {
                selectedLatLng = Pair(latitude, longitude)
                etCoordinates.setText("Lat: $latitude, Lng: $longitude")
            }
        }
    }

    companion object {
        private const val PICK_LOCATION_REQUEST_CODE = 1
    }
}
