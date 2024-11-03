package dev.sumanth.spd

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.supersuman.apkupdater.ApkUpdater
import dev.sumanth.spd.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList.YouTube
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMusicSearchExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.model_objects.specification.Paging
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection


class MainActivity : ComponentActivity() {

    private val totalProgress = mutableFloatStateOf(0f)
    private val fileName = mutableStateOf("")
    private val updateFound = mutableStateOf(false)

    private lateinit var spotifyApi: SpotifyApi

    private lateinit var updater: ApkUpdater
    private val url = "https://github.com/supersu-man/spotify-playlist-downloader/releases/latest"

    private val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/spotify-playlist-downloader"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSpotify()
        checkForUpdates()
        NewPipe.init(Downloader.getInstance())
        if (!checkForPermissions()) askForPermissions()

        setContent { App() }
    }

    private fun checkForUpdates() = CoroutineScope(Dispatchers.IO).launch {
        updater = ApkUpdater(this@MainActivity, url)
        updater.threeNumbers = true
        if (updater.isInternetConnection() && updater.isNewUpdateAvailable() == true) {
            updateFound.value = true
        }
    }

    @Composable
    fun App() {
        val totalProgress by totalProgress
        val fileName by fileName
        var playListLink by rememberSaveable { mutableStateOf("") }
        var openAlertDialog by updateFound

        Background {
            if (openAlertDialog) {
                UpdateDialog(updater = updater) { openAlertDialog = false }
            }
            TextInput(playListLink = playListLink) { playListLink = it }
            if (fileName != "") {
                Text(text = "Downloading $fileName", modifier = Modifier.padding(10.dp), textAlign = TextAlign.Center)
                ProgressIndicator(currentProgress = totalProgress, text = String.format("%.1f", totalProgress*100) + "%")
            }
            Row {
                Button(text = "Download") { downloadPlaylist(playListLink) }
                Button(text = "Show downloads") { openFolder() }
            }
        }
    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.parse(downloadsFolder)
        intent.setDataAndType(uri, "resource/folder")
        startActivity(intent)
    }

    private fun setupSpotify() = CoroutineScope(Dispatchers.IO).launch {
        spotifyApi = SpotifyApi.Builder().setClientId(getString(R.string.CLIENT_ID))
            .setClientSecret(getString(R.string.CLIENT_SECRET)).build()
        val clientCredentialsRequest = spotifyApi.clientCredentials().build()
        val clientCredentials = clientCredentialsRequest.execute()
        spotifyApi.accessToken = clientCredentials.accessToken
    }

    private fun downloadPlaylist(playListLink: String) = CoroutineScope(Dispatchers.IO).launch {
        closeKeyboard()
        toast("Fetching playlist")
        val spotifyList = getPlaylistItems(playListLink)
        toast("Successfully fetched playlist")
        println(spotifyList)
        var queryNumber = 0
        var retries = 5
        while (queryNumber < spotifyList.size) {
            try {
                val downloadLink = getDownloadLink(spotifyList[queryNumber])
                File(downloadsFolder).mkdir()
                val path = File("$downloadsFolder/${fileName.value}").path
                downloadFile(downloadLink, path) { b, c ->
//                    songProgress.floatValue = (b * 100 / c).toFloat()
                }
                queryNumber++
                retries = 5
                totalProgress.floatValue = (queryNumber.toFloat()/spotifyList.size)
            } catch (e: Exception) {
                if (retries == 0) {
                    queryNumber++
                    retries = 5
                    totalProgress.floatValue = (queryNumber.toFloat()/spotifyList.size)
                } else {
                    retries--
                }
                println(e)
            }
            println(queryNumber.toFloat()/spotifyList.size)
        }
    }

    private fun getPlaylistItems(link: String): MutableList<String> {
        val arrayList: MutableList<String> = ArrayList()
        try {
            val playlistId = link.split("playlist/")[1].split("?")[0]
            val getPlaylistsItemsRequests = spotifyApi.getPlaylistsItems(playlistId).build()
            var playlistTrackPaging: Paging<PlaylistTrack>?
            playlistTrackPaging = getPlaylistsItemsRequests.execute()
            for (i in 0..playlistTrackPaging!!.total / 100) {
                playlistTrackPaging =
                    spotifyApi.getPlaylistsItems(playlistId).offset(i * 100).build().execute()
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

    private fun getDownloadLink(text: String): String {
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
        fileName.value = "${extractor.name}.${extractor.audioStreams[0].format}"
        return url
    }

    private fun downloadFile(link: String, path: String, progress: ((Long, Long) -> Unit)? = null) {
        val request = Request.Builder().addHeader("Range", "bytes=0-").url(link).build()
        val response = OkHttpClient().newCall(request).execute()
        val body = response.body
        val responseCode = response.code
        println(responseCode)
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

    private fun askForPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${applicationContext.packageName}")
            startActivity(intent)
        }
    }

    private fun checkForPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun toast(string: String) {
        runOnUiThread {
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager =
                this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}
