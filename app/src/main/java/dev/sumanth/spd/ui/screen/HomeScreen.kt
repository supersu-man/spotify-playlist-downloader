package dev.sumanth.spd.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel) {
    Column(modifier = Modifier.padding(10.dp, 20.dp).fillMaxSize()) {
        Text("Enter Playlist Link")
        OutlinedTextField(
            value = viewModel.spotifyLink, onValueChange = { viewModel.spotifyLink = it },
            label = { Text("Paste playlist link here") },
            modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp), minLines = 3
        )
        Button (onClick = { viewModel.downloadPlaylist(viewModel.spotifyLink) }, modifier = Modifier.padding(0.dp, 5.dp).fillMaxWidth().height(40.dp)) {
            if(viewModel.loader) {
                CircularProgressIndicator(modifier = Modifier.fillMaxHeight().aspectRatio(1f), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Download Playlist")
            }
        }
        HorizontalDivider(modifier = Modifier.padding(0.dp, 20.dp))
        if (viewModel.totalProgress > 0f && viewModel.totalProgress < 1f) {
            Text(text = viewModel.fileName, modifier = Modifier.padding(0.dp, 10.dp))
            LinearProgressIndicator(progress = { viewModel.totalProgress }, modifier = Modifier.fillMaxWidth().padding(0.dp, 10.dp).height(14.dp), drawStopIndicator = { })
            Text(text = String.format(java.util.Locale.ENGLISH, "%.1f", viewModel.totalProgress * 100) + "%", modifier = Modifier.padding(0.dp, 10.dp).fillMaxWidth(), textAlign = TextAlign.Center)
        }
        if (viewModel.totalProgress == 1f) {
            Text(text =  "Download complete", modifier = Modifier.padding(0.dp, 10.dp).fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}








