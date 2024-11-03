package dev.sumanth.spd


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.supersuman.apkupdater.ApkUpdater
import dev.sumanth.spd.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun UpdateDialog(updater: ApkUpdater, close: () -> Unit) {
    AlertDialog(
        title = { Text("New update found") },
        text = { Text(text = "Would you like to download new apk?") },
        onDismissRequest = { close() },
        confirmButton = {
            TextButton(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    updater.requestDownload()
                }
                close()
            }) {
                Text(text = "Download")
            }
        }
    )
}

@Composable
fun ProgressIndicator(currentProgress: Float, text: String) {
    Text(text = text, modifier = Modifier.padding(0.dp, 10.dp))
    LinearProgressIndicator(
        progress = { currentProgress },
        modifier = Modifier.fillMaxWidth().padding(20.dp, 10.dp),
        drawStopIndicator = { }
    )
}

@Composable
fun TextInput(playListLink: String, onValueChange: (it: String) -> Unit) {
    OutlinedTextField(
        value = playListLink,
        onValueChange = { onValueChange(it) },
        label = { Text("Playlist link") },
        modifier = Modifier.fillMaxWidth().padding(30.dp, 10.dp)
    )
}

@Composable
fun Button(text: String, onClick: () -> Unit) {
    androidx.compose.material3.Button(onClick = { onClick() }, modifier = Modifier.padding(10.dp)) {
        Text(text = text)
    }
}

@Composable
fun Background (callback: @Composable () -> Unit) {
    AppTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                callback()
            }
        }
    }
}

@Composable
fun PermissionDialog(close: () -> Unit) {
    AlertDialog(
        title = { Text("Permission required") },
        text = { Text(text = "Need permission to download files to Download folder") },
        onDismissRequest = { close() },
        confirmButton = {
            TextButton(onClick = { close() }) {
                Text(text = "Ok")
            }
        }
    )
}