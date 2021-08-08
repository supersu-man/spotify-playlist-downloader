package com.supersuman.spd

import okhttp3.JavaNetCookieJar
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

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

    fun post(uri: String, multipartBody: MultipartBody): String? {
        try {
            val request = Request.Builder().url(uri).post(multipartBody).build()
            val response = httpClient.newCall(request).execute().body?.string()
            return response
        }catch (e : SocketTimeoutException){
            return null
        }
    }

    fun get(uri: String): String? {
        try {
            val request = Request.Builder().url(uri).build()
            val response = httpClient.newCall(request).execute().body?.string()
            return response
        } catch (e:SocketTimeoutException){
            return null
        }
    }

    fun isInternetConnection(): Boolean {
        var returnVal = false
        thread {
            returnVal = try {
                get("https://www.google.com/")
                true
            }catch (e:Exception){
                false
            }
        }.join()
        return returnVal
    }
}