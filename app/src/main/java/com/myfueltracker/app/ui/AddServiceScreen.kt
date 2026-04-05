package com.myfueltracker.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.myfueltracker.app.data.local.Vehicle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: FuelViewModel,
    vehicleId: Int = -1,
    onSaveComplete: () -> Unit, // Standardized for both Add and Edit modes
    onBackClick: () -> Unit      // Specifically for the TopAppBar navigation
) {
    // Identity Info
    var name by remember { mutableStateOf("") }
    var registrationNumber by remember { mutableStateOf("") }
    var chassisNumber by remember { mutableStateOf("") }
    var engineNumber by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("Car") }

    // Technical Fields
    var modelYear by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var fuelCapacity by remember { mutableStateOf("") }
    var seatingCapacity by remember { mutableStateOf("") }
    var wheelSize by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var loadCapacity by remember { mutableStateOf("") }

    // Engine Specs
    var engineCC by remember { mutableStateOf("") }
    var cylinders by remember { mutableStateOf("") }
    var maxPower by remember { mutableStateOf("") }
    var maxTorque by remember { mutableStateOf("") }

    // Date Picker State
    var purchaseDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Observe existing vehicle for Edit mode
    val existingVehicle by if (vehicleId != -1) {
        viewModel.getVehicleById(vehicleId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    LaunchedEffect(existingVehicle) {
        existingVehicle?.let { v ->
            name = v.name
            registrationNumber = v.registrationNumber
            chassisNumber = v.chassisNumber
            engineNumber = v.engineNumber
            brand = v.brand
            model = v.model
            vehicleType = v.vehicleType
            modelYear = v.modelYear
            purchaseDate = v.purchaseDate
            color = v.color
            fuelCapacity = if(v.fuelCapacity > 0.0) v.fuelCapacity.toString() else ""
            seatingCapacity = if(v.seatingCapacity > 0) v.seatingCapacity.toString() else ""
            wheelSize = v.wheelSize
            weight = if(v.weight > 0.0) v.weight.toString() else ""
            height = if(v.height > 0.0) v.height.toString() else ""
            length = if(v.length > 0.0) v.length.toString() else ""
            width = if(v.width > 0.0) v.width.toString() else ""
            engineCC = if(v.engineCC > 0) v.engineCC.toString() else ""
            cylinders = if(v.cylinders > 0) v.cylinders.toString() else ""
            maxPower = v.maxPower
            maxTorque = v.maxTorque
            loadCapacity = if(v.loadCapacity > 0.0) v.loadCapacity.toString() else ""
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = purchaseDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    purchaseDate = datePickerState.selectedDateMillis ?: purchaseDate
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vehicleId == -1) "Add Vehicle" else "Edit Vehicle") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { // FIXED: Now using onBackClick
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text("Vehicle Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf(
                    "Car" to Icons.Default.DirectionsCar,
                    "Motorcycle" to Icons.Default.TwoWheeler,
                    "Truck" to Icons.Default.LocalShipping,
                    "Boat" to Icons.Default.DirectionsBoat
                )

                types.forEach { (type, icon) ->
                    FilterChip(
                        selected = vehicleType == type,
                        onClick = { vehicleType = type },
                        label = { Text(type) },
                        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            SectionHeader("Identity")
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Vehicle Nickname") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = registrationNumber, onValueChange = { registrationNumber = it }, label = { Text("Registration Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = chassisNumber, onValueChange = { chassisNumber = it }, label = { Text("Chassis Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = engineNumber, onValueChange = { engineNumber = it }, label = { Text("Engine Number") }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, modifier = Modifier.weight(1f))
            }

            SectionHeader("Purchase Details")
            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = dateFormatter.format(Date(purchaseDate)),
                    onValueChange = {},
                    label = { Text("Purchase Date") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = modelYear, onValueChange = { modelYear = it }, label = { Text("Model Year") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") }, modifier = Modifier.weight(1f))
            }

            SectionHeader("Engine & Power")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = engineCC, onValueChange = { engineCC = it }, label = { Text("Engine (CC)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = cylinders, onValueChange = { cylinders = it }, label = { Text("Cylinders") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = maxPower, onValueChange = { maxPower = it }, label = { Text("Max Power") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = maxTorque, onValueChange = { maxTorque = it }, label = { Text("Max Torque") }, modifier = Modifier.weight(1f))
            }

            SectionHeader("Capacities")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = fuelCapacity, onValueChange = { fuelCapacity = it }, label = { Text("Fuel (Litre)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = seatingCapacity, onValueChange = { seatingCapacity = it }, label = { Text("Seating") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }

            SectionHeader("Dimensions & Weight")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = wheelSize, onValueChange = { wheelSize = it }, label = { Text("Wheel Size") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = length, onValueChange = { length = it }, label = { Text("Length") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("Width") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = loadCapacity, onValueChange = { loadCapacity = it }, label = { Text("Load Cap.") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val vehicle = Vehicle(
                        id = if (vehicleId == -1) 0 else vehicleId,
                        name = name,
                        registrationNumber = registrationNumber,
                        chassisNumber = chassisNumber,
                        engineNumber = engineNumber,
                        brand = brand,
                        model = model,
                        vehicleType = vehicleType,
                        modelYear = modelYear,
                        purchaseDate = purchaseDate,
                        color = color,
                        fuelCapacity = fuelCapacity.toDoubleOrNull() ?: 0.0,
                        seatingCapacity = seatingCapacity.toIntOrNull() ?: 0,
                        wheelSize = wheelSize,
                        weight = weight.toDoubleOrNull() ?: 0.0,
                        height = height.toDoubleOrNull() ?: 0.0,
                        length = length.toDoubleOrNull() ?: 0.0,
                        width = width.toDoubleOrNull() ?: 0.0,
                        engineCC = engineCC.toIntOrNull() ?: 0,
                        cylinders = cylinders.toIntOrNull() ?: 0,
                        maxPower = maxPower,
                        maxTorque = maxTorque,
                        loadCapacity = loadCapacity.toDoubleOrNull() ?: 0.0
                    )

                    if (vehicleId == -1) viewModel.addVehicle(vehicle)
                    else viewModel.updateVehicle(vehicle)

                    onSaveComplete() // Navigation trigger
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                enabled = name.isNotBlank() && registrationNumber.isNotBlank()
            ) {
                Text(if (vehicleId == -1) "Save Vehicle" else "Update Vehicle")
            }
        }
    }
}

//@Composable
//fun SectionHeader(title: String) {
//    Text(
//        text = title,
//        style = MaterialTheme.typography.labelLarge,
//        color = MaterialTheme.colorScheme.primary,
//        fontWeight = FontWeight.Bold,
//        modifier = Modifier.padding(top = 8.dp)
//    )
//}
