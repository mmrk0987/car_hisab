package com.example.data.repository

import com.example.data.db.BookingDao
import com.example.data.model.BookingEntity
import kotlinx.coroutines.flow.Flow

class BookingRepository(private val bookingDao: BookingDao) {
  val allBookings: Flow<List<BookingEntity>> = bookingDao.getAllBookings()
  val upcomingBookings: Flow<List<BookingEntity>> = bookingDao.getUpcomingBookings()

  suspend fun getAllBookingsSnapshot(): List<BookingEntity> = bookingDao.getAllBookingsSnapshot()

  suspend fun insertBooking(booking: BookingEntity): Long = bookingDao.insertBooking(booking)

  suspend fun insertBookings(bookings: List<BookingEntity>): List<Long> =
    bookingDao.insertBookings(bookings)

  suspend fun updateBooking(booking: BookingEntity) = bookingDao.updateBooking(booking)

  suspend fun deleteBookingById(id: Long) = bookingDao.deleteBookingById(id)

  suspend fun deleteBooking(booking: BookingEntity) = bookingDao.deleteBooking(booking)

  suspend fun getBookingCount(): Int = bookingDao.getBookingCount()

  suspend fun deleteDemoBookings() = bookingDao.deleteDemoBookings()
}
