package com.example.travelman.dao

import androidx.room.*
import com.example.travelman.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity): Long

    @Update
    suspend fun update(trip: TripEntity)

    @Delete
    suspend fun delete(trip: TripEntity)

    @Query("SELECT * FROM trips")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips")
    fun getAllTripsSync(): List<TripEntity>

    @Query("SELECT * FROM trips WHERE name = :tripName LIMIT 1")
    fun getTripByName(tripName: String): TripEntity?
}
