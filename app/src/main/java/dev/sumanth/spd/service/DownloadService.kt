package dev.sumanth.spd.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dev.sumanth.spd.MainActivity
import dev.sumanth.spd.model.AppStatus
import dev.sumanth.spd.model.DownloadStatus
import dev.sumanth.spd.utils.DownloadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DownloadService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var downloadJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "download_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_DOWNLOAD_PATH = "EXTRA_DOWNLOAD_PATH"
        const val EXTRA_CONVERT_TO_MP3 = "EXTRA_CONVERT_TO_MP3"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val path = intent.getStringExtra(EXTRA_DOWNLOAD_PATH) ?: return START_NOT_STICKY
                val convert = intent.getBooleanExtra(EXTRA_CONVERT_TO_MP3, false)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(NOTIFICATION_ID, createNotification("Preparing download..."), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    startForeground(NOTIFICATION_ID, createNotification("Preparing download..."))
                }
                
                startDownloads(path, convert)
            }
            ACTION_STOP -> {
                stopDownloads()
            }
        }
        return START_NOT_STICKY
    }

    private fun startDownloads(downloadPath: String, convertToMp3: Boolean) {
        downloadJob?.cancel()
        downloadJob = serviceScope.launch {
            DownloadState.appStatus = AppStatus.DOWNLOADING
            DownloadState.tracks.forEachIndexed { i, track ->
                if (track.status == DownloadStatus.COMPLETE) return@forEachIndexed

                DownloadState.currentTrackIndex = i
                DownloadState.tracks[i] = track.copy(status = DownloadStatus.DOWNLOADING)
                
                updateNotification("Downloading: ${track.title}")

                try {
                    val fileMeta = DownloadManager.getFileMeta(track.title, track.artist)
                    DownloadManager.downloadFile(
                        this@DownloadService,
                        fileMeta.url,
                        downloadPath,
                        sanitizeFilename(track.title),
                        fileMeta.extention,
                        convertToMp3,
                        track.artist
                    )
                    DownloadState.tracks[i] = track.copy(status = DownloadStatus.COMPLETE)
                } catch (e: Exception) {
                    e.printStackTrace()
                    DownloadState.tracks[i] = track.copy(status = DownloadStatus.FAILED)
                }
            }
            DownloadState.appStatus = AppStatus.DOWNLOADING_COMPLETE
            DownloadState.currentTrackIndex = -1
            stopForeground(true)
            stopSelf()
        }
    }

    private fun stopDownloads() {
        downloadJob?.cancel()
        DownloadState.appStatus = AppStatus.SCRAPING_COMPLETE
        stopForeground(true)
        stopSelf()
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Spotify Downloader")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification(content))
    }
}
