package com.supersuman.spd


import com.example.gitamite.Requests
import okhttp3.MultipartBody
import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val requests = Requests()
        val s= requests.get("https://yt1s.com/youtube-to-mp3/en2")
        val s1= requests.post("https://yt1s.com/api/ajaxSearch/index", getmapped())!!
        println(s1)
        val videoID = s1.split("vid\":\"")[1].split("\"")[0]
        val k = s1.split("kc\":\"")[1].split("\"")[0]
        //val s2 = requests.post("https://yt1s.com/api/ajaxConvert/convert", getmapped2(k,videoID))!!
        //val downloadLink= s2.split("dlink\":\"")[1].split("\"")[0].replace("\\", "")
    }
    fun getmapped() : MultipartBody {
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("q", "https://www.youtube.com/watch?v=Dy3u2IUAbtY")
            .addFormDataPart("vt","mp3")
            .build()
        return requestBody
    }

}