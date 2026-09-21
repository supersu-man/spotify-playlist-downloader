package dev.sumanth.spd.utils

import android.util.Base64
import android.util.Log
import dev.sumanth.spd.model.DownloadStatus
import dev.sumanth.spd.model.Track
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min
import kotlin.math.pow

class SpotifyScraper(private val client: OkHttpClient = OkHttpClient()) {

    companion object {
        private const val TAG = "SpotifyScraper"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        private val FALLBACK_SECRETS = listOf(
            SpotifySecret(",7/*F(\"rLJ2oxaKL^f+E1xvP@N", 61),
            SpotifySecret("OmE{ZA.J^\":0FG\\Uz?[@WW", 60),
            SpotifySecret("{iOFn;4}<1PFYKPV?5{%u14]M>/V0hDH", 59)
        )
        private const val FALLBACK_QUERY_HASH = "86dde7b9d9356e2369414647cf6950cfed96e778e129cfdfc99aea6c1613b3b0"
    }

    data class SpotifySecret(val secret: String, val version: Int)

    private var cachedSecrets: List<SpotifySecret>? = null
    private val cachedHashes = mutableMapOf<String, String>()
    private var accessToken: String? = null
    private var tokenExpiration: Long = 0
    private var cookies: String = ""

    private val cookieStore = HashMap<String, MutableList<Cookie>>()

    private val internalClient = client.newBuilder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                val host = url.host
                val existing = cookieStore[host] ?: mutableListOf()
                for (cookie in cookies) {
                    existing.removeAll { it.name == cookie.name }
                    existing.add(cookie)
                }
                cookieStore[host] = existing
                Log.d(TAG, "CookieJar saved ${cookies.size} cookies for host: $host")
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val host = url.host
                val loaded = cookieStore[host] ?: emptyList()
                Log.d(TAG, "CookieJar loaded ${loaded.size} cookies for host: $host")
                return loaded
            }
        })
        .build()

    fun parsePlaylistId(input: String): String {
        val regex = Regex("playlist[/:]([a-zA-Z0-9]{22})")
        val match = regex.find(input)
        if (match != null) return match.groupValues[1]
        
        val idRegex = Regex("^[a-zA-Z0-9]{22}$")
        if (idRegex.matches(input.trim())) return input.trim()
        
        throw IllegalArgumentException("Unable to extract Spotify playlist ID from: $input")
    }

    private fun generateTOTP(secretStr: String, timestamp: Long = System.currentTimeMillis()): String {
        val period = 30
        val digits = 6

        // 1. Transform secret string using Spotify's XOR cipher
        val r = secretStr.mapIndexed { i, c -> (c.code xor ((i % 33) + 9)) }
        val joinedStr = r.joinToString("")
        val secretBytes = joinedStr.toByteArray(StandardCharsets.UTF_8)

        // 2. Compute 8-byte big-endian counter
        val counter = timestamp / 1000 / period
        val counterBuf = ByteBuffer.allocate(8).putLong(counter).array()

        // 3. HMAC-SHA1
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(secretBytes, "HmacSHA1"))
        val hmac = mac.doFinal(counterBuf)

        // 4. Dynamic Truncation
        val offset = hmac[hmac.size - 1].toInt() and 0x0f
        val binary = ((hmac[offset].toInt() and 0x7f) shl 24) or
                     ((hmac[offset + 1].toInt() and 0xff) shl 16) or
                     ((hmac[offset + 2].toInt() and 0xff) shl 8) or
                     (hmac[offset + 3].toInt() and 0xff)

        val otp = binary % 10.0.pow(digits.toDouble()).toInt()
        return otp.toString().padStart(digits, '0')
    }

    private fun fetchInitialSession(playlistId: String): SessionInfo {
        val url = "https://open.spotify.com/playlist/$playlistId"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        Log.d(TAG, "fetchInitialSession: Fetching initial session from $url")
        internalClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to fetch initial session: ${response.code}")
            
            val html = response.body?.string() ?: ""
            val cookieHeaders = response.headers("Set-Cookie")
            cookies = cookieHeaders.joinToString("; ") { it.split(";")[0] }
            Log.d(TAG, "fetchInitialSession: Set-Cookie headers size = ${cookieHeaders.size}")

            var serverTime: Long? = null
            var clientVersion = "1.3.2.52.g20a9e685"

            val cfgRegex = Regex("<script[^>]*id=['\"]appServerConfig['\"][^>]*>([A-Za-z0-9+/=\\s]+)</script>")
            val cfgMatch = cfgRegex.find(html)
            if (cfgMatch != null) {
                try {
                    val base64Str = cfgMatch.groupValues[1].replace("\\s".toRegex(), "")
                    val decoded = String(Base64.decode(base64Str, Base64.DEFAULT), StandardCharsets.UTF_8)
                    val json = JSONObject(decoded)
                    if (json.has("serverTime")) {
                        serverTime = json.getLong("serverTime")
                        Log.d(TAG, "fetchInitialSession: Found serverTime = $serverTime")
                    }
                    if (json.has("clientVersion")) {
                        clientVersion = json.getString("clientVersion")
                        Log.d(TAG, "fetchInitialSession: Found clientVersion = $clientVersion")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse appServerConfig", e)
                }
            } else {
                Log.d(TAG, "fetchInitialSession: appServerConfig script block not found via regex")
            }

            if (cachedSecrets == null || !cachedHashes.containsKey("fetchPlaylistContents")) {
                Log.d(TAG, "fetchInitialSession: cachedSecrets or fetchPlaylistContents hash missing. Triggering bundle config extraction.")
                extractBundleConfig(html)
            }

            return SessionInfo(serverTime, clientVersion)
        }
    }

    private fun extractBundleConfig(html: String) {
        try {
            val scriptRegex = Regex("src=['\"](https://open\\.spotifycdn\\.com/cdn/build/web-player/web-player\\.[a-f0-9]+\\.js)['\"]")
            val scriptMatch = scriptRegex.find(html)
            if (scriptMatch == null) {
                Log.d(TAG, "extractBundleConfig: Web player JS bundle URL not found in HTML, applying fallbacks")
                useFallbacks()
                return
            }

            val jsUrl = scriptMatch.groupValues[1]
            Log.d(TAG, "extractBundleConfig: Fetching JS bundle from $jsUrl")
            val jsRequest = Request.Builder().url(jsUrl).build()
            internalClient.newCall(jsRequest).execute().use { response ->
                val jsText = response.body?.string() ?: ""
                Log.d(TAG, "extractBundleConfig: JS bundle loaded successfully, size = ${jsText.length}")

                // Extract secrets
                val ejRegex = Regex("let\\s+[a-zA-Z0-9_$]+\\s*=\\s*(\\[\\s*\\{secret:[\\s\\S]*?\\}\\s*\\])\\.map")
                val ejMatch = ejRegex.find(jsText)
                if (ejMatch != null) {
                    val objRegex = Regex("\\{secret:\\s*(['\"])((?:\\\\.|(?!\\1)[^\\\\])*)\\1\\s*,\\s*version:\\s*(\\d+)\\s*\\}")
                    val matches = objRegex.findAll(ejMatch.groupValues[1])
                    val list = matches.map { SpotifySecret(it.groupValues[2], it.groupValues[4].toInt()) }.toList()
                    if (list.isNotEmpty()) {
                        cachedSecrets = list
                        Log.d(TAG, "extractBundleConfig: Successfully extracted ${list.size} secrets dynamically from bundle")
                    } else {
                        Log.d(TAG, "extractBundleConfig: Secrets match found but individual secret list is empty")
                    }
                } else {
                    Log.d(TAG, "extractBundleConfig: Failed to match secrets array regex in JS bundle")
                }

                // Extract hashes
                val contentsHashRegex = Regex("\"fetchPlaylistContents\"\\s*,\\s*\"query\"\\s*,\\s*\"([a-f0-9]{64})\"")
                contentsHashRegex.find(jsText)?.let {
                    cachedHashes["fetchPlaylistContents"] = it.groupValues[1]
                    Log.d(TAG, "extractBundleConfig: Extracted fetchPlaylistContents hash = ${it.groupValues[1]}")
                }

                val metaHashRegex = Regex("\"fetchPlaylistMetadata\"\\s*,\\s*\"query\"\\s*,\\s*\"([a-f0-9]{64})\"")
                metaHashRegex.find(jsText)?.let {
                    cachedHashes["fetchPlaylistMetadata"] = it.groupValues[1]
                    Log.d(TAG, "extractBundleConfig: Extracted fetchPlaylistMetadata hash = ${it.groupValues[1]}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse bundle dynamically, using fallbacks", e)
        }

        if (cachedSecrets == null) {
            Log.d(TAG, "extractBundleConfig: Falling back to static secrets")
            cachedSecrets = FALLBACK_SECRETS
        }
        if (!cachedHashes.containsKey("fetchPlaylistContents")) {
            Log.d(TAG, "extractBundleConfig: Falling back to static fetchPlaylistContents hash")
            cachedHashes["fetchPlaylistContents"] = FALLBACK_QUERY_HASH
        }
        if (!cachedHashes.containsKey("fetchPlaylistMetadata")) {
            Log.d(TAG, "extractBundleConfig: Falling back to static fetchPlaylistMetadata hash")
            cachedHashes["fetchPlaylistMetadata"] = FALLBACK_QUERY_HASH
        }
    }

    private fun useFallbacks() {
        Log.d(TAG, "useFallbacks: Loading static fallback secrets and query hashes")
        cachedSecrets = FALLBACK_SECRETS
        cachedHashes["fetchPlaylistContents"] = FALLBACK_QUERY_HASH
        cachedHashes["fetchPlaylistMetadata"] = FALLBACK_QUERY_HASH
    }

    private fun getAccessToken(playlistId: String): String {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiration - 60000) {
            Log.d(TAG, "getAccessToken: Returning non-expired cached access token")
            return accessToken!!
        }

        Log.d(TAG, "getAccessToken: Refreshing access token for playlistId = $playlistId")
        val session = fetchInitialSession(playlistId)
        val activeSecret = cachedSecrets!![0]

        val now = System.currentTimeMillis()
        val totp = generateTOTP(activeSecret.secret, now)
        val totpServer = if (session.serverTime != null) {
            generateTOTP(activeSecret.secret, session.serverTime * 1000)
        } else {
            "unavailable"
        }

        Log.d(TAG, "getAccessToken: Preparing token request with secret version = ${activeSecret.version}, totp = $totp, totpServer = $totpServer")

        val url = "https://open.spotify.com/api/token".toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("reason", "init")
            .addQueryParameter("productType", "web_player")
            .addQueryParameter("totp", totp)
            .addQueryParameter("totpServer", totpServer)
            .addQueryParameter("totpVer", activeSecret.version.toString())
            .build()

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Referer", "https://open.spotify.com/playlist/$playlistId")
            .header("Accept", "application/json")
            .header("spotify-app-version", session.clientVersion)
            .header("app-platform", "WebPlayer")
            .build()

        internalClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e(TAG, "getAccessToken: Error response code = ${response.code}, body = $errorBody")
                throw Exception("Failed to obtain access token: ${response.code}, body: $errorBody")
            }
            val responseBody = response.body?.string() ?: "{}"
            Log.d(TAG, "getAccessToken: Successfully received response body: $responseBody")
            val data = JSONObject(responseBody)
            accessToken = data.getString("accessToken")
            tokenExpiration = data.optLong("accessTokenExpirationTimestampMs", System.currentTimeMillis() + 3600000)
            return accessToken!!
        }
    }

    fun getPlaylistMetadata(playlistId: String): PlaylistMetadata {
        val token = getAccessToken(playlistId)
        val hash = cachedHashes["fetchPlaylistMetadata"] ?: FALLBACK_QUERY_HASH

        val variables = JSONObject().apply {
            put("uri", "spotify:playlist:$playlistId")
            put("offset", 0)
            put("limit", 25)
            put("enableWatchFeedEntrypoint", false)
        }

        val extensions = JSONObject().apply {
            put("persistedQuery", JSONObject().apply {
                put("version", 1)
                put("sha256Hash", hash)
            })
        }

        val url = "https://api-partner.spotify.com/pathfinder/v1/query".toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("operationName", "fetchPlaylistMetadata")
            .addQueryParameter("variables", variables.toString())
            .addQueryParameter("extensions", extensions.toString())
            .build()

        Log.d(TAG, "getPlaylistMetadata: Querying metadata from $url")
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("User-Agent", USER_AGENT)
            .header("app-platform", "WebPlayer")
            .header("Accept", "application/json")
            .build()

        internalClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e(TAG, "getPlaylistMetadata: Error code = ${response.code}, body = $errorBody")
                throw Exception("Failed to fetch metadata: ${response.code}, body: $errorBody")
            }
            val json = JSONObject(response.body?.string() ?: "{}")
            val p = json.optJSONObject("data")?.optJSONObject("playlistV2") ?: throw Exception("Playlist not found")

            val name = p.getString("name")
            val totalCount = p.optJSONObject("content")?.optInt("totalCount") ?: 0
            val ownerName = p.optJSONObject("ownerV2")?.optJSONObject("data")?.optString("name")
            val followers = p.optInt("followers", 0)
            val description = p.optString("description", "")
            
            Log.d(TAG, "getPlaylistMetadata: Found playlist name = $name, totalCount = $totalCount, owner = $ownerName")

            return PlaylistMetadata(
                id = playlistId,
                name = name,
                totalCount = totalCount,
                ownerName = ownerName,
                followers = followers,
                description = description
            )
        }
    }

    fun scrapePlaylist(playlistUrlOrId: String, onProgress: (List<Track>, Int) -> Unit): ScrapeResult {
        val playlistId = parsePlaylistId(playlistUrlOrId)
        val metadata = getPlaylistMetadata(playlistId)
        val totalCount = metadata.totalCount
        val tracks = mutableListOf<Track>()
        var offset = 0
        val pageSize = 50

        val token = getAccessToken(playlistId)
        val hash = cachedHashes["fetchPlaylistContents"] ?: FALLBACK_QUERY_HASH

        Log.d(TAG, "scrapePlaylist: Starting tracks scrape for playlistId = $playlistId, totalCount = $totalCount")

        while (offset < totalCount || (offset == 0 && totalCount == 0)) {
            val limit = if (totalCount > 0) min(pageSize, totalCount - offset) else pageSize
            if (limit <= 0 && offset > 0) break

            val variables = JSONObject().apply {
                put("uri", "spotify:playlist:$playlistId")
                put("offset", offset)
                put("limit", limit)
                put("includeEpisodeContentRatingsV2", false)
            }

            val extensions = JSONObject().apply {
                put("persistedQuery", JSONObject().apply {
                    put("version", 1)
                    put("sha256Hash", hash)
                })
            }

            val url = "https://api-partner.spotify.com/pathfinder/v1/query".toHttpUrlOrNull()!!.newBuilder()
                .addQueryParameter("operationName", "fetchPlaylistContents")
                .addQueryParameter("variables", variables.toString())
                .addQueryParameter("extensions", extensions.toString())
                .build()

            Log.d(TAG, "scrapePlaylist: Fetching tracks at offset = $offset, limit = $limit")
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .header("User-Agent", USER_AGENT)
                .header("app-platform", "WebPlayer")
                .header("Accept", "application/json")
                .build()

            internalClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "scrapePlaylist: Failed to fetch tracks at offset $offset: ${response.code}, body: $errorBody")
                    throw Exception("Failed to fetch tracks at offset $offset: ${response.code}, body: $errorBody")
                }
                val json = JSONObject(response.body?.string() ?: "{}")
                val items = json.optJSONObject("data")?.optJSONObject("playlistV2")?.optJSONObject("content")?.optJSONArray("items") ?: JSONArray()
                
                Log.d(TAG, "scrapePlaylist: Received ${items.length()} items at offset $offset")
                if (items.length() == 0) return@use

                for (i in 0 until items.length()) {
                    val rawItem = items.getJSONObject(i)
                    val trackData = rawItem.optJSONObject("itemV2")?.optJSONObject("data") ?: continue
                    
                    val name = trackData.optString("name")
                    val artistsArray = trackData.optJSONObject("artists")?.optJSONArray("items") ?: JSONArray()
                    val artistsText = mutableListOf<String>()
                    for (j in 0 until artistsArray.length()) {
                        artistsText.add(artistsArray.getJSONObject(j).optJSONObject("profile")?.optString("name") ?: "")
                    }
                    
                    if (name.isNotEmpty()) {
                        val album = trackData.optJSONObject("albumOfTrack") ?: trackData.optJSONObject("album")
                        val coverArt = album?.optJSONObject("coverArt")
                        val sources = coverArt?.optJSONArray("sources")
                        val imageUrl = if (sources != null && sources.length() > 0) {
                            sources.getJSONObject(sources.length() - 1).optString("url")
                        } else null

                        tracks.add(Track(name, artistsText.joinToString(", "), DownloadStatus.IDLE, imageUrl))
                    }
                }
                
                offset += items.length()
                onProgress(tracks, totalCount)
            }
            
            if (offset >= totalCount && totalCount > 0) break
            Thread.sleep(150)
        }

        Log.d(TAG, "scrapePlaylist: Scraping completed. Scraped ${tracks.size} tracks total.")
        return ScrapeResult(metadata, tracks.size, tracks)
    }

    data class ScrapeResult(
        val metadata: PlaylistMetadata,
        val totalTracks: Int,
        val tracks: List<Track>
    )

    private data class SessionInfo(val serverTime: Long?, val clientVersion: String)
    data class PlaylistMetadata(
        val id: String, 
        val name: String, 
        val totalCount: Int,
        val ownerName: String? = null,
        val followers: Int = 0,
        val description: String? = null
    )
}
