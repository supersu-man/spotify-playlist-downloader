package dev.sumanth.spd.utils

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.schabi.newpipe.extractor.ServiceList.YouTube
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMusicSearchExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import se.michaelthelin.spotify.model_objects.specification.Paging
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class Downloader private constructor(builder: OkHttpClient.Builder) : Downloader() {
    private val client: OkHttpClient = builder.readTimeout(30, TimeUnit.SECONDS).build()

    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()
        var requestBody: RequestBody? = null
        if (dataToSend != null) {
            requestBody = dataToSend.toRequestBody()
        }
        val requestBuilder: okhttp3.Request.Builder = okhttp3.Request.Builder().method(httpMethod, requestBody).url(url).addHeader("User-Agent", USER_AGENT)
        for ((headerName, headerValueList) in headers) {
            if (headerValueList.size > 1) {
                requestBuilder.removeHeader(headerName)
                for (headerValue in headerValueList) {
                    requestBuilder.addHeader(headerName, headerValue)
                }
            } else if (headerValueList.size == 1) {
                requestBuilder.header(headerName, headerValueList[0])
            }
        }
        val response: okhttp3.Response = client.newCall(requestBuilder.build()).execute()
        if (response.code == 429) {
            response.close()
            throw ReCaptchaException("reCaptcha Challenge requested", url)
        }
        val body: ResponseBody? = response.body
        var responseBodyToReturn: String? = null
        if (body != null) {
            responseBodyToReturn = body.string()
        }
        val latestUrl: String = response.request.url.toString()
        return Response(
            response.code, response.message, response.headers.toMultimap(), responseBodyToReturn, latestUrl
        )
    }

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0"
        private var instance: Downloader? = null

        /**
         * It's recommended to call exactly once in the entire lifetime of the application.
         *
         * @param builder if null, default builder will be used
         * @return a new instance of [DownloaderTestImpl]
         */
        fun init(builder: OkHttpClient.Builder?): Downloader? {
            instance = Downloader(
                builder ?: OkHttpClient.Builder()
            )
            return instance
        }

        fun getInstance(): Downloader? {
            if (instance == null) {
                init(null)
            }
            return instance
        }
    }
}

fun getPlaylistItems(link: String): MutableList<String> {
    val arrayList: MutableList<String> = ArrayList()
    try {
        val playlistId = link.split("playlist/")[1].split("?")[0]
        val getPlaylistsItemsRequests = spotify.spotifyApi.getPlaylistsItems(playlistId).build()
        var playlistTrackPaging: Paging<PlaylistTrack>?
        playlistTrackPaging = getPlaylistsItemsRequests.execute()
        for (i in 0..playlistTrackPaging!!.total / 100) {
            playlistTrackPaging = spotify.spotifyApi.getPlaylistsItems(playlistId).offset(i * 100).build().execute()
            for (j in playlistTrackPaging!!.items.indices) {
                val temp = playlistTrackPaging.items[j].track
                val songName = temp.name
                val artistName =
                    temp.toString().split("artists=")[1].split("name=")[1].split(",")[0]
                arrayList.add("$songName by $artistName")
            }
        }
    } catch (e: Exception) {
        println(e.message)
    }
    return arrayList
}

fun getFileMeta(text: String): HashMap<String, String> {
    val extra = YouTube.getSearchExtractor(
        text,
        listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS),
        null
    ) as YoutubeMusicSearchExtractor
    extra.fetchPage()
    val mediaLink = extra.initialPage.items[0].url
    val extractor = YouTube.getStreamExtractor(mediaLink)
    extractor.fetchPage()
    extractor.audioStreams.sortByDescending { it.bitrate }
    val url = extractor.audioStreams[0].content
    return hashMapOf("url" to url, "filename" to "${extractor.name}.${extractor.audioStreams[0].format}")
}

fun downloadFile(link: String, path: String, progress: ((Long, Long) -> Unit)? = null) {
    val request = okhttp3.Request.Builder().addHeader("Range", "bytes=0-").url(link).build()
    val response = OkHttpClient().newCall(request).execute()
    val body = response.body
    val responseCode = response.code
    if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE && body != null) {
        val length = body.contentLength()
        body.byteStream().apply {
            FileOutputStream(File(path)).use { output ->
                var bytesCopied = 0L
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = read(buffer)
                while (bytes >= 0) {
                    output.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    progress?.invoke(bytesCopied, length)
                    bytes = read(buffer)
                }
            }
        }
    }
}
