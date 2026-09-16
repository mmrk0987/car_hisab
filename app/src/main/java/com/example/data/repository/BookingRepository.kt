package com.example.data.repository

import com.example.data.db.BookingDao
import com.example.data.model.BookingEntity
import kotlinx.coroutines.flow.Flow

class BookingRepository(private var bookingDao: BookingDao) {
  val allBookings: Flow<List<BookingEntity>> get() = bookingDao.getAllBookings()
  val upcomingBookings: Flow<List<BookingEntity>> get() = bookingDao.getUpcomingBookings()

  fun updateDao(newDao: BookingDao) {
    this.bookingDao = newDao
  }

  suspend fun getAllBookingsSnapshot(): List<BookingEntity> = bookingDao.getAllBookingsSnapshot()

  suspend fun insertBooking(booking: BookingEntity): Long = bookingDao.insertBooking(booking)

  suspend fun insertBookings(bookings: List<BookingEntity>): List<Long> =
    bookingDao.insertBookings(bookings)

  suspend fun updateBooking(booking: BookingEntity) = bookingDao.updateBooking(booking)

  suspend fun deleteBookingById(id: Long) = bookingDao.deleteBookingById(id)

  suspend fun deleteAllBookings() = bookingDao.deleteAllBookings()

  suspend fun deleteBooking(booking: BookingEntity) = bookingDao.deleteBooking(booking)

  suspend fun getBookingCount(): Int = bookingDao.getBookingCount()

  suspend fun deleteDemoBookings() = bookingDao.deleteDemoBookings()
}
