package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val dateMillis: Long = System.currentTimeMillis(),
  val dateString: String,
  val place: String,
  val rent: Double,
  val gratuity: Double,
  val maintenanceCost: Double,
  val kmDriven: Double = 0.0,
  val description: String = "",
  val passengerName: String = "",
  val passengerPhone: String = "",
  val income: Double, // rent - gratuity
  val profit: Double  // income - maintenanceCost
)
