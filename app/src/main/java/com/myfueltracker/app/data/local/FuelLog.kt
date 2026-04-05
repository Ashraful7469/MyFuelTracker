package com.myfueltracker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_logs")
data class FuelLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val odo: Double,
    val fuelAmount: Double,
    val pricePerUnit: Double,
    val fullTank: Boolean,
    val notes: String,
    val date: Long,
    // Add these fields specifically:
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
