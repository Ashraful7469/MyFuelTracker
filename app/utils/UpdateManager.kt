package com.myfueltracker.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.myfueltracker.app.data.remote.GitHubRelease
import com.myfueltracker.app.data.remote.GitHubService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UpdateManager(private val context: Context) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(GitHubService::class.java)

    /**
     * Checks GitHub for a version tag that doesn't match the current one.
     */
    suspend fun checkForUpdates(currentVersion: String): GitHubRelease? {
        return try {
            val latest = service.getLatestRelease()
            // Compares GitHub tag (e.g., "v1.0.1") with your app's current version
            if (latest.tag_name != currentVersion) latest else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Redirects the user to the browser to download and install the APK.
     * This is the most reliable method for non-Play Store updates.
     */
    fun downloadAndInstall(url: String) {
        if (url.isBlank()) return

        try {
            // Inform the user the process is starting
            Toast.makeText(context, "Opening download link...", Toast.LENGTH_SHORT).show()

            // Creating an intent to open the URL in a browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                // Required when starting an activity from outside an Activity context
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open browser. Please check your link.", Toast.LENGTH_LONG).show()
        }
    }
}
