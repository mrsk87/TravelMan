package com.example.travelman

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TRIP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val tripName = data?.getStringExtra("TRIP_NAME")
            if (!tripName.isNullOrEmpty()) {
                trips.add(tripName)
                (lvTrips.adapter as TripAdapter).notifyDataSetChanged()
            }
        }
    }

    inner class TripAdapter : ArrayAdapter<String>(this, R.layout.list_item_trip, trips) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.list_item_trip, parent, false)

            val tvTripName = view.findViewById<TextView>(R.id.tvTripName)
            val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
            val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

            tvTripName.text = getItem(position)

            btnEdit.setOnClickListener {
                Toast.makeText(this@TripDetailActivity, "Editar ${getItem(position)}", Toast.LENGTH_SHORT).show()
                // Lógica para editar a viagem
            }

            btnDelete.setOnClickListener {
                trips.removeAt(position)
                notifyDataSetChanged()
                Toast.makeText(this@TripDetailActivity, "Apagar ${getItem(position)}", Toast.LENGTH_SHORT).show()
                // Lógica para apagar a viagem
            }

            return view
        }
    }

    companion object {
        const val ADD_TRIP_REQUEST_CODE = 1
    }
}
