package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.sumanth.spd.model.AppStatus
import dev.sumanth.spd.service.DownloadService
import dev.sumanth.spd.service.DownloadState
import dev.sumanth.spd.utils.SharedPref
import dev.sumanth.spd.utils.SpotifyScraper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPref = SharedPref(application)
    private val spotifyScraper = SpotifyScraper()
    private var scrapingJob: Job? = null
    
    var appStatus: AppStatus
        get() = DownloadState.appStatus
        set(value) { DownloadState.appStatus = value }
        
    var spotifyLink by mutableStateOf("")
    var convertToMp3 by mutableStateOf(false)
    var totalTracksToScrape by mutableIntStateOf(0)
    
    var currentTrack: Int
        get() = DownloadState.currentTrackIndex
        set(value) { DownloadState.currentTrackIndex = value }
        
    val tracks get() = DownloadState.tracks

    fun startScraping() {
        if (spotifyLink.isBlank()) return Toast.makeText(getApplication(), "Spotify link is invalid.", Toast.LENGTH_SHORT).show()
        if(spotifyLink.contains("?")) spotifyLink = spotifyLink.split("?")[0]
        
        appStatus = AppStatus.SCRAPING
        tracks.clear()
        totalTracksToScrape = 0
        
        scrapingJob = viewModelScope.launch {
            try {
                val scrapeResult = withContext(Dispatchers.IO) {
                    spotifyScraper.scrapePlaylist(spotifyLink) { fetchedTracks, total ->
                        viewModelScope.launch {
                            totalTracksToScrape = total
                            tracks.clear()
                            tracks.addAll(fetchedTracks)
                        }
                    }
                }
                totalTracksToScrape = scrapeResult.totalTracks
                tracks.clear()
                tracks.addAll(scrapeResult.tracks)
                appStatus = AppStatus.SCRAPING_COMPLETE
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Scraping failed: ${e.message}", Toast.LENGTH_LONG).show()
                    appStatus = AppStatus.IDLE
                }
            }
        }
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

    fun cancelScraping() {
        scrapingJob?.cancel()
        appStatus = AppStatus.IDLE
        tracks.clear()
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
