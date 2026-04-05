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
    // 1. FIX: Declare entryId at the class level to resolve the "Unresolved reference"
    private var entryId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_fuel)

        // Retrieve entryId if we are in "Edit" mode (passed from History screen)
        entryId = intent.getIntExtra("entryId", -1)

        db = AppDatabase.getDatabase(this)

        val odoInput = findViewById<EditText>(R.id.odo)
        val fuelInput = findViewById<EditText>(R.id.fuel)
        val priceInput = findViewById<EditText>(R.id.price)
        val saveButton = findViewById<Button>(R.id.saveBtn)

        saveButton.setOnClickListener {
            // 2. FIX: Convert text to values using the actual Input variable names
            val odoValue = odoInput.text.toString().toDoubleOrNull() ?: 0.0
            val fuelValue = fuelInput.text.toString().toDoubleOrNull() ?: 0.0
            val priceValue = priceInput.text.toString().toDoubleOrNull() ?: 0.0

            // 3. FIX: Align constructor parameters with the FuelEntry entity (odometer)
            val entry = FuelEntry(
                id = if (entryId == -1) 0 else entryId,
                odometer = odoValue,
                fuelAmount = fuelValue,
                pricePerUnit = priceValue,
                isFullTank = true, // Defaulting to true for simple XML version
                notes = "",        // Placeholder if no notes field in XML
                dateTimestamp = System.currentTimeMillis()
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 4. FIX: Use the DAO function name from your FuelDao (insertFuel)
                    db.fuelDao().insertFuel(entry)
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}