package com.example.travelman.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.travelman.database.Converters

@Entity(tableName = "locations")
@TypeConverters(Converters::class)
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val name: String,
    val type: String?,
    val description: String?,
    val visitDate: String?,
    val rating: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val photos: List<String>? = emptyList() // Use uma lista vazia como padrão
)
