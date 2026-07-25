package dev.sumanth.spd.ui.component

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.sumanth.spd.model.AppStatus
import dev.sumanth.spd.model.DownloadStatus
import dev.sumanth.spd.model.Track
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel
import dev.sumanth.spd.utils.SpotifyManager
import org.json.JSONArray


@Composable
fun SpotifyDialog(viewModel: HomeScreenViewModel) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scraping Playlist...",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
            Button(onClick = { viewModel.appStatus = AppStatus.IDLE }) {
                Text("Close")
            }
        }
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36"
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("Scraper", "Page finished loading: $url")
                            view?.evaluateJavascript(SpotifyManager.jsScript, null)
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            consoleMessage?.message()?.let { msg ->
                                if (msg.startsWith("FINAL_ROWS:")) {
                                    viewModel.tracks.clear()
                                    val rows = JSONArray(msg.substringAfter("FINAL_ROWS: "))
                                    for (i in 0 until rows.length()) {
                                        val track = rows.getJSONObject(i)
                                        val trackName = track.getString("title")
                                        val artist = track.getString("artist")
                                        viewModel.tracks.add(Track(trackName, artist, DownloadStatus.IDLE))
                                    }
                                    Log.d("Scraper", "FINAL_DATA: " + viewModel.tracks.size)
                                    viewModel.appStatus = AppStatus.SCRAPING_COMPLETE
                                } else if (msg.startsWith("JS: ERROR")) {
                                    viewModel.appStatus = AppStatus.IDLE
                                } else {
                                    Log.d("WebViewConsole", msg)
                                }
                            }
                            return true
                        }
                    }
                    loadUrl(viewModel.spotifyLink)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}