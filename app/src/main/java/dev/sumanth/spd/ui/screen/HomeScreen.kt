package dev.sumanth.spd.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sumanth.spd.ui.component.SpotifyDialog
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel
import dev.sumanth.spd.ui.viewmodel.Status
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = viewModel()) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.appStatus == Status.SCRAPING) {
            SpotifyDialog(viewModel)
        }

        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text("Playlist Details", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = viewModel.spotifyLink,
                        onValueChange = { viewModel.spotifyLink = it },
                        label = { Text("Spotify Playlist Link") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Convert to MP3", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = viewModel.convertToMp3, onCheckedChange = { viewModel.convertToMp3 = it })
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.startScraping() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = viewModel.appStatus != Status.SCRAPING
                ) {
                    if (viewModel.appStatus == Status.SCRAPING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Scrape Playlist")
                    }
                }
                
                Button(
                    onClick = { viewModel.downloadPlaylist() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = viewModel.appStatus == Status.SCRAPED
                ) {
                    if (viewModel.appStatus == Status.DOWNLOADING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Download Playlist")
                    }
                }

                if (viewModel.appStatus == Status.DOWNLOADING) {
                    OutlinedButton(
                        onClick = { viewModel.cancelDownload() },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }

            if (viewModel.appStatus == Status.SCRAPED) {
                Text(
                    text = "Scraping complete!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (viewModel.totalProgress > 0f) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusText = if (viewModel.totalProgress == 1f) "Download complete" else viewModel.fileName
                        Text(
                            text = statusText, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (viewModel.totalProgress < 1f) {
                            LinearProgressIndicator(
                                progress = { viewModel.totalProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp)
                            )
                            Text(
                                text = String.format(Locale.ENGLISH, "%.1f%%", viewModel.totalProgress * 100),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            if (viewModel.failedTracks.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${viewModel.getFailedDownloadsCount()} songs failed to download.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.retryFailedDownloads() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = viewModel.appStatus == Status.COMPLETED
                        ) {
                            if (viewModel.appStatus == Status.RETRYING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Retry Failed Downloads")
                            }
                        }
                    }
                }
            }
        }
    }
}
