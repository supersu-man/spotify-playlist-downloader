package com.supersuman.spd


import com.ajts.androidmads.youtubemp3.YTubeMp3Service
import com.github.kotvertolet.youtubejextractor.JExtractorCallback
import com.github.kotvertolet.youtubejextractor.YoutubeJExtractor
import com.github.kotvertolet.youtubejextractor.exception.YoutubeRequestException
import com.github.kotvertolet.youtubejextractor.models.newModels.VideoPlayerConfig
import com.github.kotvertolet.youtubejextractor.models.youtube.videoData.YoutubeVideoData
import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val s= khttp.get("https://yt1s.com/youtube-to-mp3/en2")
        val s1= khttp.post("https://yt1s.com/api/ajaxSearch/index",cookies = s.cookies, data = mapOf("vt" to "mp3", "q" to "https://www.youtube.com/watch?v=TUVcZfQe-Kw"),allowRedirects = true)
        val s2 = khttp.post("https://yt1s.com/api/ajaxConvert/convert",cookies = s.cookies,data = mapOf("k" to s1.text.split("kc\":\"")[1].split("\"")[0], "vid" to "TUVcZfQe-Kw"))
        val dlink = s2.text.split("dlink\":\"")[1].split("\"")[0].replace("\\","")
        println(dlink)
        khttp.get(dlink)
    }

}