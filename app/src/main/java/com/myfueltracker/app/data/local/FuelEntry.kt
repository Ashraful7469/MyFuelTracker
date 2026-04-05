package com.myfueltracker.app.data.local

// --- CRITICAL IMPORTS ---
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_entries")
data class FuelEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int = 1, // Add this line
    val odometer: Double,
    val fuelAmount: Double,
    val pricePerUnit: Double,
    val isFullTank: Boolean,
    val notes: String,
    val dateTimestamp: Long
)