package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.sumanth.spd.model.Track
import dev.sumanth.spd.utils.DownloadManager
import dev.sumanth.spd.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

enum class Status {
    IDLE,
    SCRAPING,
    SCRAPED,
    DOWNLOADING,
    RETRYING,
    COMPLETED,
}
class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    var fileProgress by mutableFloatStateOf(0f)
    var totalProgress by mutableFloatStateOf(0f)
    var fileName by mutableStateOf("")
    var spotifyLink by mutableStateOf("")
    var convertToMp3 by mutableStateOf(false)
    private val sharedPref = SharedPref(application)
    private var downloadJob: Job? = null
    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }
    val failedTracks = mutableListOf<Track>()
    var appStatus by mutableStateOf(Status.IDLE)

    var spotifyList by mutableStateOf(JSONArray())


    fun startScraping() {
        if (spotifyLink.isBlank()) return Toast.makeText(getApplication(), "Spotify link is invalid.", Toast.LENGTH_SHORT).show()
        appStatus = Status.SCRAPING
    }

    fun downloadPlaylist() {
        if(spotifyList.length() == 0) return Toast.makeText(getApplication(), "Playlist is empty.", Toast.LENGTH_SHORT).show()
        downloadJob = viewModelScope.launch {
            appStatus = Status.DOWNLOADING
            failedTracks.clear()
            try {
                val downloadPath = sharedPref.getDownloadPath()

                for (i in 0 until spotifyList.length()) {
                    val track = spotifyList.getJSONObject(i)
                    val trackName = track.getString("title")
                    val artist = track.getString("artist")
                    try {
                        val fileMeta = withContext(Dispatchers.IO) {
                            DownloadManager.getFileMeta(trackName, artist)
                        }
                        fileName = "Downloading $trackName"
                        val path = "$downloadPath/${sanitizeFilename(trackName)}"
                        withContext(Dispatchers.IO) {
                            DownloadManager.downloadFile(fileMeta.url, "$path.${fileMeta.extention}") { b, c ->
                                fileProgress = (b * 100 / c).toFloat() / 100
                            }
                            if (convertToMp3) {
                                DownloadManager.convertToMp3(path, fileMeta.extention, trackName, artist)
                            } else {
                                DownloadManager.tagFile(path, fileMeta.extention, trackName, artist)
                            }
                        }
                        totalProgress = (i + 1).toFloat() / spotifyList.length()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        failedTracks.add(Track(trackName, artist))
                    }

                }
                appStatus = Status.COMPLETED
            } catch (e: Exception) {
                appStatus = Status.IDLE
                fileName = "Error: ${e.message}"
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        appStatus = Status.SCRAPED
        fileName = "Download cancelled"
        totalProgress = 0f
    }
    fun retryFailedDownloads() {
        downloadJob = viewModelScope.launch {
            appStatus = Status.DOWNLOADING
            val tracksToRetry = failedTracks.toList()
            failedTracks.clear()
            try {
                val downloadPath = sharedPref.getDownloadPath()
                tracksToRetry.forEachIndexed { index, item ->
                    try {
                        val fileMeta = withContext(Dispatchers.IO) {
                            DownloadManager.getFileMeta(item.title, item.artist)
                        }
                        fileName = "Downloading ${item.title}"
                        val path = "$downloadPath/${sanitizeFilename(item.title)}"

                        withContext(Dispatchers.IO) {
                            DownloadManager.downloadFile(fileMeta.url, "$path.${fileMeta.extention}") { b, c ->
                                fileProgress = (b * 100 / c).toFloat() / 100
                            }
                            if (convertToMp3) {
                                DownloadManager.convertToMp3(path, fileMeta.extention, item.title, item.artist)
                            } else {
                                DownloadManager.tagFile(path, fileMeta.extention, item.title, item.artist)
                            }
                        }
                        totalProgress = (index + 1).toFloat() / tracksToRetry.size
                    } catch (e: Exception) {
                        e.printStackTrace()
                        failedTracks.add(item)
                    }
                }
                fileName = "Download completed"
            } catch (e: Exception) {
                fileName = "Error: ${e.message}"
            }
        }
    }
    fun getFailedDownloadsCount(): Int = failedTracks.size
}
