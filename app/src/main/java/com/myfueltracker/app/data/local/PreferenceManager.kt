package com.myfueltracker.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.myfueltracker.app.ui.BackupData // Ensure this import matches your BackupData location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferenceManager(context: Context) {
    private val settingsDataStore = context.dataStore
    private val gson = Gson()

    companion object {
        private val CURRENCY_KEY = stringPreferencesKey("currency_symbol")
        private val DISTANCE_UNIT_KEY = stringPreferencesKey("distance_unit")
        private val FUEL_UNIT_KEY = stringPreferencesKey("fuel_unit")
        private val DATE_FORMAT_KEY = stringPreferencesKey("date_format")
        private val IS_FIRST_RUN_KEY = booleanPreferencesKey("is_first_run")

        // --- USER PROFILE KEYS ---
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val VEHICLE_NAME_KEY = stringPreferencesKey("vehicle_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val LOGIN_PROVIDER_KEY = stringPreferencesKey("login_provider")
        private val PHONE_NUMBER_KEY = stringPreferencesKey("phone_number")
        private val USER_DOB_KEY = stringPreferencesKey("user_dob")
        private val PROFILE_IMAGE_URI_KEY = stringPreferencesKey("profile_image_uri")
    }

    // A clean, non-generic base flow to avoid type inference errors
    private val safeData: Flow<Preferences> = settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    // --- First Run Logic ---
    val isFirstRun: Flow<Boolean> = safeData.map { it[IS_FIRST_RUN_KEY] ?: true }

    suspend fun setFirstRunCompleted() {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { it[IS_FIRST_RUN_KEY] = false }
        }
    }

    // --- USER PROFILE FLOWS ---
    val userName: Flow<String> = safeData.map { it[USER_NAME_KEY] ?: "User" }
    val vehicleName: Flow<String> = safeData.map { it[VEHICLE_NAME_KEY] ?: "" }
    val userEmail: Flow<String> = safeData.map { it[USER_EMAIL_KEY] ?: "Offline Account" }
    val loginProvider: Flow<String> = safeData.map { it[LOGIN_PROVIDER_KEY] ?: "Local" }
    val phoneNumber: Flow<String> = safeData.map { it[PHONE_NUMBER_KEY] ?: "" }
    val userDob: Flow<String> = safeData.map { it[USER_DOB_KEY] ?: "" }
    val profileImageUri: Flow<String?> = safeData.map { it[PROFILE_IMAGE_URI_KEY] }

    suspend fun saveUserData(
        name: String,
        vehicle: String,
        email: String,
        provider: String,
        phone: String = "",
        dob: String = "",
        imageUri: String? = null
    ) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { preferences ->
                preferences[USER_NAME_KEY] = name
                preferences[VEHICLE_NAME_KEY] = vehicle
                preferences[USER_EMAIL_KEY] = email
                preferences[LOGIN_PROVIDER_KEY] = provider
                preferences[PHONE_NUMBER_KEY] = phone
                preferences[USER_DOB_KEY] = dob
                preferences[PROFILE_IMAGE_URI_KEY] = imageUri ?: ""
            }
        }
    }

    // --- SETTINGS FLOWS ---
    val currencySymbol: Flow<String> = safeData.map { it[CURRENCY_KEY] ?: "$" }
    val distanceUnit: Flow<String> = safeData.map { it[DISTANCE_UNIT_KEY] ?: "km" }
    val fuelUnit: Flow<String> = safeData.map { it[FUEL_UNIT_KEY] ?: "L" }
    val dateFormat: Flow<String> = safeData.map { it[DATE_FORMAT_KEY] ?: "dd/MM/yyyy" }

    // --- INDIVIDUAL SAVE FUNCTIONS ---
    suspend fun saveCurrency(symbol: String) = withContext(Dispatchers.IO) {
        settingsDataStore.edit { it[CURRENCY_KEY] = symbol }
    }

    suspend fun saveDistanceUnit(unit: String) = withContext(Dispatchers.IO) {
        settingsDataStore.edit { it[DISTANCE_UNIT_KEY] = unit }
    }

    suspend fun saveFuelUnit(unit: String) = withContext(Dispatchers.IO) {
        settingsDataStore.edit { it[FUEL_UNIT_KEY] = unit }
    }

    suspend fun saveDateFormat(format: String) = withContext(Dispatchers.IO) {
        settingsDataStore.edit { it[DATE_FORMAT_KEY] = format }
    }

    // FIXED: Using settingsDataStore and wrapping in withContext(Dispatchers.IO)
    suspend fun saveProfileImageUri(uri: String?) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { preferences ->
                preferences[PROFILE_IMAGE_URI_KEY] = uri ?: ""
            }
        }
    }

    suspend fun saveSettings(
        currency: String,
        distanceUnit: String,
        fuelUnit: String,
        dateFormat: String
    ) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { preferences ->
                preferences[CURRENCY_KEY] = currency
                preferences[DISTANCE_UNIT_KEY] = distanceUnit
                preferences[FUEL_UNIT_KEY] = fuelUnit
                preferences[DATE_FORMAT_KEY] = dateFormat
            }
        }
    }

    // --- Export/Import Helpers ---
    fun createBackupJson(backupData: BackupData): String = gson.toJson(backupData)

    fun parseBackupJson(json: String): BackupData? {
        return try { gson.fromJson(json, BackupData::class.java) }
        catch (e: Exception) { null }
    }
}