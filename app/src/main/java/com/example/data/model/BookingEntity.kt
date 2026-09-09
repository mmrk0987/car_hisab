package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val passengerName: String,
  val passengerPhone: String,
  val pickupLocation: String,
  val dropLocation: String,
  val tripDateMillis: Long,
  val tripDateString: String,
  val tripTimeString: String = "08:00 AM",
  val totalFare: Double,
  val advancePaid: Double = 0.0,
  val dueFare: Double = totalFare - advancePaid,
  val status: String = "CONFIRMED", // "CONFIRMED", "PENDING", "COMPLETED", "CANCELLED"
  val notes: String = "",
  val createdAtMillis: Long = System.currentTimeMillis()
)
