package dev.sumanth.spd.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.supersuman.apkupdater.ApkUpdater

class UpdaterViewModel: ViewModel() {
    var updateFound by mutableStateOf(false)
    var updater: ApkUpdater? = null
}