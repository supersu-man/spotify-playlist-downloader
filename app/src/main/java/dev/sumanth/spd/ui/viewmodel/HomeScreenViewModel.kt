package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.sumanth.spd.utils.DownloadManager
import dev.sumanth.spd.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.michaelthelin.spotify.model_objects.specification.Track

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    var fileProgress by mutableFloatStateOf(0f)
    var totalProgress by mutableFloatStateOf(0f)
    var fileName by mutableStateOf("")
    var spotifyLink by mutableStateOf("")
    var loader by mutableStateOf(false)
    var convertToMp3 by mutableStateOf(false)
    private val sharedPref = SharedPref(application)
    private var downloadJob: Job? = null
    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }
    private val failedTracks = mutableListOf<Track>()
    var downloadCompletedWithFailures by mutableStateOf(false)

    fun downloadPlaylist() {
        if (loader) return
        downloadJob = viewModelScope.launch {
            loader = true
            downloadCompletedWithFailures = false
            failedTracks.clear()
            try {
                val downloadPath = sharedPref.getDownloadPath()
                fileName = "Fetching playlist..."
                val spotifyList = withContext(Dispatchers.IO) {
                    DownloadManager.getPlaylistItems(spotifyLink)
                }
                if (spotifyList.isEmpty()) {
                    fileName = "Playlist is empty or link is invalid"
                    loader = false
                    return@launch
                }
                fileName = "Playlist fetched successfully"

                spotifyList.forEachIndexed { index, item ->
                    try {
                        val fileMeta = withContext(Dispatchers.IO) {
                            DownloadManager.getFileMeta(item)
                        }

                        fileName = "Downloading ${item.name}"
                        val path = "$downloadPath/${sanitizeFilename(item.name)}"

                        withContext(Dispatchers.IO) {
                            DownloadManager.downloadFile(fileMeta.url, "$path.${fileMeta.extention}") { b, c ->
                                fileProgress = (b * 100 / c).toFloat() / 100
                            }
                            if (convertToMp3) {
                                DownloadManager.convertToMp3(path, fileMeta.extention, item)
                            } else {
                                DownloadManager.tagFile(path, fileMeta.extention, item)
                            }
                        }
                        totalProgress = (index + 1).toFloat() / spotifyList.size
                    } catch (e: Exception) {
                        e.printStackTrace()
                        failedTracks.add(item)
                    }
                }
            } catch (e: Exception) {
                fileName = "Error: ${e.message}"
            } finally {
                loader = false
                if (failedTracks.isNotEmpty()) {
                    downloadCompletedWithFailures = true
                }
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        loader = false
        fileName = "Download cancelled"
        totalProgress = 0f
    }
    fun retryFailedDownloads() {
        downloadJob = viewModelScope.launch {
            loader = true
            downloadCompletedWithFailures = false
            val tracksToRetry = failedTracks.toList()
            failedTracks.clear()
            try {
                val downloadPath = sharedPref.getDownloadPath()
                tracksToRetry.forEachIndexed { index, item ->
                    try {
                        val fileMeta = withContext(Dispatchers.IO) {
                            DownloadManager.getFileMeta(item)
                        }
                        fileName = "Downloading ${item.name}"
                        val path = "$downloadPath/${sanitizeFilename(item.name)}"

                        withContext(Dispatchers.IO) {
                            DownloadManager.downloadFile(fileMeta.url, "$path.${fileMeta.extention}") { b, c ->
                                fileProgress = (b * 100 / c).toFloat() / 100
                            }
                            if (convertToMp3) {
                                DownloadManager.convertToMp3(path, fileMeta.extention, item)
                            } else {
                                DownloadManager.tagFile(path, fileMeta.extention, item)
                            }
                        }
                        totalProgress = (index + 1).toFloat() / tracksToRetry.size
                    } catch (e: Exception) {
                        e.printStackTrace()
                        failedTracks.add(item)
                    }
                }
            } catch (e: Exception) {
                fileName = "Error: ${e.message}"
            } finally {
                loader = false
                if (failedTracks.isNotEmpty()) {
                    downloadCompletedWithFailures = true
                } else {
                    fileName = "Download completed"
                }
            }
        }
    }
    fun getFailedDownloadsCount(): Int = failedTracks.size
}
