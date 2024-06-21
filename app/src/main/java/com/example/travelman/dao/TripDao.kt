package com.example.travelman.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.travelman.entity.TripEntity

@Dao
interface TripDao {
    @Insert
    suspend fun insert(trip: TripEntity): Long

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Int): TripEntity?

    @Query("SELECT * FROM trips")
    suspend fun getAllTrips(): List<TripEntity>

    @Delete
    suspend fun delete(trip: TripEntity)
}
