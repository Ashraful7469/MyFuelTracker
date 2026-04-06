package com.myfueltracker.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.myfueltracker.app.ui.*
import com.myfueltracker.app.utils.UpdateManager
import com.myfueltracker.app.data.remote.GitHubRelease

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: FuelViewModel = viewModel()

            val isFirstRun by viewModel.isFirstRun.collectAsState(initial = true)
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val context = LocalContext.current

            // Initialize UpdateManager once
            val updateManager = remember { UpdateManager(context) }
            var updateInfo by remember { mutableStateOf<GitHubRelease?>(null) }

            // Check for updates on launch
            LaunchedEffect(Unit) {
                // Get the current version from your app's build config
                val currentVersionTag = "v${packageManager.getPackageInfo(packageName, 0).versionName}"

                val latest = updateManager.checkForUpdates(currentVersionTag)
                if (latest != null) {
                    updateInfo = latest
                }
            }

            // Show the Update Dialog if a new release is found
            if (updateInfo != null) {
                AlertDialog(
                    onDismissRequest = { updateInfo = null },
                    title = { Text("Update Available") },
                    text = {
                        Text("A new version (${updateInfo?.tag_name}) is ready. Would you like to download and install the latest features?")
                    },
                    confirmButton = {
                        Button(onClick = {
                            // Find the APK in the assets list, fallback to HTML URL if not found
                            val downloadUrl = updateInfo?.assets?.find { it.name.endsWith(".apk") }?.browser_download_url
                                ?: updateInfo?.html_url

                            downloadUrl?.let { url ->
                                updateManager.downloadAndInstall(url)
                            }
                            updateInfo = null
                        }) {
                            Text("Download")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { updateInfo = null }) {
                            Text("Later")
                        }
                    }
                )
            }

            // Routes where the bottom navigation bar should be hidden
            val hideBottomBarRoutes = listOf(
                "welcome",
                "offline_reg",
                Screen.EditProfile.route,
                Screen.AddVehicle.route,
                Screen.EditVehicle.route + "/{vehicleId}",
                Screen.AddFuel.route + "/{vehicleId}",
                Screen.AddService.route + "/{vehicleId}"
            )

            Scaffold(
                bottomBar = {
                    val shouldShowBottomBar = currentRoute != null &&
                            currentRoute !in hideBottomBarRoutes &&
                            !isFirstRun

                    if (shouldShowBottomBar) {
                        BottomNavigationBar(navController)
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = if (isFirstRun) "welcome" else Screen.Dashboard.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("welcome") {
                        WelcomeScreen(
                            viewModel = viewModel,
                            onGetStarted = {
                                viewModel.setFirstRunCompleted()
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            },
                            onOfflineClick = {
                                navController.navigate("offline_reg")
                            }
                        )
                    }

                    composable("offline_reg") {
                        OfflineRegistrationScreen(
                            viewModel = viewModel,
                            onRegistrationComplete = {
                                viewModel.setFirstRunCompleted()
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Dashboard.route) {
                        DashboardScreen(viewModel = viewModel, navController = navController)
                    }

                    composable(Screen.MonthlySummary.route) {
                        MonthlySummaryScreen(viewModel = viewModel)
                    }

                    composable(Screen.History.route) {
                        HistoryScreen(viewModel = viewModel, navController = navController)
                    }

                    composable(Screen.UserProfile.route) {
                        UserProfileScreen(
                            viewModel = viewModel,
                            onAddVehicleClick = { navController.navigate(Screen.AddVehicle.route) },
                            onEditVehicleClick = { vId ->
                                navController.navigate(Screen.EditVehicle.route + "/$vId")
                            },
                            onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                            onLogout = {
                                navController.navigate("welcome") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.EditProfile.route) {
                        EditProfileScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() },
                            onSaveComplete = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onLogout = {
                                navController.navigate("welcome") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.AddVehicle.route) {
                        AddVehicleScreen(
                            viewModel = viewModel,
                            onSaveComplete = { navController.popBackStack() },
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Screen.EditVehicle.route + "/{vehicleId}",
                        arguments = listOf(navArgument("vehicleId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val vId = backStackEntry.arguments?.getInt("vehicleId") ?: 0
                        AddVehicleScreen(
                            viewModel = viewModel,
                            vehicleId = vId,
                            onSaveComplete = { navController.popBackStack() },
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Screen.AddFuel.route + "/{vehicleId}",
                        arguments = listOf(navArgument("vehicleId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val vIdFromNav = backStackEntry.arguments?.getInt("vehicleId") ?: 0
                        AddFuelScreen(
                            vehicleId = vIdFromNav,
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() },
                            onSaveComplete = { navController.popBackStack() },
                            onSaveClick = { vehicleId, id, odo, amt, price, full, notes, date,
                                            sName, loc, phone, types, hour, hosp, wash, wait, road ->
                                if (id == -1) {
                                    viewModel.addFuelEntry(odo, amt, price, full, notes, date,
                                        sName, loc, phone, types, hour, hosp, wash, wait, road)
                                } else {
                                    viewModel.updateFuelEntry(id, vehicleId, odo, amt, price, full, notes, date,
                                        sName, loc, phone, types, hour, hosp, wash, wait, road)
                                }
                            }
                        )
                    }

                    composable(
                        route = Screen.AddService.route + "/{vehicleId}",
                        arguments = listOf(navArgument("vehicleId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val vId = backStackEntry.arguments?.getInt("vehicleId") ?: 0
                        AddServiceScreen(
                            vehicleId = vId,
                            viewModel = viewModel,
                            onSaveComplete = { navController.popBackStack() },
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
