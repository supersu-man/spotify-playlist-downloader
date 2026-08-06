package dev.sumanth.spd.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.sumanth.spd.model.AppStatus
import dev.sumanth.spd.model.Track

object DownloadState {
    var appStatus by mutableStateOf(AppStatus.IDLE)
    var currentTrackIndex by mutableIntStateOf(-1)
    val tracks = mutableStateListOf<Track>()
}
