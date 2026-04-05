package com.myfueltracker.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FuelEntry::class,
        ServiceLog::class,
        Vehicle::class // 1. Register the new Vehicle entity
    ],
    version = 3, // 2. Increment version (from 2 to 3) to trigger the migration
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fuelDao(): FuelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fuel_tracker_db"
                )
                    // This will wipe existing data and recreate the DB with the new 'vehicles' table
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}