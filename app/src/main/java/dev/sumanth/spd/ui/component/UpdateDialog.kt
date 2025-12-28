package dev.sumanth.spd.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import dev.sumanth.spd.ui.viewmodel.UpdaterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(updaterViewModel: UpdaterViewModel) {
    if (!updaterViewModel.updateFound) return

    AlertDialog(
        onDismissRequest = { updaterViewModel.updateFound = false },
        title = { Text("New update found") },
        text = { Text("Would you like to download the new APK?") },
        confirmButton = {
            TextButton(
                onClick = {
                    updaterViewModel.viewModelScope.launch(Dispatchers.IO) {
                        updaterViewModel.updater?.requestDownload()
                    }
                    updaterViewModel.updateFound = false
                }
            ) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = { updaterViewModel.updateFound = false }) {
                Text("Cancel")
            }
        }
    )
}
