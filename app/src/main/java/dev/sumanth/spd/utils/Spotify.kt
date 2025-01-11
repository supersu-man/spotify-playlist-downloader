package dev.sumanth.spd.utils

import android.content.Context
import dev.sumanth.spd.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import se.michaelthelin.spotify.SpotifyApi

lateinit var spotify: Spotify

class Spotify(context: Context) {

    lateinit var spotifyApi: SpotifyApi

    init {
        setupSpotify(context)
    }

    private fun setupSpotify(context: Context) = CoroutineScope(Dispatchers.IO).launch {
        spotifyApi = SpotifyApi.Builder()
            .setClientId(context.getString(R.string.CLIENT_ID))
            .setClientSecret(context.getString(R.string.CLIENT_SECRET)).build()
        val clientCredentialsRequest = spotifyApi.clientCredentials().build()
        val clientCredentials = clientCredentialsRequest.execute()
        spotifyApi.accessToken = clientCredentials.accessToken
    }
}