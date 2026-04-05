package com.myfueltracker.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    // UI States
    var serviceType by remember { mutableStateOf("General Service") }
    var odoInput by remember { mutableStateOf("") }
    var costInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    // Date Picker State
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }
    val dateTimestamp = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(editingService) {
        editingService?.let { service ->
            serviceType = service.serviceType ?: "General Service"
            odoInput = service.odoReading.toString()
            costInput = service.cost.toString()
            notesInput = service.notes ?: ""
            // Update the date picker state with existing date
            datePickerState.selectedDateMillis = service.date
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.setSelectedServiceLog(null) }
    }

    // --- DATE PICKER DIALOG ---
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
            OutlinedTextField(
                value = serviceType,
                onValueChange = { serviceType = it },
                label = { Text("Service Type") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Build, null) },
                placeholder = { Text("Oil Change, Tires, etc.") }
            )

            OutlinedTextField(
                value = odoInput,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) odoInput = it },
                label = { Text("Odometer Reading") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = costInput,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) costInput = it },
                label = { Text("Total Cost") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Notes / Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // --- CLICKABLE DATE FIELD ---
            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = sdf.format(Date(dateTimestamp)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Service Date") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    enabled = false, // Disabling input but clickable Box triggers the dialog
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val odo = odoInput.toDoubleOrNull() ?: 0.0
                    val cost = costInput.toDoubleOrNull() ?: 0.0

                    val currentEditing = editingService
                    if (currentEditing == null) {
                        viewModel.addServiceLog(serviceType, odo, cost, notesInput, dateTimestamp)
                    } else {
                        viewModel.updateServiceEntry(
                            id = currentEditing.id,
                            vehicleId = currentEditing.vehicleId,
                            type = serviceType,
                            odo = odo,
                            cost = cost,
                            date = dateTimestamp,
                            notes = notesInput
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