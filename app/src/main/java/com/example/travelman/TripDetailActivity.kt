package com.example.travelman

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.travelman.database.AppDatabase
import com.example.travelman.entity.TripEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TripDetailActivity : AppCompatActivity() {

    private lateinit var lvTrips: ListView
    private lateinit var btnAddTrip: ImageButton
    private lateinit var tvGreeting: TextView
    private lateinit var db: AppDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var trips = arrayListOf<TripEntity>()
    private lateinit var adapter: TripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Viagens"

        tvGreeting = findViewById(R.id.tvGreeting)
        lvTrips = findViewById(R.id.lvTrips)
        btnAddTrip = findViewById(R.id.btnAddTrip)

        db = AppDatabase.getDatabase(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        user?.let {
            fetchUsername(it.uid)
        }

        adapter = TripAdapter()
        lvTrips.adapter = adapter

        btnAddTrip.setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            startActivityForResult(intent, ADD_TRIP_REQUEST_CODE)
        }

        lvTrips.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, TripLocationsActivity::class.java)
            intent.putExtra("TRIP_ID", trips[position].id)
            startActivity(intent)
        }

        loadTrips()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_trip_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadTrips() {
        CoroutineScope(Dispatchers.IO).launch {
            val tripList = db.tripDao().getAllTrips()
            withContext(Dispatchers.Main) {
                trips.clear()
                trips.addAll(tripList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun fetchUsername(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val document = firestore.collection("Users").document(userId).get().await()
                val username = document.getString("username") ?: "User"
                withContext(Dispatchers.Main) {
                    tvGreeting.text = "Olá $username"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvGreeting.text = "Olá"
                    Toast.makeText(this@TripDetailActivity, "Failed to fetch username", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_TRIP_REQUEST_CODE, EDIT_TRIP_REQUEST_CODE -> loadTrips()
            }
        }
    }

    inner class TripAdapter : ArrayAdapter<TripEntity>(this@TripDetailActivity, R.layout.list_item_trip, trips) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.list_item_trip, parent, false)

            val tvTripName = view.findViewById<TextView>(R.id.tvTripName)
            val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
            val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)
            val btnAddLocation = view.findViewById<ImageButton>(R.id.btnAddLocation)

            val trip = getItem(position)
            tvTripName.text = trip?.name

            btnEdit.setOnClickListener {
                trip?.let {
                    val intent = Intent(this@TripDetailActivity, EditTripActivity::class.java)
                    intent.putExtra("TRIP_ID", it.id)
                    startActivityForResult(intent, EDIT_TRIP_REQUEST_CODE)
                }
            }

            btnDelete.setOnClickListener {
                trip?.let {
                    if (position in trips.indices) {
                        trips.removeAt(position)
                        notifyDataSetChanged()
                        CoroutineScope(Dispatchers.IO).launch {
                            db.tripDao().delete(it)
                        }
                        Toast.makeText(this@TripDetailActivity, "Apagar ${it.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnAddLocation.setOnClickListener {
                trip?.let {
                    val intent = Intent(this@TripDetailActivity, TripLocationsActivity::class.java)
                    intent.putExtra("TRIP_ID", it.id)
                    startActivity(intent)
                }
            }

            return view
        }
    }

    companion object {
        const val ADD_TRIP_REQUEST_CODE = 1
        const val EDIT_TRIP_REQUEST_CODE = 2
    }
}
