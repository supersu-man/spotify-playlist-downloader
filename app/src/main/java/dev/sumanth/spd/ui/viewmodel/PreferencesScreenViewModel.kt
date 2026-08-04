package dev.sumanth.spd.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.AndroidViewModel
import dev.sumanth.spd.utils.SharedPref
import java.io.File

class PreferencesScreenViewModel(application: Application) : AndroidViewModel(application) {
    val sharedPref = SharedPref(application)
    var downloadPath by mutableStateOf(sharedPref.getDownloadPath() ?: "No folder selected")
    var autoUpdateCheck by mutableStateOf(sharedPref.getAutoUpdateCheck())

    private val githubUrl = "https://github.com/supersu-man/spotify-playlist-downloader"
    val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)

    fun openGitHub(uriHandler: UriHandler) {
        uriHandler.openUri(githubUrl)
    }

    fun handleFolderSelection(uri: Uri?) {
        uri?.let { folderUri ->
            val context = getApplication<Application>()
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(folderUri, takeFlags)

            val uriString = folderUri.toString()
            downloadPath = uriString
            sharedPref.storeDownloadPath(uriString)
        }
    }

    fun getDisplayPath(): String {
        if (downloadPath == "No folder selected") return downloadPath
        val uri = Uri.parse(downloadPath)
        val path = uri.path ?: return downloadPath
        if (path.contains(":")) {
            return path.split(":").last()
        }
        return path
    }
}