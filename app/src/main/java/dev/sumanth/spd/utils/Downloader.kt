package dev.sumanth.spd.utils

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.Level
import com.arthenica.ffmpegkit.ReturnCode
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.ServiceList.YouTube
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMusicSearchExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import se.michaelthelin.spotify.model_objects.specification.Track
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

data class FileMeta(val url: String, val name: String, val extention: String)

object DownloadManager {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getPlaylistItems(link: String): List<Track> {
        val songs = mutableListOf<Track>()
        try {
            val playlistId = link.split("playlist/")[1].split("?")[0]
            var offset = 0
            var hasMore = true

            while (hasMore) {
                val paging = spotify.spotifyApi.getPlaylistsItems(playlistId)
                    .offset(offset)
                    .limit(100)
                    .build()
                    .execute()

                paging.items.forEach { item ->
                    val track = item.track as Track
                    songs.add(track)
                }

                offset += paging.items.size
                hasMore = offset < paging.total
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songs
    }

    fun getFileMeta(track: Track): FileMeta {
        val extra = YouTube.getSearchExtractor(
            track.name + " " + track.artists.joinToString(" ") { it.name },
            listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS),
            null
        ) as YoutubeMusicSearchExtractor
        extra.fetchPage()

        val mediaLink = extra.initialPage.items.firstOrNull()?.url
            ?: throw IOException("No search results found for ${track.name}")

        val extractor = YouTube.getStreamExtractor(mediaLink)
        extractor.fetchPage()

        val bestStream = extractor.audioStreams.maxByOrNull { it.bitrate }
            ?: throw IOException("No audio streams found for ${track.name}")

        return FileMeta(url = bestStream.content, name = extractor.name, extention = bestStream.format?.suffix ?: "m4a")
    }

    fun downloadFile(url: String, path: String, onProgress: ((Long, Long) -> Unit)? = null) {
        val request = okhttp3.Request.Builder()
            .url(url)
            .addHeader("Range", "bytes=0-")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val body = response.body ?: throw IOException("Empty response body")
            val totalSize = body.contentLength()

            File(path).parentFile?.mkdirs()

            body.byteStream().use { input ->
                FileOutputStream(File(path)).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        onProgress?.invoke(totalRead, totalSize)
                    }
                }
            }
        }
    }

    fun convertToMp3(filePath: String, ext: String) {
        val inputPath = "$filePath.$ext"
        val outputPath = "$filePath.mp3"
        if(ext == "mp3") return

        val command = "-i \"$inputPath\" -vn -ab 192k -ar 44100 -y \"$outputPath\""

        FFmpegKitConfig.setLogLevel(Level.AV_LOG_QUIET)
        val session = FFmpegKit.execute(command)

        if (ReturnCode.isSuccess(session.returnCode)) {
            File(inputPath).delete()
        }
    }
}

class NewPipeDownloader private constructor() : Downloader() {
    
    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()
        
        var requestBody = dataToSend?.toRequestBody()

        if (requestBody == null && (httpMethod == "POST" || httpMethod == "PUT" || httpMethod == "PATCH")) {
            requestBody = "".toRequestBody()
        }

        val requestBuilder = okhttp3.Request.Builder()
            .method(httpMethod, requestBody)
            .url(url)
            .addHeader("User-Agent", USER_AGENT)

        headers.forEach { (name, values) ->
            values.forEach { value ->
                requestBuilder.addHeader(name, value)
            }
        }

        val response = DownloadManager.client.newCall(requestBuilder.build()).execute()
        
        if (response.code == 429) {
            response.close()
            throw ReCaptchaException("reCaptcha Challenge requested", url)
        }

        val bodyString = response.body?.string() ?: ""
        val latestUrl = response.request.url.toString()
        
        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            bodyString,
            latestUrl
        )
    }

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        private var instance: NewPipeDownloader? = null

        fun getInstance(): NewPipeDownloader {
            if (instance == null) {
                instance = NewPipeDownloader()
            }
            return instance!!
        }
    }
}
