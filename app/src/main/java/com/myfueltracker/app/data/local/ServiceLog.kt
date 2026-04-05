package com.myfueltracker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_logs")
data class ServiceLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val vehicleId: Int,
    val serviceType: String,
    val odoReading: Double,
    val cost: Double,
    val notes: String?,
    val date: Long,

    // Provider specific fields
    val serviceCenter: String? = "",
    val location: String? = "",
    val contact: String? = "",
    val rating: Int = 4
)
