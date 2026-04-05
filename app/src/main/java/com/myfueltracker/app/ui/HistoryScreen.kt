package com.myfueltracker.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myfueltracker.app.data.local.FuelEntry
import com.myfueltracker.app.data.local.ServiceLog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: FuelViewModel, navController: NavController) {
    val historyItems by viewModel.combinedHistory.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val distanceUnit by viewModel.distanceUnit.collectAsState()
    val fuelUnit by viewModel.fuelUnit.collectAsState()

    var itemToDelete by remember { mutableStateOf<HistoryItem?>(null) }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this record? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteHistoryItem(it) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (historyItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No records found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Use itemsIndexed to access the next item for mileage calculation
                itemsIndexed(
                    items = historyItems,
                    key = { _, item ->
                        when(item) {
                            is HistoryItem.Fuel -> "fuel_${item.entry.id}"
                            is HistoryItem.Service -> "service_${item.log.id}"
                        }
                    }
                ) { index, item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                itemToDelete = item
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color by animateColorAsState(
                                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                    MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                                label = "delete_bg"
                            )
                            Box(
                                modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium).background(color).padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    ) {
                        when (item) {
                            is HistoryItem.Fuel -> {
                                // --- MILEAGE CALCULATION ---
                                // Find the previous fueling for the same vehicle
                                val previousFuelEntry = historyItems
                                    .drop(index + 1) // Look at items older than this one
                                    .filterIsInstance<HistoryItem.Fuel>()
                                    .firstOrNull { it.entry.vehicleId == item.entry.vehicleId }

                                val mileageInfo = if (previousFuelEntry != null) {
                                    val dist = item.entry.odometer - previousFuelEntry.entry.odometer
                                    val efficiency = dist / item.entry.fuelAmount
                                    String.format("%.2f %s/%s", efficiency, distanceUnit, fuelUnit)
                                } else {
                                    "Initial Fill"
                                }

                                HistoryItemCard(
                                    modifier = Modifier.clickable {
                                        viewModel.setSelectedFuelEntry(item.entry)
                                        navController.navigate("${Screen.AddFuel.route}/${item.entry.vehicleId}")
                                    },
                                    title = "${item.entry.fuelAmount} $fuelUnit",
                                    subtitle = "Odo: ${item.entry.odometer} $distanceUnit • $mileageInfo",
                                    amount = "$currency${String.format("%.2f", item.entry.fuelAmount * item.entry.pricePerUnit)}",
                                    date = item.entry.dateTimestamp,
                                    icon = Icons.Default.LocalGasStation,
                                    iconColor = Color.Red,
                                    textColor = MaterialTheme.colorScheme.primary,
                                    onDeleteClick = { itemToDelete = item }
                                )
                            }
                            is HistoryItem.Service -> HistoryItemCard(
                                modifier = Modifier.clickable {
                                    viewModel.setSelectedServiceLog(item.log)
                                    navController.navigate("${Screen.AddService.route}/${item.log.vehicleId}")
                                },
                                title = item.log.serviceType,
                                subtitle = "Odometer: ${item.log.odoReading} $distanceUnit",
                                amount = "$currency${String.format("%.2f", item.log.cost)}",
                                date = item.log.date,
                                icon = Icons.Default.Handyman,
                                iconColor = Color(0xFF2E7D32),
                                textColor = MaterialTheme.colorScheme.tertiary,
                                onDeleteClick = { itemToDelete = item }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    amount: String,
    date: Long,
    icon: ImageVector,
    iconColor: Color,
    textColor: Color,
    onDeleteClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(date))

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(text = dateStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.7f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = amount, style = MaterialTheme.typography.titleMedium, color = textColor, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}