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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.supersuman.githubapkupdater.Updater
import com.supersuman.spd.CustomClass
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
import java.io.*
import java.net.HttpURLConnection


class MainActivity : ComponentActivity() {

    private val songProgress = mutableStateOf("")
    private val totalProgress = mutableStateOf("")
    private val fileName = mutableStateOf("")

    private lateinit var spotifyApi: SpotifyApi

    private val customClass = CustomClass()
    private val updater =
        Updater(this, "https://github.com/supersu-man/SpotifyPlaylistDownloader/releases/latest")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSpotify()
        NewPipe.init(Downloader.getInstance())
        if (!checkForPermissions()) askForPermissions()

        setContent {
            somFun()
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
    )
    @Composable
    fun somFun() {
        val songProgress by songProgress
        val totalProgress by totalProgress
        val fileName by fileName
        var playListLink by rememberSaveable { mutableStateOf("") }

        AppTheme {
            Surface {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = playListLink,
                        onValueChange = { playListLink = it },
                        label = { Text("Playlist link") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp)
                    )
                    Text(
                        text = "$fileName $songProgress",
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = totalProgress,
                        modifier = Modifier.padding(10.dp)
                    )
                    Button(onClick = { downloadPlaylist(playListLink) }) {
                        Text(text = "Download")
                    }
                }
            }
        }
    }

    private fun setupSpotify() = CoroutineScope(Dispatchers.IO).launch {
        spotifyApi = SpotifyApi.Builder().setClientId(getString(R.string.CLIENT_ID))
            .setClientSecret(getString(R.string.CLIENT_SECRET)).build()
        val clientCredentialsRequest = spotifyApi.clientCredentials().build()
        val clientCredentials = clientCredentialsRequest.execute()
        spotifyApi.accessToken = clientCredentials.accessToken
    }

    private fun downloadPlaylist(playListLink: String) = CoroutineScope(Dispatchers.IO).launch {
        customClass.closeKeyboard(this@MainActivity)
        toast("Fetching playlist")
        val spotifyList = getPlaylistItems(playListLink)
        toast("Successfully fetched playlist")
        println(spotifyList)
        var queryNumber = 0
        var retries = 5
        while (queryNumber < spotifyList.size) {
            try {
                val downloadLink = getDownloadLink(spotifyList[queryNumber])
                println(downloadLink)
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "spotify-playlist-downloader"
                ).mkdir()
                val path = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "spotify-playlist-downloader/$fileName"
                ).path
                downloadFile(downloadLink, path) { b, c ->
                    songProgress.value = "${(b * 100 / c).toInt()}%"
                }
                queryNumber++
                retries = 5
                totalProgress.value = "$queryNumber/${spotifyList.size}"
                println(totalProgress.value)
            } catch (e: Exception) {
                if (retries == 0) {
                    queryNumber++
                    retries = 5
                    totalProgress.value = "$queryNumber/${spotifyList.size}"
                } else {
                    retries--
                }
                println(e)
            }

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
            println("test3")
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


}
