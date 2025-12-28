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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    var fileProgress by mutableFloatStateOf(0f)
    var totalProgress by mutableFloatStateOf(0f)
    var fileName by mutableStateOf("")
    var spotifyLink by mutableStateOf("")
    var loader by mutableStateOf(false)
    var convertToMp3 by mutableStateOf(false)
    private val sharedPref = SharedPref(application)

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    fun downloadPlaylist() {
        if (loader) return
        viewModelScope.launch {
            loader = true
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
                        withContext(Dispatchers.IO) { File(downloadPath).mkdirs() }

                        fileName = "Downloading ${item.name}"
                        val path = "$downloadPath/${sanitizeFilename(item.name)}"

                        withContext(Dispatchers.IO) {
                            DownloadManager.downloadFile(fileMeta.url, "$path.${fileMeta.extention}") { b, c ->
                                fileProgress = (b * 100 / c).toFloat() / 100
                            }
                            if (convertToMp3) {
                                DownloadManager.convertToMp3(path, fileMeta.extention)
                            }
                        }
                        totalProgress = (index + 1).toFloat() / spotifyList.size
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                fileName = "Error: ${e.message}"
            } finally {
                loader = false
            }
        }
    }
}