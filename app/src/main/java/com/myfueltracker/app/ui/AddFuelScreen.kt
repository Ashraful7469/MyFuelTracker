package com.myfueltracker.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.myfueltracker.app.data.local.FuelEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelScreen(
    vehicleId: Int,
    viewModel: FuelViewModel,
    onSaveComplete: () -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: (Int, Int, Double, Double, Double, Boolean, String, Long) -> Unit
) {
    // 1. Observe the selected entry from the ViewModel (set by HistoryScreen)
    val editingEntry by viewModel.selectedFuelEntry.collectAsState()

    // UI States
    var odo by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isFull by remember { mutableStateOf(true) }

    // Date State
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

    // Settings from ViewModel
    val currencySymbol by viewModel.currency.collectAsState()
    val distanceUnitLabel by viewModel.distanceUnit.collectAsState()
    val fuelUnitLabel by viewModel.fuelUnit.collectAsState()

    // 2. FILL FIELDS IF EDITING
    // This effect runs when the screen opens or editingEntry changes
    LaunchedEffect(editingEntry) {
        editingEntry?.let { entry ->
            odo = entry.odometer.toString()
            amount = entry.fuelAmount.toString()
            price = entry.pricePerUnit.toString()
            note = entry.notes
            isFull = entry.isFullTank
            datePickerState.selectedDateMillis = entry.dateTimestamp
        }
    }

    // 3. CLEANUP: Reset the selection in ViewModel when we leave the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.setSelectedFuelEntry(null)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
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

            // Date Picker Field (Wrapped in Box to allow click)
            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = formatter.format(Date(selectedDate)),
                    onValueChange = {},
                    label = { Text("Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.DateRange, "Select Date") },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Odometer
            OutlinedTextField(
                value = odo,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) odo = it },
                label = { Text("Odometer Reading ($distanceUnitLabel)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Fuel Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                label = { Text("Fuel Amount ($fuelUnitLabel)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Price per Unit
            OutlinedTextField(
                value = price,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) price = it },
                label = { Text("Price per $fuelUnitLabel ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Notes
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Full Tank Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isFull = !isFull },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isFull, onCheckedChange = { isFull = it })
                Text("Filled to Full Tank", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onSaveClick(
                        vehicleId,
                        editingEntry?.id ?: -1, // Use editingEntry ID if it exists
                        odo.toDoubleOrNull() ?: 0.0,
                        amount.toDoubleOrNull() ?: 0.0,
                        price.toDoubleOrNull() ?: 0.0,
                        isFull,
                        note,
                        selectedDate
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = odo.isNotEmpty() && amount.isNotEmpty() && price.isNotEmpty()
            ) {
                Text(
                    text = if (editingEntry == null) "Save Refueling" else "Update Refueling",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}