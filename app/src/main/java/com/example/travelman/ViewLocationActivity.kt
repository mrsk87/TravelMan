package com.example.travelman

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.travelman.R
import com.example.travelman.database.AppDatabase
import com.example.travelman.entity.LocationEntity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewLocationActivity : AppCompatActivity() {

    private lateinit var tvType: TextView
    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvCoordinates: TextView
    private lateinit var btnOpenInMaps: Button
    private lateinit var photosContainer: LinearLayout
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_location)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Ver Local"

        tvType = findViewById(R.id.tvType)
        tvName = findViewById(R.id.tvName)
        tvDescription = findViewById(R.id.tvDescription)
        tvDate = findViewById(R.id.tvDate)
        tvRating = findViewById(R.id.tvRating)
        tvCoordinates = findViewById(R.id.tvCoordinates)
        btnOpenInMaps = findViewById(R.id.btnOpenInMaps)
        photosContainer = findViewById(R.id.photosContainer)

        db = AppDatabase.getDatabase(this)

        val locationId = intent.getIntExtra("LOCATION_ID", -1)
        if (locationId != -1) {
            loadLocationData(locationId)
        } else {
            Toast.makeText(this, "Erro ao carregar o local", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadLocationData(locationId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val location = db.locationDao().getLocationById(locationId)
            withContext(Dispatchers.Main) {
                updateUI(location)
            }
        }
    }

    private fun updateUI(location: LocationEntity?) {
        if (location != null) {
            tvType.text = location.type
            tvName.text = location.name
            tvDescription.text = location.description
            tvDate.text = location.visitDate
            tvRating.text = location.rating.toString()
            tvCoordinates.text = "Lat: ${location.latitude}, Lng: ${location.longitude}"

            btnOpenInMaps.setOnClickListener {
                val gmmIntentUri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(${location.name})")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }

            photosContainer.removeAllViews()
            location.photos.forEach { photoUri ->
                val imageView = ImageView(this)
                Picasso.get().load(photoUri).into(imageView)
                photosContainer.addView(imageView)
            }
        } else {
            Toast.makeText(this, "Local não encontrado", Toast.LENGTH_SHORT).show()
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
}
