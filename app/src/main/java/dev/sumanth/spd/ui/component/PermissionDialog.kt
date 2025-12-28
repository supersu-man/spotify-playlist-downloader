package dev.sumanth.spd.ui.component

import android.app.Activity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.sumanth.spd.utils.PermissionUtils

@Composable
fun PermissionDialog(activity: Activity) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showDialog by remember { mutableStateOf(!PermissionUtils.isPermissionsPresent(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                showDialog = !PermissionUtils.isPermissionsPresent(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Permission Required") },
            text = { Text("This app needs storage access to download and save your playlists to your device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        PermissionUtils.askForPermissions(activity)
                    }
                ) {
                    Text("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
