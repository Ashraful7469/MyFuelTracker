package com.myfueltracker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_logs")
data class ServiceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val serviceType: String, // e.g., Oil Change, Tire Rotation
    val odoReading: Double,
    val cost: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null
)