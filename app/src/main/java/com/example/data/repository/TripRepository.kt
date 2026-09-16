package com.example.data.repository

import android.util.Log
import com.example.data.db.TripDao
import com.example.data.model.TripEntity
import kotlinx.coroutines.flow.Flow

class TripRepository(private var tripDao: TripDao) {
  val allTrips: Flow<List<TripEntity>> get() = tripDao.getAllTrips()

  fun updateDao(newDao: TripDao) {
    this.tripDao = newDao
  }

  /**
   * Validate trip mandatory fields before database insertion
   * @return null if valid, or error message String if invalid
   */
  fun validateTrip(trip: TripEntity): String? {
    if (trip.dateString.trim().isBlank()) {
      return "ট্রিপের তারিখ প্রদান করা আবশ্যক।"
    }
    if (trip.dateMillis <= 0) {
      return "অকার্যকর ট্রিপ তারিখ চিহ্নিত হয়েছে।"
    }
    if (trip.place.trim().isBlank()) {
      return "গন্তব্য বা রুটের নাম প্রদান করা আবশ্যক।"
    }
    if (trip.userId.trim().isBlank()) {
      return "ইউজার আইডি (user_id) সংযুক্ত করা হয়নি। RLS পলিসি ব্যর্থ হতে পারে।"
    }
    if (trip.vehicleId.trim().isBlank()) {
      return "গাড়ির আইডি/নম্বর (vehicle_id) সংযুক্ত করা হয়নি।"
    }
    if (trip.rent.isNaN() || trip.rent < 0.0) {
      return "ভাড়ার পরিমাণ সঠিক সংখ্যা হতে হবে।"
    }
    if (trip.gratuity.isNaN() || trip.gratuity < 0.0) {
      return "ট্রিপ খরচ/বখশিস সঠিক সংখ্যা হতে হবে।"
    }
    if (trip.maintenanceCost.isNaN() || trip.maintenanceCost < 0.0) {
      return "মেইনটেন্যান্স খরচ সঠিক সংখ্যা হতে হবে।"
    }
    if (trip.kmDriven.isNaN() || trip.kmDriven < 0.0) {
      return "দূরত্ব/ওডোমিটার সংখ্যা সঠিক হতে হবে।"
    }
    return null
  }

  suspend fun insertTrip(trip: TripEntity): Long {
    val validationError = validateTrip(trip)
    if (validationError != null) {
      Log.e("TripSaveError", "Trip validation failed: $validationError")
      throw IllegalArgumentException(validationError)
    }

    return try {
      tripDao.insertTrip(trip)
    } catch (e: Exception) {
      Log.e("TripSaveError", "Database error inserting trip into Room SQLite: ${e.message}", e)
      throw e
    }
  }

  suspend fun insertTrips(trips: List<TripEntity>): List<Long> {
    for (trip in trips) {
      val validationError = validateTrip(trip)
      if (validationError != null) {
        Log.e("TripSaveError", "Batch trip validation failed: $validationError")
        throw IllegalArgumentException(validationError)
      }
    }
    return try {
      tripDao.insertTrips(trips)
    } catch (e: Exception) {
      Log.e("TripSaveError", "Database error batch inserting trips into Room SQLite: ${e.message}", e)
      throw e
    }
  }

  suspend fun updateTrip(trip: TripEntity) {
    val validationError = validateTrip(trip)
    if (validationError != null) {
      Log.e("TripSaveError", "Trip update validation failed: $validationError")
      throw IllegalArgumentException(validationError)
    }
    try {
      tripDao.updateTrip(trip)
    } catch (e: Exception) {
      Log.e("TripSaveError", "Database error updating trip in Room SQLite: ${e.message}", e)
      throw e
    }
  }

  suspend fun getAllTripsSnapshot(): List<TripEntity> {
    return tripDao.getAllTripsSnapshot()
  }

  suspend fun deleteTrip(trip: TripEntity) {
    try {
      tripDao.deleteTrip(trip)
    } catch (e: Exception) {
      Log.e("TripSaveError", "Database error deleting trip: ${e.message}", e)
      throw e
    }
  }

  suspend fun deleteTripById(id: Long) {
    try {
      tripDao.deleteTripById(id)
    } catch (e: Exception) {
      Log.e("TripSaveError", "Database error deleting trip by ID: ${e.message}", e)
      throw e
    }
  }

  suspend fun deleteAllTrips() {
    try {
      tripDao.deleteAllTrips()
    } catch (e: Exception) {
      Log.e("TripSaveError", "Database error deleting all trips: ${e.message}", e)
      throw e
    }
  }

  suspend fun getTripCount(): Int {
    return tripDao.getTripCount()
  }

  suspend fun clearPassengerNamesFromTrips() {
    tripDao.clearPassengerNamesFromTrips()
  }
}
