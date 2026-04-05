package com.myfueltracker.app.data.local

// --- CRITICAL IMPORTS ---
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_entries")
data class FuelEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val odometer: Double,
    val fuelAmount: Double,
    val pricePerUnit: Double,
    val isFullTank: Boolean,
    val notes: String,
    val dateTimestamp: Long,

    // --- ADD THESE NEW FIELDS ---
    val stationName: String? = null,
    val location: String? = null,
    val contactNumber: String? = null,
    val fuelTypes: String? = null,
    val serviceHour: String? = null,
    val hospitality: String? = null,
    val hasWashroom: String? = null,
    val hasWaiting: String? = null,
    val goodForRoadsideStop: String? = null
)
