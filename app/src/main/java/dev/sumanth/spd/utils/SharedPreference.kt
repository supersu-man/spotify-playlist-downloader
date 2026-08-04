package dev.sumanth.spd.utils

import android.content.Context
import androidx.core.content.edit
import java.io.File

class SharedPref(context: Context) {

    private val sharedPref = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getDownloadPath(): String? {
        return sharedPref.getString(KEY_DOWNLOAD_PATH, null)
    }

    fun storeDownloadPath(path: String) {
        sharedPref.edit {
            putString(KEY_DOWNLOAD_PATH, path)
        }
    }

    fun getAutoUpdateCheck(): Boolean {
        return sharedPref.getBoolean(KEY_AUTO_UPDATE_CHECK, true)
    }

    fun storeAutoUpdateCheck(enabled: Boolean) {
        sharedPref.edit { 
            putBoolean(KEY_AUTO_UPDATE_CHECK, enabled)
        }
    }

    companion object {
        private const val PREFS_NAME = "spd_settings"
        private const val KEY_DOWNLOAD_PATH = "download_path"
        private const val KEY_AUTO_UPDATE_CHECK = "auto_update_check"
    }
}