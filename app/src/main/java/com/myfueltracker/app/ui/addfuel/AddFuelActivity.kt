package com.myfueltracker.app.ui.addfuel

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.myfueltracker.app.R
import com.myfueltracker.app.data.local.AppDatabase
import com.myfueltracker.app.data.local.FuelEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFuelActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var entryId: Int = -1
    private var vehicleId: Int = -1 // Added to track which vehicle this fuel belongs to

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_fuel)

        // 1. Retrieve BOTH the entryId (for editing) and vehicleId (for new entries)
        entryId = intent.getIntExtra("entryId", -1)
        vehicleId = intent.getIntExtra("vehicleId", -1)

        db = AppDatabase.getDatabase(this)

        val odoInput = findViewById<EditText>(R.id.odo)
        val fuelInput = findViewById<EditText>(R.id.fuel)
        val priceInput = findViewById<EditText>(R.id.price)
        val saveButton = findViewById<Button>(R.id.saveBtn)

        saveButton.setOnClickListener {
            val odoValue = odoInput.text.toString().toDoubleOrNull() ?: 0.0
            val fuelValue = fuelInput.text.toString().toDoubleOrNull() ?: 0.0
            val priceValue = priceInput.text.toString().toDoubleOrNull() ?: 0.0

            // 2. Update the FuelEntry constructor to match your new 17-field model
            val entry = FuelEntry(
                id = if (entryId == -1) 0 else entryId,
                vehicleId = vehicleId, // Now correctly assigned
                odometer = odoValue,
                fuelAmount = fuelValue,
                pricePerUnit = priceValue,
                isFullTank = true,
                notes = "",
                dateTimestamp = System.currentTimeMillis(),
                // 3. Add the 9 new optional fields as null/empty for the XML version
                stationName = null,
                location = null,
                contactNumber = null,
                fuelTypes = null,
                serviceHour = "Not Sure",
                hospitality = "Moderate",
                hasWashroom = "Not sure",
                hasWaiting = "Not sure",
                goodForRoadsideStop = "Not sure"
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 4. Ensure your DAO has an 'upsert' or 'insert' function
                    db.fuelDao().insertFuel(entry)
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
