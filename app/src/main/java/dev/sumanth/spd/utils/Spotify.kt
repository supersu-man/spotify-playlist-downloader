package dev.sumanth.spd.utils

import android.content.Context
import dev.sumanth.spd.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.michaelthelin.spotify.SpotifyApi

lateinit var spotify: SpotifyManager

class SpotifyManager(private val context: Context) {

    var spotifyApi: SpotifyApi = SpotifyApi.Builder()
        .setClientId(context.getString(R.string.CLIENT_ID))
        .setClientSecret(context.getString(R.string.CLIENT_SECRET))
        .build()
        private set

    suspend fun init() = withContext(Dispatchers.IO) {
        try {
            val clientCredentials = spotifyApi.clientCredentials().build().execute()
            spotifyApi.accessToken = clientCredentials.accessToken
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
