package com.example.travelman

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travelman.database.AppDatabase
import com.example.travelman.entity.LocationEntity
import com.example.travelman.entity.TripEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TripLocationsActivity : AppCompatActivity() {

    private lateinit var tvTripName: TextView
    private lateinit var tvCountry: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvVisitDate: TextView
    private lateinit var btnOpenInMaps: Button
    private lateinit var lvLocations: ListView
    private lateinit var btnAddLocation: ImageButton
    private val locations = arrayListOf<LocationEntity>()
    private lateinit var trip: TripEntity
    private lateinit var db: AppDatabase
    private lateinit var adapter: LocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_locations)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Locais da Viagem"

        tvTripName = findViewById(R.id.tvTripName)
        tvCountry = findViewById(R.id.tvCountry)
        tvCity = findViewById(R.id.tvCity)
        tvVisitDate = findViewById(R.id.tvVisitDate)
        btnOpenInMaps = findViewById(R.id.btnOpenInMaps)
        lvLocations = findViewById(R.id.lvLocations)
        btnAddLocation = findViewById(R.id.btnAddLocation)

        db = AppDatabase.getDatabase(this)
        adapter = LocationAdapter()
        lvLocations.adapter = adapter

        val tripId = intent.getIntExtra("TRIP_ID", -1)
        if (tripId != -1) {
            loadTripData(tripId)
        } else {
            Toast.makeText(this, "Erro ao carregar a viagem", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnAddLocation.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            intent.putExtra("TRIP_ID", trip.id)
            startActivityForResult(intent, ADD_LOCATION_REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::trip.isInitialized) {
            loadTripData(trip.id)
        }
    }

    private fun loadTripData(tripId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            trip = db.tripDao().getTripById(tripId)!!
            locations.clear()
            locations.addAll(db.locationDao().getLocationsByTripId(tripId))
            withContext(Dispatchers.Main) {
                updateUI()
            }
        }
    }

    private fun updateUI() {
        tvTripName.text = "Nome: ${trip.name}"
        tvCountry.text = "País: ${trip.country}"
        tvCity.text = "Cidade: ${trip.city}"
        tvVisitDate.text = "Data de Visita: ${trip.visitDate}"

        btnOpenInMaps.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:${trip.latitude},${trip.longitude}?q=${trip.latitude},${trip.longitude}(${trip.name})")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "Google Maps app is not installed.", Toast.LENGTH_SHORT).show()
            }
        }

        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_LOCATION_REQUEST_CODE, EDIT_LOCATION_REQUEST_CODE -> {
                    loadTripData(trip.id)  // Reload trip data to update locations list
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

    inner class LocationAdapter : ArrayAdapter<LocationEntity>(this@TripLocationsActivity, R.layout.list_item_location, locations) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.list_item_location, parent, false)

            val tvLocationName = view.findViewById<TextView>(R.id.tvLocationName)
            val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
            val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)
            val btnView = view.findViewById<ImageButton>(R.id.btnView)

            tvLocationName.text = getItem(position)?.name

            btnEdit.setOnClickListener {
                val intent = Intent(this@TripLocationsActivity, EditLocationActivity::class.java)
                intent.putExtra("LOCATION_ID", getItem(position)?.id)
                startActivityForResult(intent, EDIT_LOCATION_REQUEST_CODE)
            }

            btnDelete.setOnClickListener {
                if (position in locations.indices) {
                    val location = locations[position]
                    CoroutineScope(Dispatchers.IO).launch {
                        db.locationDao().delete(location)
                        withContext(Dispatchers.Main) {
                            locations.removeAt(position)
                            notifyDataSetChanged()
                            Toast.makeText(this@TripLocationsActivity, "Apagado ${location.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            btnView.setOnClickListener {
                val intent = Intent(this@TripLocationsActivity, ViewLocationActivity::class.java)
                intent.putExtra("LOCATION_ID", getItem(position)?.id)
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
