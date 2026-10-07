package dev.sumanth.spd.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import java.io.File

class SharedPref(context: Context) {

    private val context = context.applicationContext
    private val sharedPref = this.context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getDownloadPath(): String? {
        val path = sharedPref.getString(KEY_DOWNLOAD_PATH, null) ?: return null
        try {
            val uri = path.toUri()
            val hasPermission = context.contentResolver.persistedUriPermissions.any {
                it.uri == uri && (it.isWritePermission || it.isReadPermission)
            }
            if (!hasPermission) {
                sharedPref.edit { remove(KEY_DOWNLOAD_PATH) }
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return path
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

    fun getCreateSubfolder(): Boolean {
        return sharedPref.getBoolean(KEY_CREATE_SUBFOLDER, true)
    }

    fun storeCreateSubfolder(enabled: Boolean) {
        sharedPref.edit {
            putBoolean(KEY_CREATE_SUBFOLDER, enabled)
        }
    }

    companion object {
        private const val PREFS_NAME = "spd_settings"
        private const val KEY_DOWNLOAD_PATH = "download_path"
        private const val KEY_AUTO_UPDATE_CHECK = "auto_update_check"
        private const val KEY_CREATE_SUBFOLDER = "create_subfolder"
    }
}