package com.example.travelman

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ViewLocationActivity : AppCompatActivity() {

    private lateinit var tvType: TextView
    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvCoordinates: TextView
    private lateinit var btnOpenInMaps: Button
    private lateinit var photosContainer: LinearLayout

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

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

        // Receber dados passados da atividade anterior
        val type = intent.getStringExtra("TYPE") ?: "Tipo"
        val name = intent.getStringExtra("NAME") ?: "Nome"
        val location = intent.getStringExtra("LOCATION") ?: "Localização"
        val description = intent.getStringExtra("DESCRIPTION") ?: "Descrição"
        val date = intent.getStringExtra("DATE") ?: "Data"
        val rating = intent.getStringExtra("RATING") ?: "Classificação"
        val photos = intent.getStringArrayListExtra("PHOTOS") ?: arrayListOf()
        latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        longitude = intent.getDoubleExtra("LONGITUDE", 0.0)

        // Preencher os campos com os dados recebidos
        tvType.text = type
        tvName.text = name
        tvDescription.text = description
        tvDate.text = date
        tvRating.text = rating
        tvCoordinates.text = "$latitude, $longitude"

        btnOpenInMaps.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($name)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        // Adicionar fotos ao container
        photos.forEach { photo ->
            val imageView = ImageView(this)
            Glide.with(this).load(photo).into(imageView)
            imageView.layoutParams = LinearLayout.LayoutParams(
                200,
                200
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            photosContainer.addView(imageView)
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
