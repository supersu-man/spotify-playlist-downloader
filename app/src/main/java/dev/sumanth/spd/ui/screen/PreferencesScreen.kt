package dev.sumanth.spd.ui.screen

import android.content.Intent
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sumanth.spd.utils.SharedPref

@Composable
fun PreferencesScreen() {
    val context = LocalContext.current
    val sharedPref = remember { SharedPref(context) }
    var downloadPath by remember { mutableStateOf(sharedPref.getDownloadPath()) }
    var autoUpdateCheck by remember { mutableStateOf(sharedPref.getAutoUpdateCheck()) }

    val uriHandler = LocalUriHandler.current
    val githubUrl = "https://github.com/supersu-man/spotify-playlist-downloader"

    val version = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "Version ${packageInfo.versionName}"
        } catch (e: Exception) {
            "Version N/A"
        }
    }

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

        Text("Updates", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Auto-check for updates", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = autoUpdateCheck,
                    onCheckedChange = {
                        autoUpdateCheck = it
                        sharedPref.storeAutoUpdateCheck(it)
                    }
                )
            }
        }

        Text("About", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "Developer", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Developed by Sumanth", style = MaterialTheme.typography.bodyLarge)
                }
                Row(
                    modifier = Modifier.clickable { uriHandler.openUri(githubUrl) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Source Code on GitHub",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.Launch,
                        contentDescription = "Open Link",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = version,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}