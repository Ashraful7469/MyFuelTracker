package com.myfueltracker.app.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
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
            // If the GitHub tag (e.g. "v1.1.0") is different from your app version, return it
            if (latest.tag_name != currentVersion) latest else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Uses the System DownloadManager to grab the APK file.
     */
    fun downloadAndInstall(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("MyFuelTracker Update")
            .setDescription("Downloading the latest version...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MyFuelTracker_Update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }
}
