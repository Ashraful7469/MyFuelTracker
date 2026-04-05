package com.myfueltracker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val registrationNumber: String,
    val chassisNumber: String = "", // New
    val engineNumber: String = "",    // New
    val brand: String,
    val model: String,
    val vehicleType: String,
    val modelYear: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),
    val color: String = "",
    val fuelCapacity: Double = 0.0,
    val seatingCapacity: Int = 0,
    val wheelSize: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val length: Double = 0.0,
    val width: Double = 0.0,
    val engineCC: Int = 0,
    val cylinders: Int = 0,
    val maxPower: String = "",
    val maxTorque: String = "",
    val loadCapacity: Double = 0.0
)