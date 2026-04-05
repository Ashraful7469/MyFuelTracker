package com.myfueltracker.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelDao {

    // --- VEHICLES ---
    @Query("SELECT * FROM vehicles ORDER BY name ASC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    fun getVehicleById(id: Int): Flow<Vehicle?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)

    // --- FUEL ENTRIES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuel(entry: FuelEntry)

    @Update
    suspend fun updateFuel(entry: FuelEntry)

    @Delete
    suspend fun deleteFuel(entry: FuelEntry)

    @Query("SELECT * FROM fuel_entries ORDER BY dateTimestamp DESC")
    fun getAllFuelEntries(): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vId ORDER BY dateTimestamp DESC")
    fun getFuelEntriesByVehicle(vId: Int): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries WHERE id = :id")
    fun getFuelEntryById(id: Int): Flow<FuelEntry?>

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vId ORDER BY dateTimestamp DESC LIMIT 1")
    fun getLatestFuelEntry(vId: Int): Flow<FuelEntry?>

    // --- SERVICE LOGS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceLog)

    @Update
    suspend fun updateService(service: ServiceLog)

    @Delete
    suspend fun deleteService(service: ServiceLog)

    @Query("SELECT * FROM service_logs ORDER BY date DESC")
    fun getAllServiceLogs(): Flow<List<ServiceLog>>

    @Query("SELECT * FROM service_logs WHERE vehicleId = :vId ORDER BY date DESC")
    fun getServiceLogsByVehicle(vId: Int): Flow<List<ServiceLog>>

    @Query("SELECT * FROM service_logs WHERE id = :id")
    fun getServiceById(id: Int): Flow<ServiceLog?>

    @Query("SELECT * FROM service_logs WHERE vehicleId = :vId ORDER BY date DESC LIMIT 1")
    fun getLatestServiceLog(vId: Int): Flow<ServiceLog?>

    // --- ANALYTICS & SUMMARY (FILTERED BY VEHICLE) ---

    @Transaction
    @Query("SELECT SUM(fuelAmount * pricePerUnit) FROM fuel_entries WHERE vehicleId = :vId")
    fun getTotalFuelingCostForVehicle(vId: Int): Flow<Double?>

    @Transaction
    @Query("SELECT SUM(cost) FROM service_logs WHERE vehicleId = :vId")
    fun getTotalServiceCostForVehicle(vId: Int): Flow<Double?>

    @Query("SELECT (MAX(odometer) - MIN(odometer)) FROM fuel_entries WHERE vehicleId = :vId")
    fun getTotalMileageForVehicle(vId: Int): Flow<Double?>

    @Query("SELECT MAX(odometer) FROM fuel_entries WHERE vehicleId = :vId")
    fun getLatestOdometerForVehicle(vId: Int): Flow<Double?>

    // --- GLOBAL STATS (Optional) ---

    @Query("SELECT SUM(fuelAmount * pricePerUnit) FROM fuel_entries")
    fun getTotalFuelingCost(): Flow<Double?>

    @Query("SELECT SUM(cost) FROM service_logs")
    fun getTotalServiceCost(): Flow<Double?>
}