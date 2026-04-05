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
    val totalCost: Double,
    val date: Long
)