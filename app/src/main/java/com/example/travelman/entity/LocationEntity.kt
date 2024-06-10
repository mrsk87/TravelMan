package com.example.travelman.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firestoreId: String = "",
    val tripId: Int,
    val type: String,
    val name: String,
    val location: String,
    val description: String,
    val date: String,
    val rating: String,
    val photos: List<String>
)
