package com.myfueltracker.app.data.local


import com.myfueltracker.app.data.local.FuelEntry
import com.myfueltracker.app.data.local.ServiceLog
import com.google.gson.Gson

data class BackupData(
    val fuelEntries: List<FuelEntry>,
    val serviceLogs: List<ServiceLog>
)