package com.myfueltracker.app.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FuelViewModel, onLogout: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Get App Metadata
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    val publisherName = "BIZBEBIG" // Replace with your actual name
    val supportEmail = "app@bizbebig.com" // Replace with your actual email

    // --- 1. STATE MAPPING ---
    val currentCurrency by viewModel.currency.collectAsState(initial = "$")
    val currentDistanceUnit by viewModel.distanceUnit.collectAsState(initial = "km")
    val currentFuelUnit by viewModel.fuelUnit.collectAsState(initial = "L")
    val currentDateVariant by viewModel.dateFormat.collectAsState(initial = "dd/MM/yyyy")

    var currency by remember(currentCurrency) { mutableStateOf(currentCurrency) }
    var distanceUnit by remember(currentDistanceUnit) { mutableStateOf(currentDistanceUnit) }
    var fuelUnit by remember(currentFuelUnit) { mutableStateOf(currentFuelUnit) }
    var dateFormat by remember(currentDateVariant) { mutableStateOf(currentDateVariant) }

    var currencyExpanded by remember { mutableStateOf(false) }
    var distanceExpanded by remember { mutableStateOf(false) }
    var fuelExpanded by remember { mutableStateOf(false) }
    var dateExpanded by remember { mutableStateOf(false) }

    val currencyOptions = listOf("$", "€", "£", "¥", "৳", "₹")
    val distanceOptions = listOf("km", "mi")
    val fuelOptions = listOf("L", "Gal")
    val dateOptions = listOf("dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "MMM dd, yyyy")

    // --- 2. EXPORT / IMPORT LOGIC ---
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(context, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importData(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- USER PROFILE HEADER ---
        if (currentUser != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD4F1F9)),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = currentUser.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = currentUser.displayName ?: "User", fontWeight = FontWeight.Bold)
                        Text(text = currentUser.email ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, "Logout", tint = Color.Red)
                    }
                }
            }
        }

        Text("App Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        SettingsDropdown("Currency Symbol", currency, currencyOptions, currencyExpanded, { currencyExpanded = it }, { currency = it })
        SettingsDropdown("Distance Unit", distanceUnit, distanceOptions, distanceExpanded, { distanceExpanded = it }, { distanceUnit = it })
        SettingsDropdown("Fuel Unit", fuelUnit, fuelOptions, fuelExpanded, { fuelExpanded = it }, { fuelUnit = it })
        SettingsDropdown("Date Format", dateFormat, dateOptions, dateExpanded, { dateExpanded = it }, { dateFormat = it })

        Button(
            onClick = {
                viewModel.updateUnits(currency, distanceUnit, fuelUnit, dateFormat)
                Toast.makeText(context, "Settings Saved", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Save Settings")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // --- EXPORT & IMPORT SECTION ---
        Text("Data Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { exportLauncher.launch("MyFuelTracker_Backup.json") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export")
            }

            OutlinedButton(
                onClick = { importLauncher.launch("application/json") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import")
            }
        }

        Text(
            "Note: Exporting creates a JSON file. Use Import to restore this file on another device.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- APP INFO SECTION (The Enrichment) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Version $versionName",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
            Text(
                text = "Published by $publisherName",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$supportEmail")
                    putExtra(Intent.EXTRA_SUBJECT, "Support: MyFuelTracker App")
                }
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Help & Contact Support", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}