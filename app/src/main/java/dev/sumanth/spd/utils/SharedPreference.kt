package dev.sumanth.spd.utils

import android.content.Context
import android.os.Environment
import androidx.core.content.edit
import java.io.File

class SharedPref(context: Context) {

    private val sharedPref = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getDownloadPath(): String {
        return sharedPref.getString(KEY_DOWNLOAD_PATH, null) ?: getDefaultDownloadPath()
    }

    fun storeDownloadPath(path: String) {
        sharedPref.edit {
            putString(KEY_DOWNLOAD_PATH, path)
        }
    }

    private fun getDefaultDownloadPath(): String {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDirectory, DEFAULT_FOLDER_NAME).absolutePath
    }

    companion object {
        private const val PREFS_NAME = "spd_settings"
        private const val KEY_DOWNLOAD_PATH = "download_path"
        private const val DEFAULT_FOLDER_NAME = "spotify-playlist-downloader"
    }
}