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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.myfueltracker.app.data.local.ServiceLog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    vehicleId: Int,
    viewModel: FuelViewModel,
    onSaveComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val editingService by viewModel.selectedServiceLog.collectAsState()

    // --- UI STATES ---
    var serviceType by remember { mutableStateOf("") }
    var odoInput by remember { mutableStateOf("") }
    var costInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    var centerName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var serviceQuality by remember { mutableFloatStateOf(4f) }

    // Date Picker State
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }

    // We use a derived state or a local variable to handle the display date
    val dateTimestamp = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // --- POPULATE DATA ON EDIT ---
    LaunchedEffect(editingService) {
        editingService?.let { service ->
            serviceType = service.serviceType ?: ""
            odoInput = service.odoReading.toString()
            costInput = service.cost.toString()
            notesInput = service.notes ?: ""
            datePickerState.selectedDateMillis = service.date

            // Populate the provider-specific fields
            centerName = service.serviceCenter ?: ""
            location = service.location ?: ""
            contactInfo = service.contact ?: ""
            serviceQuality = service.rating.toFloat()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.setSelectedServiceLog(null) }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingService == null) "Add Service Record" else "Edit Service Record") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Date Field
            OutlinedTextField(
                value = sdf.format(Date(dateTimestamp)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Service Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // 2. Service Type
            OutlinedTextField(
                value = serviceType,
                onValueChange = { serviceType = it },
                label = { Text("Service Type") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Handyman, null) },
                placeholder = { Text("e.g. Engine Oil Change") }
            )

            // --- SERVICE PROVIDER SECTION ---
            Text(
                text = "Service Provider",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            OutlinedTextField(
                value = centerName,
                onValueChange = { centerName = it },
                label = { Text("Service Center Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Store, null) }
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location / Address") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, null) }
            )

            OutlinedTextField(
                value = contactInfo,
                onValueChange = { contactInfo = it },
                label = { Text("Contact Number") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Column {
                Text(
                    text = "Service Quality: ${serviceQuality.toInt()}/5",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = serviceQuality,
                    onValueChange = { serviceQuality = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --- COSTS AND ODOMETER ---
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = odoInput,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) odoInput = it },
                    label = { Text("Odometer") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("km") }
                )
                OutlinedTextField(
                    value = costInput,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) costInput = it },
                    label = { Text("Total Cost") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Notes / Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SAVE BUTTON ---
            Button(
                onClick = {
                    val odo = odoInput.toDoubleOrNull() ?: 0.0
                    val cost = costInput.toDoubleOrNull() ?: 0.0

                    if (editingService == null) {
                        viewModel.addServiceLog(
                            type = serviceType,
                            odo = odo,
                            cost = cost,
                            n = notesInput,
                            d = dateTimestamp,
                            center = centerName,
                            loc = location,
                            phone = contactInfo,
                            star = serviceQuality.toInt(),
                            vId = vehicleId // Using the vehicleId passed into the Composable
                        )
                    } else {
                        viewModel.updateServiceEntry(
                            id = editingService!!.id,
                            vehicleId = editingService!!.vehicleId,
                            type = serviceType,
                            odo = odo,
                            cost = cost,
                            n = notesInput,
                            d = dateTimestamp,
                            center = centerName,
                            loc = location,
                            phone = contactInfo,
                            star = serviceQuality.toInt()
                        )
                    }
                    onSaveComplete()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = serviceType.isNotBlank() && odoInput.isNotBlank() && costInput.isNotBlank()
            ) {
                Text(if (editingService == null) "Save Record" else "Update Record")
            }
        }
    }
}
