package com.example.travelman

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddTripActivity : AppCompatActivity() {

    private lateinit var etTripName: EditText
    private lateinit var etCountry: EditText
    private lateinit var etCity: EditText
    private lateinit var etVisitDate: EditText
    private lateinit var btnAddTrip: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trip)

        etTripName = findViewById(R.id.etTripName)
        etCountry = findViewById(R.id.etCountry)
        etCity = findViewById(R.id.etCity)
        etVisitDate = findViewById(R.id.etVisitDate)
        btnAddTrip = findViewById(R.id.btnAddTrip)

        btnAddTrip.setOnClickListener {
            val tripName = etTripName.text.toString()
            val country = etCountry.text.toString()
            val city = etCity.text.toString()
            val visitDate = etVisitDate.text.toString()

            if (tripName.isNotEmpty() && country.isNotEmpty() && city.isNotEmpty() && visitDate.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("TRIP_NAME", tripName)
                resultIntent.putExtra("VISIT_DATE", visitDate)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
