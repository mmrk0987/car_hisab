package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
  @Query("SELECT * FROM trips ORDER BY dateMillis DESC, id DESC")
  fun getAllTrips(): Flow<List<TripEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrip(trip: TripEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrips(trips: List<TripEntity>): List<Long>

  @Update
  suspend fun updateTrip(trip: TripEntity)

  @Query("SELECT * FROM trips ORDER BY dateMillis DESC, id DESC")
  suspend fun getAllTripsSnapshot(): List<TripEntity>

  @Delete
  suspend fun deleteTrip(trip: TripEntity)

  @Query("DELETE FROM trips WHERE id = :id")
  suspend fun deleteTripById(id: Long)

  @Query("DELETE FROM trips")
  suspend fun deleteAllTrips()

  @Query("SELECT COUNT(*) FROM trips")
  suspend fun getTripCount(): Int

  @Query("UPDATE trips SET passengerName = '', passengerPhone = ''")
  suspend fun clearPassengerNamesFromTrips()
}
