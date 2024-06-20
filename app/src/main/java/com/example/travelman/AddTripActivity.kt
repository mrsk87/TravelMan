package com.example.travelman

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travelman.R
import com.example.travelman.database.AppDatabase
import com.example.travelman.entity.TripEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTripActivity : AppCompatActivity() {

    private lateinit var etTripName: EditText
    private lateinit var etCountry: EditText
    private lateinit var etCity: EditText
    private lateinit var etVisitDate: EditText
    private lateinit var btnAddTrip: Button

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trip)

        etTripName = findViewById(R.id.etTripName)
        etCountry = findViewById(R.id.etCountry)
        etCity = findViewById(R.id.etCity)
        etVisitDate = findViewById(R.id.etVisitDate)
        btnAddTrip = findViewById(R.id.btnAddTrip)

        db = AppDatabase.getDatabase(this)

        btnAddTrip.setOnClickListener {
            val tripName = etTripName.text.toString()
            val country = etCountry.text.toString()
            val city = etCity.text.toString()
            val visitDate = etVisitDate.text.toString()

            if (tripName.isNotEmpty() && country.isNotEmpty() && city.isNotEmpty() && visitDate.isNotEmpty()) {
                val trip = TripEntity(name = tripName, country = country, city = city, visitDate = visitDate)
                saveTrip(trip)
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTrip(trip: TripEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.tripDao().insert(trip)
            runOnUiThread {
                Toast.makeText(this@AddTripActivity, "Viagem salva com sucesso", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
