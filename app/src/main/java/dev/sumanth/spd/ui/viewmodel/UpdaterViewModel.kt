package dev.sumanth.spd.ui.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supersuman.apkupdater.ApkUpdater
import dev.sumanth.spd.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdaterViewModel : ViewModel() {
    var updateFound by mutableStateOf(false)
    var updater: ApkUpdater? = null

    fun checkForUpdate(context: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updater = ApkUpdater(context, context.getString(R.string.update_url))
                updater?.threeNumbers = true
                if (updater?.isNewUpdateAvailable() == true) {
                    updateFound = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}