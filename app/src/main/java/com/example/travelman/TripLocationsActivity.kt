package com.example.travelman

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class TripLocationsActivity : AppCompatActivity() {

    private lateinit var tvTripName: TextView
    private lateinit var lvLocations: ListView
    private lateinit var btnAddLocation: ImageButton
    private val locations = arrayListOf("Local 1", "Local 2", "Local 3")
    private lateinit var tripName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_locations)

        tvTripName = findViewById(R.id.tvTripName)
        lvLocations = findViewById(R.id.lvLocations)
        btnAddLocation = findViewById(R.id.btnAddLocation)

        tripName = intent.getStringExtra("TRIP_NAME") ?: "Viagem"
        tvTripName.text = tripName

        val adapter = LocationAdapter()
        lvLocations.adapter = adapter

        lvLocations.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(this, "Clicked: ${locations[position]}", Toast.LENGTH_SHORT).show()
            // Lógica para abrir detalhes do local, se necessário
        }

        btnAddLocation.setOnClickListener {
            // Lógica para adicionar um novo local
            val newLocationName = "Local ${locations.size + 1}"
            locations.add(newLocationName)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Novo local adicionado", Toast.LENGTH_SHORT).show()
        }
    }

    inner class LocationAdapter : ArrayAdapter<String>(this@TripLocationsActivity, R.layout.list_item_location, locations) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.list_item_location, parent, false)

            val tvLocationName = view.findViewById<TextView>(R.id.tvLocationName)
            val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
            val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

            tvLocationName.text = getItem(position)

            btnEdit.setOnClickListener {
                Toast.makeText(this@TripLocationsActivity, "Editar ${getItem(position)}", Toast.LENGTH_SHORT).show()
                // Lógica para editar o local
            }

            btnDelete.setOnClickListener {
                if (position in locations.indices) {
                    locations.removeAt(position)
                    notifyDataSetChanged()
                    Toast.makeText(this@TripLocationsActivity, "Apagar ${getItem(position)}", Toast.LENGTH_SHORT).show()
                }
            }

            return view
        }
    }
}
