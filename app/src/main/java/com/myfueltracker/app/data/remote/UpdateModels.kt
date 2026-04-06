package com.myfueltracker.app.data.remote

import retrofit2.http.GET

// This represents the main Release object from GitHub API
data class GitHubRelease(
    val tag_name: String,      // The version tag (e.g., "v1.1.0")
    val html_url: String,      // Link to the release webpage
    val assets: List<Asset>    // List of files attached to the release
)

// This represents the actual APK file attached to the release
data class Asset(
    val name: String,
    val browser_download_url: String // The direct link to download your APK
)

// The Retrofit interface to define the API call
interface GitHubService {
    @GET("repos/Ashraful7469/MyFuelTracker/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease
}
