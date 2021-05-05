package com.supersuman.spd

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.exceptions.SpotifyWebApiException
import com.wrapper.spotify.model_objects.specification.Paging
import com.wrapper.spotify.model_objects.specification.PlaylistTrack
import org.apache.hc.core5.http.ParseException
import java.io.*
import java.net.URL
import java.net.URLConnection
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var folderpath : File
    private var title = ""
    private lateinit var spotifyApi : SpotifyApi

    private lateinit var permissionButton : Button
    private lateinit var subfolderText : EditText
    private lateinit var spotifylinkedittext : EditText
    private lateinit var songTitle : TextView
    private lateinit var songDownloadProgress : TextView
    private lateinit var downloadButton : Button
    private lateinit var progressBar : ProgressBar

    private lateinit var completedArray : MutableList<String>

    private var spotifyHasSetup = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val parent : LinearLayout = findViewById(R.id.parentview)
        initviews()

        setupSpotify()
       downloadButton.setOnClickListener {
           closeKeyboard()
           if(!checkForPermissions()){
               Snackbar.make(parent, "Please grant the permission", Snackbar.LENGTH_SHORT).show()
           }else if(subfolderText.text.isEmpty()){
               Snackbar.make(parent, "Sub Folder name can't be empty", Snackbar.LENGTH_SHORT).show()
           }else if(spotifylinkedittext.text.isEmpty()){
               Snackbar.make(parent, "No playlist link provided", Snackbar.LENGTH_SHORT).show()
           }else if (checkForPermissions() && spotifyHasSetup){
               thread {
                   createDirectory(subfolderText.text.toString())
                   val list = getPlaylistItems(spotifylinkedittext.text.toString())
                   for (i in list){
                       val videoLink = getVideoLink(i)
                       val downloadlink = getDownloadLink(videoLink)
                       title = renameTitle()
                       startDownload(downloadlink)
                   }
               }
           }else{
            Snackbar.make(parent, "What the java are you looking at", Snackbar.LENGTH_SHORT).show()
            }
        }


        permissionButton.setOnClickListener {
            closeKeyboard()
            askForPermissions()
        }

    }

    private fun askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${applicationContext.packageName}")
            startActivity(intent)
        }
    }

    private  fun checkForPermissions() : Boolean{

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Environment.isExternalStorageManager()
        }else{
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )== PackageManager.PERMISSION_GRANTED
        }
    }

    private fun initviews(){
        spotifylinkedittext  = findViewById(R.id.spotifylinkedittext)
        downloadButton = findViewById(R.id.downloadButton)
        permissionButton = findViewById(R.id.permissionButton)
        progressBar = findViewById(R.id.progressBar)
        songTitle = findViewById(R.id.songTitile)
        songDownloadProgress = findViewById(R.id.songDownloadProgress)
        completedArray = ArrayList()
        subfolderText = findViewById(R.id.subfolderName)
    }

    private fun setupSpotify(){
        thread {
            try {
                spotifyApi = SpotifyApi.Builder().setClientId(getString(R.string.clientId)).setClientSecret(
                    getString(R.string.clientSecret)
                ).build()
                val clientCredentialsRequest = spotifyApi.clientCredentials().build()
                val clientCredentials = clientCredentialsRequest.execute()
                spotifyApi.accessToken = clientCredentials.accessToken
                spotifyHasSetup = true
            }catch (e:Exception){
                println(e)
            }
        }
    }

    private fun getPlaylistItems(link: String): MutableList<String> {
        val arrayList: MutableList<String> = ArrayList()
        thread {
            try {
                val playlistId= link.split("playlist/")[1].split("?")[0]
                println(playlistId)
                val getPlaylistsItemsRequests = spotifyApi.getPlaylistsItems(playlistId).build()
                var playlistTrackPaging: Paging<PlaylistTrack>?
                playlistTrackPaging = getPlaylistsItemsRequests.execute()
                for (i in 0..playlistTrackPaging!!.total / 100) {
                    try {
                        playlistTrackPaging =
                            spotifyApi.getPlaylistsItems(playlistId).offset(i * 100).build().execute()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    for (j in playlistTrackPaging!!.items.indices) {
                        arrayList.add(playlistTrackPaging.items[j].track.name)
                    }
                }
            }catch (e:Exception){
                println(e)
            }
        }.join()
        println("Gathered playlist info")
        return arrayList
    }

    private fun createDirectory(subfoldername: String) {
        val newFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "SPD/$subfoldername"
        )
        newFolder.mkdirs()
        folderpath = newFolder

    }

    private fun getVideoLink(searchQuery: String): String {
        var videoLink=""
        thread {
            try {
                val temp  = khttp.get("https://www.youtube.com/results?search_query=$searchQuery full audio")
                val videoID = temp.text.split("estimatedResults")[1].split("videoId\":\"")[1].split(
                    "\""
                )[0]
                videoLink = "https://youtu.be/$videoID"
            }catch (e:Exception){
                println(e)
            }
        }.join()
        return videoLink
    }

    private fun  getDownloadLink(videoLink : String): String {
        var downloadlink=""
        thread {
            try {
                val s= khttp.get("https://yt1s.com/youtube-to-mp3/en2")
                val s1= khttp.post(
                    "https://yt1s.com/api/ajaxSearch/index", cookies = s.cookies, data = mapOf(
                        "vt" to "mp3",
                        "q" to videoLink
                    ), allowRedirects = true
                )
                title = s1.text.split("title\":\"")[1].split("\"")[0]
                runOnUiThread {
                    songTitle.text = title
                    songDownloadProgress.text = "0"
                    progressBar.progress = 0
                }
                val videoID = s1.text.split("vid\":\"")[1].split("\"")[0]
                val s2 = khttp.post(
                    "https://yt1s.com/api/ajaxConvert/convert", cookies = s.cookies, data = mapOf(
                        "k" to s1.text.split(
                            "kc\":\""
                        )[1].split("\"")[0], "vid" to videoID
                    )
                )
                val dlink = s2.text.split("dlink\":\"")[1].split("\"")[0].replace("\\", "")
                downloadlink = dlink
            }catch (e: Exception){
                println(e)
            }
        }.join()
        return downloadlink
    }

    private fun renameTitle(): String {
        val allowedChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz{}[]()!@#$%^&-_=+;' "
        var newtitle = ""
        for (i in title){
            if(i in allowedChar){
                newtitle+=i
            }
        }
        return newtitle
    }

    private fun startDownload(downloadlink: String){
        thread {
            try {
                var count: Int
                val url = URL(downloadlink)
                val connection: URLConnection = url.openConnection()
                connection.connect()
                val lengthOfFile: Int = connection.contentLength
                val input: InputStream = BufferedInputStream(url.openStream())
                val output: OutputStream = FileOutputStream(File(folderpath, "$title.mp3"))
                val data = ByteArray(1024)
                var total: Long = 0
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    val downloadedprogress = "" + (total * 100 / lengthOfFile).toInt()
                    output.write(data, 0, count)
                    if (downloadedprogress == "100"){
                        println("$title Download Complete")
                    }
                    runOnUiThread {
                        progressBar.progress = downloadedprogress.toInt()
                        songDownloadProgress.text = "$downloadedprogress%"
                    }
                }
                output.flush()
                output.close()
                input.close()
            }catch (e: Exception) {
                println(e)
            }
        }.join()
    }

    private fun closeKeyboard() {
        val view: View? = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}
