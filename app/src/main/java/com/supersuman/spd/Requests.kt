package com.supersuman.spd

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.SocketTimeoutException

val coroutineScope = CoroutineScope(Dispatchers.IO)

class Requests {

    private var cookieManager: CookieHandler
    private var httpClient : OkHttpClient

    init {
        try {
            cookieManager = CookieHandler.getDefault()
        } catch (e:Exception){
            cookieManager = CookieManager()
            (cookieManager as CookieManager).setCookiePolicy(CookiePolicy.ACCEPT_ALL)
            CookieManager.setDefault(cookieManager)
        }
        httpClient = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addNetworkInterceptor { chain ->
                chain.proceed(
                    chain.request()
                        .newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.94 Safari/537.36")
                        .build()
                )
            }
            .build()
    }

    fun clearCookieManager(){
        (cookieManager as CookieManager).cookieStore.removeAll()
        CookieManager.setDefault(null)
    }

    suspend fun get(uri: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(uri).build()
                return@withContext httpClient.newCall(request).execute().body?.string().toString()
            } catch (e: SocketTimeoutException) {
                return@withContext null
            }
        }
    }

    suspend fun isInternetConnection(): Boolean {
        return try {
            get("https://www.google.com/")
            true
        } catch (e:Exception){
            false
        }
    }
}