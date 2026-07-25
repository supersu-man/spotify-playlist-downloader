package dev.sumanth.spd.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sumanth.spd.model.AppStatus
import dev.sumanth.spd.model.DownloadStatus
import dev.sumanth.spd.model.Track
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel

@Composable
fun Queue(viewModel: HomeScreenViewModel) {
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.currentTrack) {
        if (viewModel.currentTrack >= 0) {
            listState.animateScrollToItem(viewModel.currentTrack)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Queue (${viewModel.tracks.size})",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Downloaded: ${viewModel.getDownloadedCount()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Failed: ${viewModel.getFailedDownloadsCount()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(viewModel.tracks) { index, track ->
                    TrackItem(index + 1, track)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                }
            }

            val failedCount = viewModel.getFailedDownloadsCount()
            val isDownloading = viewModel.appStatus == AppStatus.DOWNLOADING
            val isDownloadComplete = viewModel.appStatus == AppStatus.DOWNLOADING_COMPLETE
            val buttonText = when {
                isDownloading -> "Cancel Download"
                isDownloadComplete && failedCount > 0 -> "Download Failed Tracks ($failedCount)"
                isDownloadComplete -> "Download Another Playlist"
                else -> "Download All"
            }

            Button(
                onClick = { 
                    if (isDownloading) viewModel.cancelDownload()
                    else if (isDownloadComplete && failedCount == 0) viewModel.reset()
                    else viewModel.downloadPlaylist()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(56.dp),
                enabled = viewModel.tracks.isNotEmpty(),
                colors = if (isDownloading) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                if (isDownloading) {
                    Icon(Icons.Default.Close, contentDescription = null)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                }
                Text(buttonText, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun TrackItem(index: Int, track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "$index.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        StatusIcon(status = track.status)
    }
}

@Composable
fun StatusIcon(status: DownloadStatus) {
    when (status) {
        DownloadStatus.IDLE -> Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Idle",
            tint = MaterialTheme.colorScheme.outline
        )
        DownloadStatus.DOWNLOADING -> CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        DownloadStatus.FAILED -> Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Failed",
            tint = MaterialTheme.colorScheme.error
        )
        DownloadStatus.COMPLETE -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Complete",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
