package dev.sumanth.spd.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = viewModel()) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize(),
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

        Button(
            onClick = { viewModel.downloadPlaylist() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = !viewModel.loader
        ) {
            if (viewModel.loader) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Download Playlist")
            }
        }

        if (viewModel.totalProgress > 0f) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusText = if (viewModel.totalProgress == 1f) "Download complete" else viewModel.fileName
                    Text(text = statusText, style = MaterialTheme.typography.bodyMedium,
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
    }
}