package dev.sumanth.spd.ui.screen

import android.content.Intent
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.sumanth.spd.utils.SharedPref

@Composable
fun PreferencesScreen() {
    val context = LocalContext.current
    val sharedPref = remember { SharedPref(context) }
    var downloadPath by remember { mutableStateOf(sharedPref.getDownloadPath()) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)

            val segments = uri.path?.split(":")
            if (segments != null && segments.size > 1) {
                val folderPath = segments[1]
                val storageBase = if (uri.path?.contains("primary") == true) {
                    Environment.getExternalStorageDirectory().path
                } else {
                    "/storage/${segments[0].split("/").last()}"
                }

                downloadPath = "$storageBase/$folderPath"
                sharedPref.storeDownloadPath(downloadPath)
            }
        }
    }

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("General Settings", style = MaterialTheme.typography.titleMedium)

        Card(
            onClick = { launcher.launch(null) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Download Location", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = downloadPath,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap to change folder",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}