package com.myfueltracker.app.ui

sealed class Screen(val route: String, val label: String = "") {

    // --- Bottom Navigation Items ---
    object Dashboard : Screen("dashboard", "Home")
    object MonthlySummary : Screen("summary", "Insights")
    object History : Screen("history", "History")
    object UserProfile : Screen("profile", "Account")
    object Settings : Screen("settings", "Settings")

    // --- Action & Secondary Screens (Hidden from Bottom Nav) ---

    // Fuel & Service
    object AddFuel : Screen("add_fuel")
    object AddService : Screen("add_service")

    // Vehicle Management
    object AddVehicle : Screen("add_vehicle")
    // FIXED: Added EditVehicle to resolve "Unresolved reference" in MainActivity
    object EditVehicle : Screen("edit_vehicle")

    // Profile Management
    object EditProfile : Screen("edit_profile")
}