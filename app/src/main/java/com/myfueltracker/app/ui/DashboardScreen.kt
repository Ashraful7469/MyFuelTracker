package com.myfueltracker.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FuelViewModel,
    navController: NavController,
    onOfflineClick: () -> Unit = {}
) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val selectedVehicleId by viewModel.selectedVehicleId.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()

    // Observe flows for latest entries and history
    val latestFuel by viewModel.latestFuelEntry.collectAsState(initial = null)
    val latestService by viewModel.latestServiceLog.collectAsState(initial = null)
    val historyItems by viewModel.combinedHistory.collectAsState()

    val totalFuelingCost by viewModel.totalFuelingCost.collectAsState()
    val totalServiceCost by viewModel.totalServiceCost.collectAsState()
    val totalMileage by viewModel.totalMileage.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val distanceUnit by viewModel.distanceUnit.collectAsState()
    val fuelUnit by viewModel.fuelUnit.collectAsState()

    // --- CALCULATE EFFICIENCY FOR LAST FUEL ---
    val lastEfficiency = remember(historyItems, selectedVehicleId) {
        val fuelLogs = historyItems.filterIsInstance<HistoryItem.Fuel>()
            .filter { it.entry.vehicleId == selectedVehicleId }

        if (fuelLogs.size >= 2) {
            val current = fuelLogs[0].entry
            val previous = fuelLogs[1].entry
            val distanceTraveled = current.odometer - previous.odometer

            if (distanceTraveled > 0) {
                val efficiency = distanceTraveled / current.fuelAmount
                String.format("%.1f", efficiency) // Just the number to save space
            } else null
        } else null
    }

    // --- CALCULATE TOTAL FUEL VOLUME ---
    val totalFuelVolume = remember(historyItems, selectedVehicleId) {
        historyItems.filterIsInstance<HistoryItem.Fuel>()
            .filter { it.entry.vehicleId == selectedVehicleId }
            .sumOf { it.entry.fuelAmount }
    }

    var expanded by remember { mutableStateOf(false) }

    val grandTotal = (totalFuelingCost ?: 0.0) + (totalServiceCost ?: 0.0)
    val safeMileage = totalMileage ?: 0.0
    val costPerUnit = if (safeMileage > 0.0) grandTotal / safeMileage else 0.0

    LaunchedEffect(vehicles) {
        if (selectedVehicleId == null && vehicles.isNotEmpty()) {
            viewModel.selectVehicle(vehicles.first().id)
        }
    }

    Scaffold(
        floatingActionButton = {
            selectedVehicleId?.let { vehicleId ->
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { navController.navigate("${Screen.AddService.route}/$vehicleId") },
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) { Icon(Icons.Default.Handyman, "Add Service") }

                    FloatingActionButton(
                        onClick = { navController.navigate("${Screen.AddFuel.route}/$vehicleId") },
                        containerColor = Color.Red,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) { Icon(Icons.Default.LocalGasStation, "Add Fuel") }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- VEHICLE SELECTOR ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedVehicle?.name ?: "Select Vehicle",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Active Vehicle") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = if (selectedVehicle?.vehicleType == "Motorcycle")
                                Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                            contentDescription = null
                        )
                    }
                )

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    vehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(vehicle.name, fontWeight = FontWeight.Bold)
                                    Text(vehicle.registrationNumber, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                viewModel.selectVehicle(vehicle.id)
                                expanded = false
                            },
                            leadingIcon = { Icon(if (vehicle.vehicleType == "Motorcycle") Icons.Default.TwoWheeler else Icons.Default.DirectionsCar, null) }
                        )
                    }
                    if (vehicles.isNotEmpty()) HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Add New Vehicle", color = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            expanded = false
                            navController.navigate(Screen.AddVehicle.route)
                        },
                        leadingIcon = { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }
            }

            if (vehicles.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No vehicles found", style = MaterialTheme.typography.titleMedium)
                        Text("Please add a vehicle to start tracking.", color = Color.Gray)
                    }
                }
            } else {
                // --- FUEL SUMMARY SECTION ---
                Text("Fuel Summary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        label = "Lifetime Spend",
                        value = "$currency${String.format("%.0f", grandTotal)}",
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        label = "Cost / $distanceUnit",
                        value = "$currency${String.format("%.2f", costPerUnit)}",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Column(
                            modifier = Modifier
                                .background(Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFF3CB51))))
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text("Total Fueling", style = MaterialTheme.typography.labelMedium, color = Color.Black.copy(0.7f))
                            Text("$currency${String.format("%.2f", totalFuelingCost ?: 0.0)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        label = "Total Service",
                        value = "$currency${String.format("%.2f", totalServiceCost ?: 0.0)}",
                        containerColor = Color(0xFFE8F5E9),
                        labelColor = Color(0xFF2E7D32)
                    )
                }

                // --- ENRICHED DISTANCE CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE8DD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Distance Traveled", style = MaterialTheme.typography.labelLarge, color = Color.Black.copy(0.6f))
                                Text("$safeMileage $distanceUnit", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Icon(Icons.Default.Timeline, null, modifier = Modifier.size(32.dp), tint = Color(0xFFE57373).copy(0.6f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Enrichment Row with colored dots
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MetricDotItem(
                                dotColor = Color(0xFFFF9800),
                                value = "${String.format("%.1f", totalFuelVolume)} $fuelUnit"
                            )
                            MetricDotItem(
                                dotColor = Color(0xFF4CAF50),
                                value = "$currency${String.format("%.0f", grandTotal)}"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- QUICK VIEW: RECENT ACTIVITY ---
                Text("Recent Activity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Blue)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickViewCard(
                        modifier = Modifier.weight(1f),
                        title = "Last Fuel",
                        value = latestFuel?.let { "${it.fuelAmount} $fuelUnit" } ?: "No Records",
                        date = latestFuel?.dateTimestamp,
                        efficiency = lastEfficiency?.let { "$it $distanceUnit/$fuelUnit" },
                        icon = Icons.Default.LocalGasStation,
                        color = Color(0xFFFFEBEE),
                        iconColor = Color.Red
                    )
                    QuickViewCard(
                        modifier = Modifier.weight(1f),
                        title = "Last Service",
                        value = latestService?.serviceType ?: "No Records",
                        date = latestService?.date,
                        icon = Icons.Default.Handyman,
                        color = Color(0xFFE8F5E9),
                        iconColor = Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- PIE CHART ---
                DashboardPieChart(
                    fuelCost = totalFuelingCost ?: 0.0,
                    serviceCost = totalServiceCost ?: 0.0,
                    totalMileage = safeMileage,
                    distanceUnit = distanceUnit
                )
            }
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun QuickViewCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    date: Long?,
    efficiency: String? = null,
    icon: ImageVector,
    color: Color,
    iconColor: Color
) {
    val dateStr = date?.let {
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(it))
    } ?: "---"

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(14.dp), tint = iconColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = iconColor)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Side-by-side Layout for Amount and Efficiency
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (efficiency != null) {
                        Text(
                            text = "• $efficiency",
                            style = MaterialTheme.typography.labelSmall,
                            color = iconColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    containerColor: Color,
    labelColor: Color = Color.Unspecified
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = labelColor)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = labelColor)
        }
    }
}

@Composable
fun DashboardPieChart(
    fuelCost: Double,
    serviceCost: Double,
    totalMileage: Double,
    distanceUnit: String
) {
    val totalCost = fuelCost + serviceCost
    val serviceAngle = if (totalCost > 0) (serviceCost / totalCost * 360f).toFloat() else 0f
    val fuelAngle = if (totalCost > 0) (fuelCost / totalCost * 360f).toFloat() else 360f

    val goldColors = listOf(Color(0xFFFFD700), Color(0xFFF3CB51), Color(0xFFD4AF37), Color(0xFFB8860B), Color(0xFFFFD700))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
            Canvas(modifier = Modifier.size(180.dp)) {
                drawArc(
                    color = Color(0xFF4B7DBC),
                    startAngle = -90f,
                    sweepAngle = serviceAngle,
                    useCenter = false,
                    style = Stroke(width = 35.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(colors = goldColors),
                    startAngle = -90f + serviceAngle,
                    sweepAngle = fuelAngle,
                    useCenter = false,
                    style = Stroke(width = 35.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.0f", totalMileage),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Total $distanceUnit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            ChartLegendItem("Fueling", Color(0xFFFFD700))
            ChartLegendItem("Service", Color(0xFF4B7DBC))
        }
    }
}

@Composable
fun MetricDotItem(dotColor: Color, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}