package com.myfueltracker.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddFuelScreen(
    vehicleId: Int,
    viewModel: FuelViewModel,
    onSaveComplete: () -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: (
        Int, Int, Double, Double, Double, Boolean, String, Long,
        String?, String?, String?, String?, String?, String?, String?, String?, String?
    ) -> Unit
) {
    // Expandable State
    var isStationInfoExpanded by remember { mutableStateOf(false) }

    val editingEntry by viewModel.selectedFuelEntry.collectAsState()

    // --- CORE STATES ---
    var odo by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isFull by remember { mutableStateOf(true) }

    // --- OPTIONAL STATION STATES ---
    var stationName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var stationContact by remember { mutableStateOf("") }

    val fuelTypesList = listOf("Diesel", "Petrol", "Octane", "LPG", "CNG")
    val selectedFuelTypes = remember { mutableStateListOf<String>() }

    var serviceHour by remember { mutableStateOf("Not Sure") }
    var hospitality by remember { mutableStateOf("Moderate") }
    var hasWashroom by remember { mutableStateOf("Not sure") }
    var hasWaiting by remember { mutableStateOf("Not sure") }
    var goodForStop by remember { mutableStateOf("Not sure") }

    // Date State
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

    // Settings
    val currencySymbol by viewModel.currency.collectAsState()
    val distanceUnitLabel by viewModel.distanceUnit.collectAsState()
    val fuelUnitLabel by viewModel.fuelUnit.collectAsState()

    // Rotate icon based on expansion
    val rotationState by animateFloatAsState(targetValue = if (isStationInfoExpanded) 180f else 0f, label = "Rotate")

    // FILL FIELDS IF EDITING
    LaunchedEffect(editingEntry) {
        editingEntry?.let { entry ->
            odo = entry.odometer.toString()
            amount = entry.fuelAmount.toString()
            price = entry.pricePerUnit.toString()
            note = entry.notes
            isFull = entry.isFullTank
            datePickerState.selectedDateMillis = entry.dateTimestamp

            stationName = entry.stationName ?: ""
            location = entry.location ?: ""
            stationContact = entry.contactNumber ?: ""
            serviceHour = entry.serviceHour ?: "Not Sure"
            hospitality = entry.hospitality ?: "Moderate"
            hasWashroom = entry.hasWashroom ?: "Not sure"
            hasWaiting = entry.hasWaiting ?: "Not sure"
            goodForStop = entry.goodForRoadsideStop ?: "Not sure"

            selectedFuelTypes.clear()
            entry.fuelTypes?.split(", ")?.filter { it.isNotBlank() }?.let {
                selectedFuelTypes.addAll(it)
            }

            // If we are editing an entry that has station info, expand it by default
            if (!entry.stationName.isNullOrBlank()) {
                isStationInfoExpanded = true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.setSelectedFuelEntry(null) }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingEntry == null) "Add Refueling" else "Edit Refueling") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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

            SectionHeader("Basic Details")

            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = formatter.format(Date(selectedDate)),
                    onValueChange = {},
                    label = { Text("Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.DateRange, null) },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            OutlinedTextField(
                value = odo,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) odo = it },
                label = { Text("Odometer Reading ($distanceUnitLabel)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                    label = { Text("Amount ($fuelUnitLabel)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) price = it },
                    label = { Text("Price/$fuelUnitLabel ($currencySymbol)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // --- EXPANDABLE STATION INFO SECTION ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isStationInfoExpanded = !isStationInfoExpanded }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Station Info & Quality",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotationState)
                        )
                    }

                    AnimatedVisibility(visible = isStationInfoExpanded) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = stationName,
                                onValueChange = { stationName = it },
                                label = { Text("Station Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Location / Address") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = stationContact,
                                onValueChange = { stationContact = it },
                                label = { Text("Contact Number") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )

                            Text("Available Fuel Types", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            FlowRow(modifier = Modifier.fillMaxWidth()) {
                                fuelTypesList.forEach { type ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                                        Checkbox(
                                            checked = selectedFuelTypes.contains(type),
                                            onCheckedChange = { if (it) selectedFuelTypes.add(type) else selectedFuelTypes.remove(type) }
                                        )
                                        Text(type, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Divider()

                            Text("Facilities & Service", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            RadioGroupSection("Service Hour", listOf("24/7", "Limited", "Not Sure"), serviceHour) { serviceHour = it }
                            RadioGroupSection("Hospitality", listOf("Excellent", "Good", "Moderate", "Poor", "Bad"), hospitality) { hospitality = it }
                            RadioGroupSection("Restroom", listOf("Yes", "No", "Not sure"), hasWashroom) { hasWashroom = it }
                            RadioGroupSection("Waiting Area", listOf("Yes", "No", "Not sure"), hasWaiting) { hasWaiting = it }
                            RadioGroupSection("Good for Stop?", listOf("Yes", "No", "Not sure"), goodForStop) { goodForStop = it }
                        }
                    }
                }
            }

            SectionHeader("Other")
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { isFull = !isFull }
            ) {
                Checkbox(checked = isFull, onCheckedChange = { isFull = it })
                Text("Filled to Full Tank")
            }

            Button(
                onClick = {
                    onSaveClick(
                        vehicleId,
                        editingEntry?.id ?: -1,
                        odo.toDoubleOrNull() ?: 0.0,
                        amount.toDoubleOrNull() ?: 0.0,
                        price.toDoubleOrNull() ?: 0.0,
                        isFull,
                        note,
                        selectedDate,
                        stationName.ifBlank { null },
                        location.ifBlank { null },
                        stationContact.ifBlank { null },
                        selectedFuelTypes.joinToString(", ").ifBlank { null },
                        serviceHour,
                        hospitality,
                        hasWashroom,
                        hasWaiting,
                        goodForStop
                    )
                    onSaveComplete()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 16.dp),
                enabled = odo.isNotEmpty() && amount.isNotEmpty() && price.isNotEmpty()
            ) {
                Text(if (editingEntry == null) "Save Refueling" else "Update Refueling")
            }
        }
    }
}
