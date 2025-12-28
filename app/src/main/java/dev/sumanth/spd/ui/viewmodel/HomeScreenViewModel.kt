package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sumanth.spd.utils.SharedPref
import dev.sumanth.spd.utils.convertCodec
import dev.sumanth.spd.utils.downloadFile
import dev.sumanth.spd.utils.getFileMeta
import dev.sumanth.spd.utils.getPlaylistItems
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

    fun downloadPlaylist() {
        if (loader) return
        viewModelScope.launch {
            loader = true
            try {
                val downloadPath = sharedPref.getDownloadPath()
                fileName = "Fetching playlist..."
                val spotifyList = withContext(Dispatchers.IO) { getPlaylistItems(spotifyLink) }
                if (spotifyList.isEmpty()) {
                    fileName = "Playlist is empty or link is invalid"
                    loader = false
                    return@launch
                }
                fileName = "Playlist fetched successfully"

                spotifyList.forEachIndexed { index, item ->
                    try {
                        val fileMeta = withContext(Dispatchers.IO) { getFileMeta(item) }
                        withContext(Dispatchers.IO) { File(downloadPath).mkdirs() }

                        val name = fileMeta["name"].toString()
                        fileName = "Downloading $name"
                        val path = "$downloadPath/$name"

                        withContext(Dispatchers.IO) {
                            downloadFile(fileMeta["url"].toString(), "$path.m4a") { b, c ->
                                fileProgress = (b * 100 / c).toFloat() / 100
                            }
                            if (convertToMp3) {
                                convertCodec(path)
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

