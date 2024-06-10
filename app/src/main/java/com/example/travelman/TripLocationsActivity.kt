package com.example.travelman

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Locais da Viagem"

        tvTripName = findViewById(R.id.tvTripName)
        lvLocations = findViewById(R.id.lvLocations)
        btnAddLocation = findViewById(R.id.btnAddLocation)

        tripName = intent.getStringExtra("TRIP_NAME") ?: "Viagem"
        tvTripName.text = tripName

        val adapter = LocationAdapter()
        lvLocations.adapter = adapter

        lvLocations.setOnItemClickListener { _, _, position, _ ->
            // Lógica para abrir detalhes do local, se necessário
        }

        btnAddLocation.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivityForResult(intent, ADD_LOCATION_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_LOCATION_REQUEST_CODE -> {
                    val locationName = data?.getStringExtra("NAME")
                    if (!locationName.isNullOrEmpty()) {
                        locations.add(locationName)
                        (lvLocations.adapter as LocationAdapter).notifyDataSetChanged()
                    }
                }
                EDIT_LOCATION_REQUEST_CODE -> {
                    val locationName = data?.getStringExtra("NAME")
                    val position = data?.getIntExtra("POSITION", -1)
                    if (!locationName.isNullOrEmpty() && position != null && position >= 0) {
                        locations[position] = locationName
                        (lvLocations.adapter as LocationAdapter).notifyDataSetChanged()
                    }
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

    inner class LocationAdapter : ArrayAdapter<String>(this@TripLocationsActivity, R.layout.list_item_location, locations) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.list_item_location, parent, false)

            val tvLocationName = view.findViewById<TextView>(R.id.tvLocationName)
            val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
            val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)
            val btnView = view.findViewById<ImageButton>(R.id.btnView)

            tvLocationName.text = getItem(position)

            btnEdit.setOnClickListener {
                val intent = Intent(this@TripLocationsActivity, EditLocationActivity::class.java)
                intent.putExtra("TYPE", "Tipo") // Substituir por dados reais
                intent.putExtra("NAME", getItem(position))
                intent.putExtra("LOCATION", "Localização") // Substituir por dados reais
                intent.putExtra("DESCRIPTION", "Descrição") // Substituir por dados reais
                intent.putExtra("DATE", "Data") // Substituir por dados reais
                intent.putExtra("RATING", "Classificação") // Substituir por dados reais
                intent.putStringArrayListExtra("PHOTOS", arrayListOf("Foto 1", "Foto 2")) // Substituir por dados reais
                intent.putExtra("POSITION", position)
                startActivityForResult(intent, EDIT_LOCATION_REQUEST_CODE)
            }

            btnDelete.setOnClickListener {
                if (position in locations.indices) {
                    locations.removeAt(position)
                    notifyDataSetChanged()
                    Toast.makeText(this@TripLocationsActivity, "Apagar ${getItem(position)}", Toast.LENGTH_SHORT).show()
                }
            }

            btnView.setOnClickListener {
                val intent = Intent(this@TripLocationsActivity, ViewLocationActivity::class.java)
                intent.putExtra("TYPE", "Tipo") // Substituir por dados reais
                intent.putExtra("NAME", getItem(position))
                intent.putExtra("LOCATION", "Localização") // Substituir por dados reais
                intent.putExtra("DESCRIPTION", "Descrição") // Substituir por dados reais
                intent.putExtra("DATE", "Data") // Substituir por dados reais
                intent.putExtra("RATING", "Classificação") // Substituir por dados reais
                intent.putStringArrayListExtra("PHOTOS", arrayListOf("Foto 1", "Foto 2")) // Substituir por dados reais
                startActivity(intent)
            }

            return view
        }
    }

    companion object {
        const val ADD_LOCATION_REQUEST_CODE = 1
        const val EDIT_LOCATION_REQUEST_CODE = 2
    }
}
