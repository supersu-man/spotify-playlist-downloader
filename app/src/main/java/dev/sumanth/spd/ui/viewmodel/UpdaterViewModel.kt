package dev.sumanth.spd.ui.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supersuman.apkupdater.ApkUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdaterViewModel(activity: Activity, url: String): ViewModel() {
    var updateFound by mutableStateOf(false)
    lateinit var updater: ApkUpdater

    init {
        checkForUpdate(activity, url)
    }

    private fun checkForUpdate(activity: Activity, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updater = ApkUpdater(activity, url)
            updater.threeNumbers = true
            if (updater.isInternetConnection() && updater.isNewUpdateAvailable() == true) {
                updateFound = true
            }
        }
    }

}