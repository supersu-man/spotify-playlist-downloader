package dev.sumanth.spd.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sumanth.spd.ui.viewmodel.UpdaterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(updaterViewModel: UpdaterViewModel) {
    if(!updaterViewModel.updateFound) return
    AlertDialog(
        title = { Text("New update found") },
        text = { Text(text = "Would you like to download new apk?") },
        onDismissRequest = { updaterViewModel.updateFound = false },
        confirmButton = {
            TextButton(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    updaterViewModel.updater.requestDownload()
                }
                updaterViewModel.updateFound = false
            }) {
                Text(text = "Download")
            }
        }
    )
}