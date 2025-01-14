package dev.sumanth.spd.ui.viewmodel

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.sumanth.spd.model.defaultDownloadPath
import dev.sumanth.spd.model.download_path_key


class PreferencesScreenViewModel(context: Context): ViewModel() {


    lateinit var launcher: ManagedActivityResultLauncher<Intent, ActivityResult>

    private val sharedPref = context.getSharedPreferences(context.packageName, MODE_PRIVATE)
    var downloadPath by mutableStateOf(sharedPref.getString(download_path_key, defaultDownloadPath).toString())

    private val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
    }

    fun storePath(path: String) {
        sharedPref.edit().putString(download_path_key, path).apply()
    }

    fun openDocumentTree() {
        launcher.launch(intent)
    }

}