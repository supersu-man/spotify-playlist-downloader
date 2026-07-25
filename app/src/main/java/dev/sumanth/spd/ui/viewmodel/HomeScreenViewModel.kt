package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.sumanth.spd.model.AppStatus
import dev.sumanth.spd.model.DownloadStatus
import dev.sumanth.spd.model.Track
import dev.sumanth.spd.utils.DownloadManager
import dev.sumanth.spd.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPref = SharedPref(application)
    var appStatus by mutableStateOf(AppStatus.IDLE)
    var spotifyLink by mutableStateOf("")
    var convertToMp3 by mutableStateOf(false)
    private var downloadJob: Job? = null
    var currentTrack by mutableIntStateOf(-1)
    var tracks = mutableStateListOf<Track>()

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    fun startScraping() {
        if (spotifyLink.isBlank()) return Toast.makeText(getApplication(), "Spotify link is invalid.", Toast.LENGTH_SHORT).show()
        if(spotifyLink.contains("?")) spotifyLink = spotifyLink.split("?")[0]
        appStatus = AppStatus.SCRAPING
    }

    fun downloadPlaylist() {
        if (tracks.isEmpty()) return Toast.makeText(getApplication(), "Playlist is empty.", Toast.LENGTH_SHORT).show()
        downloadJob = viewModelScope.launch {
            appStatus = AppStatus.DOWNLOADING
            try {
                val downloadPath = sharedPref.getDownloadPath()

                tracks.forEachIndexed { i, track ->
                    if (track.status == DownloadStatus.COMPLETE) return@forEachIndexed
                    
                    currentTrack = i
                    tracks[i] = track.copy(status = DownloadStatus.DOWNLOADING)
                    try {
                        val fileMeta = withContext(Dispatchers.IO) {
                            DownloadManager.getFileMeta(track.title, track.artist)
                        }
                        val path = "$downloadPath/${sanitizeFilename(track.title)}"
                        withContext(Dispatchers.IO) {
                            DownloadManager.downloadFile(fileMeta.url, "$path.${fileMeta.extention}")
                            if (convertToMp3) {
                                DownloadManager.convertToMp3(path, fileMeta.extention, track.title, track.artist)
                            } else {
                                DownloadManager.tagFile(path, fileMeta.extention, track.title, track.artist)
                            }
                        }
                        tracks[i] = track.copy(status = DownloadStatus.COMPLETE)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        tracks[i] = track.copy(status = DownloadStatus.FAILED)
                    }

                }
                appStatus = AppStatus.DOWNLOADING_COMPLETE
                currentTrack = -1
            } catch (e: Exception) {
                appStatus = AppStatus.IDLE
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        appStatus = AppStatus.SCRAPING_COMPLETE
    }

    fun reset() {
        tracks.clear()
        spotifyLink = ""
        appStatus = AppStatus.IDLE
        currentTrack = -1
    }
    
    fun getFailedDownloadsCount(): Int = tracks.count { it.status == DownloadStatus.FAILED }
    fun getDownloadedCount(): Int = tracks.count { it.status == DownloadStatus.COMPLETE }
}
