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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel
import java.util.Locale

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val homeScreenViewModel by remember { mutableStateOf(HomeScreenViewModel(context)) }
    Column(modifier = Modifier.padding(10.dp, 20.dp).fillMaxSize()) {
        Text("Enter Playlist Link")
        OutlinedTextField(
            value = homeScreenViewModel.spotifyLink.value, onValueChange = { homeScreenViewModel.spotifyLink.value = it },
            label = { Text("Paste playlist link here") },
            modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp), minLines = 3
        )
        Button (onClick = { homeScreenViewModel.downloadPlaylist() }, modifier = Modifier.padding(0.dp, 5.dp).fillMaxWidth().height(40.dp)) {
            if(homeScreenViewModel.loader.value) {
                CircularProgressIndicator(modifier = Modifier.fillMaxHeight().aspectRatio(1f), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Download Playlist")
            }
        }
        HorizontalDivider(modifier = Modifier.padding(0.dp, 20.dp))
        if (homeScreenViewModel.totalProgress.floatValue > 0f && homeScreenViewModel.totalProgress.floatValue < 100f) {
            Text(text = homeScreenViewModel.fileName.value, modifier = Modifier.padding(0.dp, 10.dp))
            LinearProgressIndicator(progress = { homeScreenViewModel.totalProgress.floatValue }, modifier = Modifier.fillMaxWidth().padding(0.dp, 10.dp).height(14.dp), drawStopIndicator = { })
            Text(text = String.format(Locale.ENGLISH, "%.1f", homeScreenViewModel.totalProgress.floatValue * 100) + "%", modifier = Modifier.padding(0.dp, 10.dp).fillMaxWidth(), textAlign = TextAlign.Center)
        }
        if (homeScreenViewModel.totalProgress.floatValue == 1f) {
            Text(text =  "Download complete", modifier = Modifier.padding(0.dp, 10.dp).fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}








