package com.myfueltracker.app.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.myfueltracker.app.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- DATA CLASSES FOR UI ---
sealed class HistoryItem {
    data class Fuel(val entry: FuelEntry) : HistoryItem()
    data class Service(val log: ServiceLog) : HistoryItem()
}

data class BackupData(
    val vehicles: List<Vehicle>,
    val fuelEntries: List<FuelEntry>,
    val serviceLogs: List<ServiceLog>
)

data class MonthlySummary(val month: String, val fuelTotal: Double, val serviceTotal: Double)

class FuelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FuelRepository
    private val preferenceManager = PreferenceManager(application)
    private val _selectedVehicleId = MutableStateFlow<Int?>(null)

    // --- EDIT SELECTION STATE ---
    private val _selectedFuelEntry = MutableStateFlow<FuelEntry?>(null)
    val selectedFuelEntry = _selectedFuelEntry.asStateFlow()

    private val _selectedServiceLog = MutableStateFlow<ServiceLog?>(null)
    val selectedServiceLog = _selectedServiceLog.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).fuelDao()
        repository = FuelRepository(dao)
    }

    // --- ONBOARDING LOGIC ---
    val isFirstRun: StateFlow<Boolean> = preferenceManager.isFirstRun
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setFirstRunCompleted() = viewModelScope.launch {
        preferenceManager.setFirstRunCompleted()
    }

    // --- SETTINGS & PREFERENCES ---
    val currency: StateFlow<String> = preferenceManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.Eagerly, "$")

    val distanceUnit: StateFlow<String> = preferenceManager.distanceUnit
        .stateIn(viewModelScope, SharingStarted.Eagerly, "km")

    val fuelUnit: StateFlow<String> = preferenceManager.fuelUnit
        .stateIn(viewModelScope, SharingStarted.Eagerly, "L")

    val dateFormat: StateFlow<String> = preferenceManager.dateFormat
        .stateIn(viewModelScope, SharingStarted.Eagerly, "dd/MM/yyyy")

    // --- PROFILE STATE ---
    val userName = preferenceManager.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")
    val userEmail = preferenceManager.userEmail.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val phone = preferenceManager.phoneNumber.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userDob = preferenceManager.userDob.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val profileImageUri = preferenceManager.profileImageUri.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- VEHICLE SELECTION ---
    val allVehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedVehicle: StateFlow<Vehicle?> = combine(allVehicles, _selectedVehicleId) { vehicles, id ->
        vehicles.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedVehicleId: StateFlow<Int?> = _selectedVehicleId.asStateFlow()

    // --- HISTORY LOGIC ---
    val combinedHistory: StateFlow<List<HistoryItem>> = combine(
        repository.allFuelEntries,
        repository.allServiceLogs
    ) { fuel, service ->
        (fuel.map { HistoryItem.Fuel(it) } + service.map { HistoryItem.Service(it) })
            .sortedByDescending { item ->
                when (item) {
                    is HistoryItem.Fuel -> item.entry.dateTimestamp
                    is HistoryItem.Service -> item.log.date
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ANALYTICS & SUMMARIES ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val totalFuelingCost: StateFlow<Double?> = _selectedVehicleId
        .flatMapLatest { id ->
            if (id == null) flowOf(0.0)
            else repository.getTotalFuelingCostForVehicle(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalServiceCost: StateFlow<Double?> = _selectedVehicleId
        .flatMapLatest { id ->
            if (id == null) flowOf(0.0)
            else repository.getTotalServiceCostForVehicle(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlySummaries: StateFlow<List<MonthlySummary>> = combinedHistory.map { items ->
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        items.groupBy { item ->
            val date = when(item) {
                is HistoryItem.Fuel -> item.entry.dateTimestamp
                is HistoryItem.Service -> item.log.date
            }
            sdf.format(Date(date))
        }.map { (monthName, list) ->
            val fuel = list.filterIsInstance<HistoryItem.Fuel>().sumOf { it.entry.fuelAmount * it.entry.pricePerUnit }
            val service = list.filterIsInstance<HistoryItem.Service>().sumOf { it.log.cost }
            MonthlySummary(monthName, fuel, service)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMileage: StateFlow<Double?> = combine(
        repository.allFuelEntries,
        _selectedVehicleId
    ) { entries, id ->
        val vehicleEntries = entries.filter { it.vehicleId == id }
        if (vehicleEntries.size < 2) 0.0
        else {
            val maxOdo = vehicleEntries.maxOfOrNull { it.odometer } ?: 0.0
            val minOdo = vehicleEntries.minOfOrNull { it.odometer } ?: 0.0
            maxOdo - minOdo
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)


    @OptIn(ExperimentalCoroutinesApi::class)
    val latestFuelEntry: StateFlow<FuelEntry?> = _selectedVehicleId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getLatestFuelEntry(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val latestServiceLog: StateFlow<ServiceLog?> = _selectedVehicleId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getLatestServiceLog(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- CRUD OPERATIONS ---

    // Missing function that was causing the crash
    fun setSelectedVehicle(vehicle: Vehicle?) {
        _selectedVehicleId.value = vehicle?.id
    }

    fun selectVehicle(vehicleId: Int) { _selectedVehicleId.value = vehicleId }

    fun setSelectedFuelEntry(entry: FuelEntry?) {
        _selectedFuelEntry.value = entry
    }

    fun setSelectedServiceLog(log: ServiceLog?) {
        _selectedServiceLog.value = log
    }

    fun addFuelEntry(odo: Double, amt: Double, p: Double, full: Boolean, n: String, d: Long) = viewModelScope.launch {
        repository.insertFuel(FuelEntry(vehicleId = _selectedVehicleId.value ?: 1, odometer = odo, fuelAmount = amt, pricePerUnit = p, isFullTank = full, notes = n, dateTimestamp = d))
    }

    fun updateFuelEntry(id: Int, vId: Int, odo: Double, amt: Double, p: Double, full: Boolean, n: String, d: Long) = viewModelScope.launch {
        repository.updateFuel(FuelEntry(id, vId, odo, amt, p, full, n, d))
    }

    fun addServiceLog(
        type: String,
        odo: Double,
        cost: Double,
        n: String,
        d: Long,
        centerName: String = "",
        location: String = "",
        phone: String = "",
        quality: Int = 4
    ) = viewModelScope.launch {
        repository.insertService(
            ServiceLog(
                vehicleId = _selectedVehicleId.value ?: 1,
                serviceType = type,
                odoReading = odo,
                cost = cost,
                notes = n,
                date = d,
                // ADD THESE NEW FIELDS BELOW:
                centerName = centerName,
                location = location,
                phone = phone,
                quality = quality
            )
        )
    }

    fun updateServiceEntry(
        id: Int,
        vehicleId: Int,
        type: String,
        odo: Double,
        cost: Double,
        n: String,    // Changed from notes to n
        d: Long,      // Changed from date to d
        centerName: String = "",
        location: String = "",
        phone: String = "",
        quality: Int = 4
    ) = viewModelScope.launch {
        repository.updateService(
            ServiceLog(
                id = id,
                vehicleId = vehicleId,
                serviceType = type,
                odoReading = odo,
                cost = cost,
                date = d,
                notes = n,
                centerName = centerName,
                location = location,
                phone = phone,
                quality = quality
            )
        )
    }

    fun deleteHistoryItem(item: HistoryItem) = viewModelScope.launch {
        when(item) {
            is HistoryItem.Fuel -> repository.deleteFuel(item.entry)
            is HistoryItem.Service -> repository.deleteService(item.log)
        }
    }

    fun addVehicle(vehicle: Vehicle) = viewModelScope.launch { repository.insertVehicle(vehicle) }
    fun updateVehicle(vehicle: Vehicle) = viewModelScope.launch { repository.updateVehicle(vehicle) }

    // --- DELETE VEHICLE LOGIC ---
    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)

            // If the deleted vehicle was currently selected, reset to null
            if (_selectedVehicleId.value == vehicle.id) {
                _selectedVehicleId.value = null
            }
        }
    }

    fun getVehicleById(id: Int): Flow<Vehicle?> = repository.getVehicleById(id)
    fun getFuelEntryById(id: Int): Flow<FuelEntry?> = repository.getFuelEntryById(id)
    fun getServiceById(id: Int): Flow<ServiceLog?> = repository.getServiceById(id)

    // --- SETTINGS & PROFILE UPDATES ---
    fun updateUnits(currency: String, distance: String, fuel: String, date: String) = viewModelScope.launch {
        preferenceManager.saveCurrency(currency)
        preferenceManager.saveDistanceUnit(distance)
        preferenceManager.saveFuelUnit(fuel)
        preferenceManager.saveDateFormat(date)
    }

    fun updateUserData(name: String, vehicle: String, email: String, provider: String, phone: String, dob: String = "") = viewModelScope.launch {
        preferenceManager.saveUserData(name, vehicle, email, provider, phone, dob)
    }

    fun saveProfileImage(uri: Uri?) = viewModelScope.launch {
        // Append a timestamp so the URI string is unique every time a new photo is taken
        val uniqueUri = uri?.let { "$it?t=${System.currentTimeMillis()}" }
        preferenceManager.saveProfileImageUri(uniqueUri)
    }

    // --- EXPORT & IMPORT ---
    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val backup = BackupData(
                    vehicles = repository.allVehicles.first(),
                    fuelEntries = repository.allFuelEntries.first(),
                    serviceLogs = repository.allServiceLogs.first()
                )
                val json = Gson().toJson(backup)
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val json = inputStream.bufferedReader().use { it.readText() }
                    val backup = Gson().fromJson(json, BackupData::class.java)
                    backup.vehicles.forEach { repository.insertVehicle(it) }
                    backup.fuelEntries.forEach { repository.insertFuel(it) }
                    backup.serviceLogs.forEach { repository.insertService(it) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
