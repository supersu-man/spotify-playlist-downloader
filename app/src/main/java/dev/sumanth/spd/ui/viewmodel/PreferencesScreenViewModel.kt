package dev.sumanth.spd.ui.viewmodel

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel

class PreferencesScreenViewModel: ViewModel() {


    fun getDownloadPath(context: Context) {
        val sharedPref = context.getSharedPreferences(context.packageName, MODE_PRIVATE)
        val downloadPath = sharedPref.getString("download_path", null)
    }
}