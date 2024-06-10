package com.example.travelman

import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ViewLocationActivity : AppCompatActivity() {

    private lateinit var spinnerType: Spinner
    private lateinit var etName: EditText
    private lateinit var etLocation: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var spinnerRating: Spinner
    private lateinit var photosContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_location)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Ver Local"

        spinnerType = findViewById(R.id.spinnerType)
        etName = findViewById(R.id.etName)
        etLocation = findViewById(R.id.etLocation)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        spinnerRating = findViewById(R.id.spinnerRating)
        photosContainer = findViewById(R.id.photosContainer)

        // Receber dados passados da atividade anterior
        val type = intent.getStringExtra("TYPE") ?: "Tipo"
        val name = intent.getStringExtra("NAME") ?: "Nome"
        val location = intent.getStringExtra("LOCATION") ?: "Localização"
        val description = intent.getStringExtra("DESCRIPTION") ?: "Descrição"
        val date = intent.getStringExtra("DATE") ?: "Data"
        val rating = intent.getStringExtra("RATING") ?: "Classificação"
        val photos = intent.getStringArrayListExtra("PHOTOS") ?: arrayListOf()

        // Preencher os campos com os dados recebidos
        spinnerType.setSelection((spinnerType.adapter as ArrayAdapter<String>).getPosition(type))
        etName.setText(name)
        etLocation.setText(location)
        etDescription.setText(description)
        etDate.setText(date)
        spinnerRating.setSelection((spinnerRating.adapter as ArrayAdapter<String>).getPosition(rating))

        // Adicionar fotos ao container
        photos.forEach { photo ->
            val imageView = ImageView(this)
            imageView.setImageResource(android.R.drawable.ic_menu_gallery) // Substituir com a lógica real para carregar a imagem
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
