package com.example.data.repository

import com.example.data.db.TripDao
import com.example.data.model.TripEntity
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {
  val allTrips: Flow<List<TripEntity>> = tripDao.getAllTrips()

  suspend fun insertTrip(trip: TripEntity): Long {
    return tripDao.insertTrip(trip)
  }

  suspend fun insertTrips(trips: List<TripEntity>): List<Long> {
    return tripDao.insertTrips(trips)
  }

  suspend fun updateTrip(trip: TripEntity) {
    tripDao.updateTrip(trip)
  }

  suspend fun getAllTripsSnapshot(): List<TripEntity> {
    return tripDao.getAllTripsSnapshot()
  }

  suspend fun deleteTrip(trip: TripEntity) {
    tripDao.deleteTrip(trip)
  }

  suspend fun deleteTripById(id: Long) {
    tripDao.deleteTripById(id)
  }

  suspend fun deleteAllTrips() {
    tripDao.deleteAllTrips()
  }

  suspend fun getTripCount(): Int {
    return tripDao.getTripCount()
  }

  suspend fun clearPassengerNamesFromTrips() {
    tripDao.clearPassengerNamesFromTrips()
  }
}
