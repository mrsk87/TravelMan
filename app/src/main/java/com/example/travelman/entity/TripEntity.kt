package com.example.travelman.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val country: String,
    val city: String,
    val visitDate: String,
    val latitude: Double?,
    val longitude: Double?
)
