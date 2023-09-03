package com.supersuman.spd

import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        println(getVideoLink("laude lag gaye"))

    }
    fun getVideoLink(query : String): String? {
        val requests = Requests()
        val response = requests.get("https://music.youtube.com/search?q=${query.replace(" ", "+")}")!!
        val jsonString = response.split("JSON.parse('\\x7b\\x22query\\x22")[1].split("data:")[1].split("'")[1]

        val replace = jsonString
            .replace("\\x22","\"")

        val y = replace.split("musicShelfRenderer")
        val z = y.subList(1,3)
        for (i in z){
            val category = i.split("text\"")[1].split("\"")[1]
            val type = i.split("\"musicVideoType\"")[1].split("\"")[1]
            val videoId = i.split("\"videoId\"")[1].split("\"")[1]
            if ("result" in category && type == "MUSIC_VIDEO_TYPE_ATV" ){
                return "https://music.youtube.com/watch?v=$videoId"
            }
            if ("songs" in category){
                return "https://music.youtube.com/watch?v=$videoId"
            }
        }
        return null
    }

}