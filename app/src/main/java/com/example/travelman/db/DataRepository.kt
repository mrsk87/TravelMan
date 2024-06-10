package com.example.travelman.db

import android.content.Context
import com.example.travelman.dao.TripDao
import com.example.travelman.dao.LocationDao
import com.example.travelman.entity.TripEntity
import com.example.travelman.entity.LocationEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataRepository(context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val tripDao: TripDao = AppDatabase.getDatabase(context).tripDao()
    private val locationDao: LocationDao = AppDatabase.getDatabase(context).locationDao()

    fun getTrips(): Flow<List<TripEntity>> {
        return tripDao.getAllTrips()
    }

    suspend fun syncTrips() {
        withContext(Dispatchers.IO) {
            val trips = tripDao.getAllTripsSync()
            trips.forEach { trip ->
                val tripData = hashMapOf(
                    "name" to trip.name,
                    "country" to trip.country,
                    "city" to trip.city,
                    "password" to trip.password
                )
                db.collection("trips")
                    .add(tripData)
                    .addOnSuccessListener { documentReference ->
                        CoroutineScope(Dispatchers.IO).launch {
                            tripDao.update(trip.copy(firestoreId = documentReference.id))
                        }
                    }
            }
        }
    }

    suspend fun syncLocations() {
        withContext(Dispatchers.IO) {
            val locations = locationDao.getLocationsForTripSync(1) // Example for tripId 1
            locations.forEach { location ->
                val locationData = hashMapOf(
                    "tripId" to location.tripId,
                    "type" to location.type,
                    "name" to location.name,
                    "location" to location.location,
                    "description" to location.description,
                    "date" to location.date,
                    "rating" to location.rating,
                    "photos" to location.photos
                )
                db.collection("locations")
                    .add(locationData)
                    .addOnSuccessListener { documentReference ->
                        CoroutineScope(Dispatchers.IO).launch {
                            locationDao.update(location.copy(firestoreId = documentReference.id))
                        }
                    }
            }
        }
    }

    suspend fun insertTrip(trip: TripEntity) {
        tripDao.insert(trip)
    }

    suspend fun updateTrip(trip: TripEntity) {
        tripDao.update(trip)
    }

    suspend fun deleteTrip(trip: TripEntity) {
        tripDao.delete(trip)
    }

    fun getLocationsForTrip(tripId: Int): Flow<List<LocationEntity>> {
        return locationDao.getLocationsForTrip(tripId)
    }

    suspend fun insertLocation(location: LocationEntity) {
        locationDao.insert(location)
    }

    suspend fun updateLocation(location: LocationEntity) {
        locationDao.update(location)
    }

    suspend fun deleteLocation(location: LocationEntity) {
        locationDao.delete(location)
    }
}
