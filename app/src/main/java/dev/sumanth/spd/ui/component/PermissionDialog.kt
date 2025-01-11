package dev.sumanth.spd.ui.component

import android.app.Activity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import dev.sumanth.spd.utils.askForPermissions
import dev.sumanth.spd.utils.isPermissionsPresent

@Composable
fun PermissionDialog(activity: Activity) {
    val context = LocalContext.current
    var needPermissions by remember { mutableStateOf(!isPermissionsPresent(context)) }
    if(!needPermissions) return
    AlertDialog(
        title = { Text("Permission required") },
        text = { Text(text = "Need permission to download files to Download folder") },
        onDismissRequest = { needPermissions = false },
        confirmButton = {
            TextButton(onClick = {
                askForPermissions(activity)
                needPermissions = false
            }) {
                Text(text = "Grant")
            }
        }
    )
}