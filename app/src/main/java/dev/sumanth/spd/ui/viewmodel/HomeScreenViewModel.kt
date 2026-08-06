package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import dev.sumanth.spd.model.AppStatus
import dev.sumanth.spd.service.DownloadService
import dev.sumanth.spd.service.DownloadState
import dev.sumanth.spd.utils.SharedPref

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPref = SharedPref(application)
    
    var appStatus: AppStatus
        get() = DownloadState.appStatus
        set(value) { DownloadState.appStatus = value }
        
    var spotifyLink by mutableStateOf("")
    var convertToMp3 by mutableStateOf(false)
    
    var currentTrack: Int
        get() = DownloadState.currentTrackIndex
        set(value) { DownloadState.currentTrackIndex = value }
        
    val tracks get() = DownloadState.tracks

    fun startScraping() {
        if (spotifyLink.isBlank()) return Toast.makeText(getApplication(), "Spotify link is invalid.", Toast.LENGTH_SHORT).show()
        if(spotifyLink.contains("?")) spotifyLink = spotifyLink.split("?")[0]
        appStatus = AppStatus.SCRAPING
    }

    fun downloadPlaylist() {
        if (tracks.isEmpty()) return Toast.makeText(getApplication(), "Playlist is empty.", Toast.LENGTH_SHORT).show()
        val downloadPath = sharedPref.getDownloadPath()
        if (downloadPath == null) return Toast.makeText(getApplication(), "Please select a download folder in Settings.", Toast.LENGTH_SHORT).show()

        val intent = Intent(getApplication(), DownloadService::class.java).apply {
            action = DownloadService.ACTION_START
            putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, downloadPath)
            putExtra(DownloadService.EXTRA_CONVERT_TO_MP3, convertToMp3)
        }
        
        getApplication<Application>().startService(intent)
    }

    fun cancelDownload() {
        val intent = Intent(getApplication(), DownloadService::class.java).apply {
            action = DownloadService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }

    fun reset() {
        tracks.clear()
        spotifyLink = ""
        appStatus = AppStatus.IDLE
        currentTrack = -1
    }
    
    fun getFailedDownloadsCount(): Int = tracks.count { it.status == dev.sumanth.spd.model.DownloadStatus.FAILED }
    fun getDownloadedCount(): Int = tracks.count { it.status == dev.sumanth.spd.model.DownloadStatus.COMPLETE }
}
