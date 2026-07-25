package dev.sumanth.spd.ui.component

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel


@Composable
fun Idle(viewModel: HomeScreenViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
            onClick = { viewModel.startScraping() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("Scrape Playlist")
        }

    }
}