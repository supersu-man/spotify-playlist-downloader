package com.supersuman.spd

import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.io.*
import java.net.URL
import java.net.URLConnection
import kotlin.concurrent.thread

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.supersuman.spd", appContext.packageName)

        try {
            YoutubeDL.getInstance().init(appContext.applicationContext)
        } catch (e: YoutubeDLException) {

        }

        val request = YoutubeDLRequest("https://www.youtube.com/watch?v=hep4en7Wq0I")
        request.addOption("-f", "best")
        val streamInfo = YoutubeDL.getInstance().getInfo(request)
        println(streamInfo.url)
    }
}