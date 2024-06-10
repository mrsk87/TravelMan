package com.example.travelman

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travelman.db.AppDatabase
import com.example.travelman.db.DataRepository
import com.example.travelman.entity.TripEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTripActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etCountry: EditText
    private lateinit var etCity: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trip)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Adicionar Viagem"

        etName = findViewById(R.id.etName)
        etCountry = findViewById(R.id.etCountry)
        etCity = findViewById(R.id.etCity)
        etPassword = findViewById(R.id.etPassword)
        btnSave = findViewById(R.id.btnSave)

        val dataRepository = DataRepository(this)

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val country = etCountry.text.toString()
            val city = etCity.text.toString()
            val password = etPassword.text.toString()

            val tripEntity = TripEntity(name = name, country = country, city = city, password = password)

            CoroutineScope(Dispatchers.IO).launch {
                val id = AppDatabase.getDatabase(this@AddTripActivity).tripDao().insert(tripEntity)
                dataRepository.syncTrips()
                runOnUiThread {
                    Toast.makeText(this@AddTripActivity, "Viagem adicionada com sucesso", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
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
}
