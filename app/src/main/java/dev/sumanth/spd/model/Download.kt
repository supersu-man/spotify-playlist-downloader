package dev.sumanth.spd.model

import android.os.Environment
import java.io.File

private val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
val defaultDownloadPath: String = File(downloadsDirectory, "spotify-playlist-downloader").path

const val download_path_key = "download_path"
var downloadPath = ""