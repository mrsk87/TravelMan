package com.example.travelman

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class TripDetailActivity : AppCompatActivity() {

    private lateinit var lvTrips: ListView
    private lateinit var btnAddTrip: ImageButton
    private lateinit var tvGreeting: TextView
    private val trips = arrayListOf("Viagem 1", "Viagem 2", "Viagem 3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Viagens"

        tvGreeting = findViewById(R.id.tvGreeting)
        lvTrips = findViewById(R.id.lvTrips)
        btnAddTrip = findViewById(R.id.btnAddTrip)

        tvGreeting.text = "Ola User"

        val adapter = TripAdapter()
        lvTrips.adapter = adapter

        btnAddTrip.setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            startActivityForResult(intent, ADD_TRIP_REQUEST_CODE)
        }

        lvTrips.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, TripLocationsActivity::class.java)
            intent.putExtra("TRIP_NAME", trips[position])
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_TRIP_REQUEST_CODE -> {
                    val tripName = data?.getStringExtra("TRIP_NAME")
                    if (!tripName.isNullOrEmpty()) {
                        trips.add(tripName)
                        (lvTrips.adapter as TripAdapter).notifyDataSetChanged()
                    }
                }
                EDIT_TRIP_REQUEST_CODE -> {
                    val newTripName = data?.getStringExtra("NEW_TRIP_NAME")
                    val position = data?.getIntExtra("POSITION", -1) ?: -1
                    if (!newTripName.isNullOrEmpty() && position in trips.indices) {
                        trips[position] = newTripName
                        (lvTrips.adapter as TripAdapter).notifyDataSetChanged()
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

    inner class TripAdapter : ArrayAdapter<String>(this@TripDetailActivity, R.layout.list_item_trip, trips) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.list_item_trip, parent, false)

            val tvTripName = view.findViewById<TextView>(R.id.tvTripName)
            val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
            val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)
            val btnAddLocation = view.findViewById<ImageButton>(R.id.btnAddLocation)

            tvTripName.text = getItem(position)

            btnEdit.setOnClickListener {
                val intent = Intent(this@TripDetailActivity, EditTripActivity::class.java)
                intent.putExtra("TRIP_NAME", getItem(position))
                intent.putExtra("POSITION", position)
                startActivityForResult(intent, EDIT_TRIP_REQUEST_CODE)
            }

            btnDelete.setOnClickListener {
                if (position in trips.indices) {
                    trips.removeAt(position)
                    notifyDataSetChanged()
                    Toast.makeText(this@TripDetailActivity, "Apagar ${getItem(position)}", Toast.LENGTH_SHORT).show()
                }
            }

            btnAddLocation.setOnClickListener {
                val intent = Intent(this@TripDetailActivity, TripLocationsActivity::class.java)
                intent.putExtra("TRIP_NAME", getItem(position))
                startActivity(intent)
            }

            return view
        }
    }

    companion object {
        const val ADD_TRIP_REQUEST_CODE = 1
        const val EDIT_TRIP_REQUEST_CODE = 2
    }
}
