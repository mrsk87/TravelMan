package com.example.travelman

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditTripActivity : AppCompatActivity() {

    private lateinit var etEditTripName: EditText
    private lateinit var etEditCountry: EditText
    private lateinit var etEditCity: EditText
    private lateinit var etEditDate: EditText
    private lateinit var btnSaveTrip: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_trip)

        etEditTripName = findViewById(R.id.etEditTripName)
        etEditCountry = findViewById(R.id.etEditCountry)
        etEditCity = findViewById(R.id.etEditCity)
        etEditDate = findViewById(R.id.etEditDate)
        btnSaveTrip = findViewById(R.id.btnSaveTrip)

        val tripName = intent.getStringExtra("TRIP_NAME")
        val position = intent.getIntExtra("POSITION", -1)

        etEditTripName.setText(tripName)

        btnSaveTrip.setOnClickListener {
            val newTripName = etEditTripName.text.toString()
            if (newTripName.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("NEW_TRIP_NAME", newTripName)
                resultIntent.putExtra("POSITION", position)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
