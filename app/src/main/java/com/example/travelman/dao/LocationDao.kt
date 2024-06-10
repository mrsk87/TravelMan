package com.example.travelman.dao

import androidx.room.*
import com.example.travelman.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long

    @Update
    suspend fun update(location: LocationEntity)

    @Delete
    suspend fun delete(location: LocationEntity)

    @Query("SELECT * FROM locations WHERE tripId = :tripId")
    fun getLocationsForTrip(tripId: Int): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE tripId = :tripId")
    fun getLocationsForTripSync(tripId: Int): List<LocationEntity>
}
