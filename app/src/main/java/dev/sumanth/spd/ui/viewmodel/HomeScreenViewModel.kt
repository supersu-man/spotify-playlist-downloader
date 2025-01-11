package dev.sumanth.spd.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.sumanth.spd.utils.spotify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.schabi.newpipe.extractor.ServiceList.YouTube
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMusicSearchExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import se.michaelthelin.spotify.model_objects.specification.Paging
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection

class HomeScreenViewModel: ViewModel() {

    private val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/spotify-playlist-downloader"
    var fileProgress by mutableFloatStateOf(0f)
    var totalProgress by mutableFloatStateOf(0f)
    var fileName by  mutableStateOf("")
    var spotifyLink by mutableStateOf("")
    var loader by mutableStateOf(false)

    fun downloadPlaylist(playListLink: String) = CoroutineScope(Dispatchers.IO).launch {
        loader = true
        fileName = "Fetching playlist"
        val spotifyList = getPlaylistItems(playListLink)
        fileName = "Playlist fetched successfully"
        var queryNumber = 0
        var retries = 5
        while (queryNumber < spotifyList.size) {
            try {
                val fileMeta = getFileMeta(spotifyList[queryNumber])
                File(downloadsFolder).mkdir()
                fileName = "Downloading " + fileMeta["filename"].toString()
                val path = File("$downloadsFolder/${fileMeta["filename"]}").path
                downloadFile(fileMeta["url"].toString(), path) { b, c ->
                    fileProgress = (b * 100 / c).toFloat() / 100
                }
                queryNumber++
                retries = 5
                totalProgress = (queryNumber.toFloat()/spotifyList.size)
            } catch (e: Exception) {
                if (retries == 0) {
                    queryNumber++
                    retries = 5
                    totalProgress = (queryNumber.toFloat()/spotifyList.size)
                } else {
                    retries--
                }
            }
            println(totalProgress)
        }
        loader = false
    }


    fun openFolder(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.parse(downloadsFolder)
        intent.setDataAndType(uri, "resource/folder")
        context.startActivity(intent)
    }

}

private fun getPlaylistItems(link: String): MutableList<String> {
    val arrayList: MutableList<String> = ArrayList()
    try {
        val playlistId = link.split("playlist/")[1].split("?")[0]
        val getPlaylistsItemsRequests = spotify.spotifyApi.getPlaylistsItems(playlistId).build()
        var playlistTrackPaging: Paging<PlaylistTrack>?
        playlistTrackPaging = getPlaylistsItemsRequests.execute()
        for (i in 0..playlistTrackPaging!!.total / 100) {
            playlistTrackPaging = spotify.spotifyApi.getPlaylistsItems(playlistId).offset(i * 100).build().execute()
            for (j in playlistTrackPaging!!.items.indices) {
                val temp = playlistTrackPaging.items[j].track
                val songName = temp.name
                val artistName =
                    temp.toString().split("artists=")[1].split("name=")[1].split(",")[0]
                arrayList.add("$songName by $artistName")
            }
        }
    } catch (e: Exception) {
        println(e.message)
    }
    return arrayList
}

private fun getFileMeta(text: String): HashMap<String, String> {
    val extra = YouTube.getSearchExtractor(
        text,
        listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS),
        null
    ) as YoutubeMusicSearchExtractor
    extra.fetchPage()
    val mediaLink = extra.initialPage.items[0].url
    val extractor = YouTube.getStreamExtractor(mediaLink)
    extractor.fetchPage()
    extractor.audioStreams.sortByDescending { it.bitrate }
    val url = extractor.audioStreams[0].content
    return hashMapOf("url" to url, "filename" to "${extractor.name}.${extractor.audioStreams[0].format}")
}

private fun downloadFile(link: String, path: String, progress: ((Long, Long) -> Unit)? = null) {
    val request = Request.Builder().addHeader("Range", "bytes=0-").url(link).build()
    val response = OkHttpClient().newCall(request).execute()
    val body = response.body
    val responseCode = response.code
    if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE && body != null) {
        val length = body.contentLength()
        body.byteStream().apply {
            FileOutputStream(File(path)).use { output ->
                var bytesCopied = 0L
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = read(buffer)
                while (bytes >= 0) {
                    output.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    progress?.invoke(bytesCopied, length)
                    bytes = read(buffer)
                }
            }
        }
    }
}


