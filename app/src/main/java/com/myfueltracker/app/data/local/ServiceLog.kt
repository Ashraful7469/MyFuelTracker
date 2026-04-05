package com.myfueltracker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_logs")
data class ServiceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val serviceType: String,
    val odoReading: Double,
    val cost: Double,
    val date: Long,
    val notes: String,
    // Ensure these exist:
    val centerName: String? = null,
    val location: String? = null,
    val phone: String? = null,
    val quality: Int = 4
)
