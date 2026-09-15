package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BookingEntity
import com.example.data.model.TripEntity

@Database(entities = [TripEntity::class, BookingEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun tripDao(): TripDao
  abstract fun bookingDao(): BookingDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "car_hisab_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }

    fun closeDatabase() {
      synchronized(this) {
        INSTANCE?.let { db ->
          if (db.isOpen) {
            db.close()
          }
        }
        INSTANCE = null
      }
    }
  }
}
