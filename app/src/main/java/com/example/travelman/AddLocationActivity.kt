package com.example.travelman

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddLocationActivity : AppCompatActivity() {

    private lateinit var spinnerType: Spinner
    private lateinit var etName: EditText
    private lateinit var etLocation: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var spinnerRating: Spinner
    private lateinit var btnAddPhoto: ImageButton
    private lateinit var btnSave: Button
    private val photos = mutableListOf<String>() // Simulando com strings de caminho de imagem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Local"

        spinnerType = findViewById(R.id.spinnerType)
        etName = findViewById(R.id.etName)
        etLocation = findViewById(R.id.etLocation)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        spinnerRating = findViewById(R.id.spinnerRating)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        btnSave = findViewById(R.id.btnSave)

        btnAddPhoto.setOnClickListener {
            // Simulando adição de foto
            photos.add("Foto ${photos.size + 1}")
            Toast.makeText(this, "Foto adicionada", Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            val intent = Intent()
            intent.putExtra("TYPE", spinnerType.selectedItem.toString())
            intent.putExtra("NAME", etName.text.toString())
            intent.putExtra("LOCATION", etLocation.text.toString())
            intent.putExtra("DESCRIPTION", etDescription.text.toString())
            intent.putExtra("DATE", etDate.text.toString())
            intent.putExtra("RATING", spinnerRating.selectedItem.toString())
            intent.putStringArrayListExtra("PHOTOS", ArrayList(photos))
            setResult(Activity.RESULT_OK, intent)
            finish()
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
