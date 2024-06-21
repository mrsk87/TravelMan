package com.example.travelman.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import com.example.travelman.entity.LocationEntity

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: LocationEntity): Long


    @Query("SELECT * FROM locations WHERE tripId = :tripId")
    suspend fun getLocationsByTripId(tripId: Int): List<LocationEntity>

    @Delete
    suspend fun delete(locationEntity: LocationEntity)

    @Query("SELECT * FROM locations WHERE id = :locationId")
    suspend fun getLocationById(locationId: Int): LocationEntity?

    @Update
    suspend fun update(location: LocationEntity)
}
