package com.example.travelman

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travelman.db.AppDatabase
import com.example.travelman.entity.LocationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TripLocationsActivity : AppCompatActivity() {

    private lateinit var lvLocations: ListView
    private lateinit var btnAddLocation: ImageButton
    private lateinit var tvTripName: TextView
    private var locations = mutableListOf<LocationEntity>()
    private lateinit var locationAdapter: LocationAdapter
    private lateinit var tripName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_locations)

        lvLocations = findViewById(R.id.lvLocations)
        btnAddLocation = findViewById(R.id.btnAddLocation)
        tvTripName = findViewById(R.id.tvTripName)

        tripName = intent.getStringExtra("TRIP_NAME") ?: ""
        tvTripName.text = tripName

        locationAdapter = LocationAdapter()
        lvLocations.adapter = locationAdapter

        btnAddLocation.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            intent.putExtra("TRIP_NAME", tripName)
            startActivityForResult(intent, ADD_LOCATION_REQUEST_CODE)
        }

        // Load locations for the trip from the database
        loadLocations()
    }

    private fun loadLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            val locationDao = AppDatabase.getDatabase(this@TripLocationsActivity).locationDao()
            val tripDao = AppDatabase.getDatabase(this@TripLocationsActivity).tripDao()
            val trip = tripDao.getTripByName(tripName)
            if (trip != null) {
                locations = locationDao.getLocationsForTripSync(trip.id).toMutableList()
                runOnUiThread {
                    locationAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ADD_LOCATION_REQUEST_CODE -> {
                    // Reload locations after adding a new one
                    loadLocations()
                }
            }
        }
    }

    inner class LocationAdapter : ArrayAdapter<LocationEntity>(this@TripLocationsActivity, R.layout.list_item_location, locations) {
        override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
            val view = convertView ?: layoutInflater.inflate(R.layout.list_item_location, parent, false)

            val tvLocationName = view.findViewById<TextView>(R.id.tvLocationName)
            val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
            val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)
            val btnView = view.findViewById<ImageButton>(R.id.btnView)

            val location = getItem(position)
            tvLocationName.text = location?.name

            btnEdit.setOnClickListener {
                location?.let {
                    val intent = Intent(this@TripLocationsActivity, EditLocationActivity::class.java)
                    intent.putExtra("LOCATION_ID", it.id)
                    startActivityForResult(intent, EDIT_LOCATION_REQUEST_CODE)
                }
            }

            btnDelete.setOnClickListener {
                location?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        val locationDao = AppDatabase.getDatabase(this@TripLocationsActivity).locationDao()
                        locationDao.delete(it)
                        runOnUiThread {
                            locations.remove(it)
                            notifyDataSetChanged()
                            Toast.makeText(this@TripLocationsActivity, "Local removido com sucesso", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            btnView.setOnClickListener {
                location?.let {
                    val intent = Intent(this@TripLocationsActivity, ViewLocationActivity::class.java)
                    intent.putExtra("LOCATION_ID", it.id)
                    startActivity(intent)
                }
            }

            return view
        }
    }

    companion object {
        const val ADD_LOCATION_REQUEST_CODE = 1
        const val EDIT_LOCATION_REQUEST_CODE = 2
    }
}
