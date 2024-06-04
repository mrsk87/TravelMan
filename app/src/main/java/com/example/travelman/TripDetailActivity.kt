package com.example.travelman

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TripDetailActivity : AppCompatActivity() {

    private lateinit var lvLocations: ListView
    private lateinit var btnAddLocation: ImageButton
    private lateinit var tvTripName: TextView
    private val locations = arrayListOf("Local 1", "Local 2", "Local 3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_detail)

        tvTripName = findViewById(R.id.tvTripName)
        lvLocations = findViewById(R.id.lvLocations)
        btnAddLocation = findViewById(R.id.btnAddLocation)

        val tripName = intent.getStringExtra("tripName")
        tvTripName.text = tripName

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, locations)
        lvLocations.adapter = adapter

        lvLocations.setOnItemClickListener { _, _, position, _ ->
            // Navegar para a tela de detalhes do local
            val intent = Intent(this, LocationDetailActivity::class.java)
            intent.putExtra("locationName", locations[position])
            startActivity(intent)
        }

        btnAddLocation.setOnClickListener {
            // Lógica para adicionar um novo local
            val newLocationName = "Local ${locations.size + 1}"
            locations.add(newLocationName)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Novo local adicionado", Toast.LENGTH_SHORT).show()
        }
    }
}
