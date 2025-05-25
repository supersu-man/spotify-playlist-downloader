package dev.sumanth.spd.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.sumanth.spd.utils.SharedPref
import dev.sumanth.spd.utils.convertCodec
import dev.sumanth.spd.utils.downloadFile
import dev.sumanth.spd.utils.getFileMeta
import dev.sumanth.spd.utils.getPlaylistItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class HomeScreenViewModel(current: Context) : ViewModel() {

    var fileProgress = mutableFloatStateOf(0f)
    var totalProgress = mutableFloatStateOf(0f)
    var fileName = mutableStateOf("")
    var spotifyLink = mutableStateOf("")
    var loader = mutableStateOf(false)
    val sharedPref = mutableStateOf(SharedPref(current))
    val convertToMp3 = mutableStateOf(false)

    fun downloadPlaylist() = CoroutineScope(Dispatchers.IO).launch {
        loader.value = true
        val downloadPath = sharedPref.value.getDownloadPath()
        fileName.value = "Fetching playlist"
        val spotifyList = getPlaylistItems(spotifyLink.value)
        fileName.value = "Playlist fetched successfully"
        var queryNumber = 0
        var retries = 5
        while (queryNumber < spotifyList.size) {
            try {
                val fileMeta = getFileMeta(spotifyList[queryNumber])
                File(downloadPath).mkdir()
                fileName.value = "Downloading " + fileMeta["name"].toString()
                val path = "$downloadPath/${fileMeta["name"]}"
                downloadFile(fileMeta["url"].toString(), "$path.m4a") { b, c ->
                    fileProgress.floatValue = (b * 100 / c).toFloat() / 100
                }
                if(convertToMp3.value) {
                    convertCodec(path)
                }
                queryNumber++
                retries = 5
                totalProgress.floatValue = (queryNumber.toFloat()/spotifyList.size)
            } catch (e: Exception) {
                println(e)
                if (retries == 0) {
                    queryNumber++
                    retries = 5
                    totalProgress.floatValue = (queryNumber.toFloat()/spotifyList.size)
                } else {
                    retries--
                }
            }
            println(totalProgress)
        }
        loader.value = false
    }

}

