package com.myfueltracker.app.data.local

import kotlinx.coroutines.flow.Flow

class FuelRepository(private val fuelDao: FuelDao) {

    // --- Vehicle Operations ---
    val allVehicles: Flow<List<Vehicle>> = fuelDao.getAllVehicles()

    fun getVehicleById(id: Int): Flow<Vehicle?> = fuelDao.getVehicleById(id)

    suspend fun insertVehicle(vehicle: Vehicle) = fuelDao.insertVehicle(vehicle)

    suspend fun updateVehicle(vehicle: Vehicle) = fuelDao.updateVehicle(vehicle)

    suspend fun deleteVehicle(vehicle: Vehicle) {
        fuelDao.deleteVehicle(vehicle)
    }

    // --- Fuel Entry Operations ---
    val allFuelEntries: Flow<List<FuelEntry>> = fuelDao.getAllFuelEntries()

    fun getFuelEntryById(id: Int): Flow<FuelEntry?> = fuelDao.getFuelEntryById(id)

    suspend fun insertFuel(fuel: FuelEntry) = fuelDao.insertFuel(fuel)

    suspend fun updateFuel(fuel: FuelEntry) = fuelDao.updateFuel(fuel)

    suspend fun deleteFuel(fuel: FuelEntry) = fuelDao.deleteFuel(fuel)

    // --- Service Log Operations ---
    val allServiceLogs: Flow<List<ServiceLog>> = fuelDao.getAllServiceLogs()

    fun getServiceById(id: Int): Flow<ServiceLog?> = fuelDao.getServiceById(id)

    suspend fun insertService(service: ServiceLog) = fuelDao.insertService(service)

    suspend fun updateService(service: ServiceLog) = fuelDao.updateService(service)

    suspend fun deleteService(service: ServiceLog) = fuelDao.deleteService(service)

    // --- Analytics Helpers ---
    fun getTotalFuelingCostForVehicle(vehicleId: Int): Flow<Double?> =
        fuelDao.getTotalFuelingCostForVehicle(vehicleId)

    fun getTotalServiceCostForVehicle(vehicleId: Int): Flow<Double?> =
        fuelDao.getTotalServiceCostForVehicle(vehicleId)

    fun getLatestFuelEntry(vehicleId: Int): Flow<FuelEntry?> {
        return fuelDao.getLatestFuelEntry(vehicleId)
    }

    fun getLatestServiceLog(vehicleId: Int): Flow<ServiceLog?> {
        return fuelDao.getLatestServiceLog(vehicleId)
    }
}