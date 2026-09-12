package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
  @Query("SELECT * FROM bookings ORDER BY tripDateMillis ASC, id ASC")
  fun getAllBookings(): Flow<List<BookingEntity>>

  @Query("SELECT * FROM bookings ORDER BY tripDateMillis ASC, id ASC")
  suspend fun getAllBookingsSnapshot(): List<BookingEntity>

  @Query("SELECT * FROM bookings WHERE status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY tripDateMillis ASC")
  fun getUpcomingBookings(): Flow<List<BookingEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBooking(booking: BookingEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookings(bookings: List<BookingEntity>): List<Long>

  @Update
  suspend fun updateBooking(booking: BookingEntity)

  @Query("DELETE FROM bookings WHERE id = :id")
  suspend fun deleteBookingById(id: Long)

  @Query("DELETE FROM bookings")
  suspend fun deleteAllBookings()

  @Delete
  suspend fun deleteBooking(booking: BookingEntity)

  @Query("SELECT COUNT(*) FROM bookings")
  suspend fun getBookingCount(): Int

  @Query("DELETE FROM bookings WHERE passengerName IN ('তানভীর আহমেদ', 'ড. মাহমুদুর রহমান', 'মাহফুজুর রহমান')")
  suspend fun deleteDemoBookings()
}
