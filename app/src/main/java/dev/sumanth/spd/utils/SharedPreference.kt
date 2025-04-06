package dev.sumanth.spd.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Environment
import java.io.File
import androidx.core.content.edit

private lateinit var sharedPref: SharedPreferences

class SharedPref(context: Context) {

    init {
        if(!::sharedPref.isInitialized) {
            sharedPref = context.getSharedPreferences(context.packageName, MODE_PRIVATE)
        }
    }

    fun getDownloadPath(): String {
        val storedPath = sharedPref.getString("download_path", null)
        return storedPath ?: getDefaultDownloadPath()
    }

    fun storeDownloadPath(path: String) {
        sharedPref.edit { putString("download_path", path) }
    }

    private fun getDefaultDownloadPath(): String {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val defaultDownloadPath: String = File(downloadsDirectory, "spotify-playlist-downloader").path
        return defaultDownloadPath
    }

}


