package com.supersuman.spd

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.gitamite.Requests
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.Paging
import com.wrapper.spotify.model_objects.specification.PlaylistTrack
import okhttp3.MultipartBody
import java.io.*
import java.net.URL
import java.net.URLConnection
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var folderpath : File
    private lateinit var spotifyApi : SpotifyApi
    private var title = ""
    private var videoLink = ""
    private var query = ""
    private var downloadLink = ""
    private var queryNumber = 0

    private lateinit var permissionButton : Button
    private lateinit var subfolderText : EditText
    private lateinit var spotifylinkedittext : EditText
    private lateinit var songTitle : TextView
    private lateinit var songDownloadProgress : TextView
    private lateinit var progressBar : ProgressBar
    private lateinit var downloadButton : Button
    private lateinit var totalDownloadProgress : TextView
    private lateinit var errorTextView : TextView

    private lateinit var spotifyList : MutableList<String>

    private var spotifyHasSetup = false
    private val requests = Requests()
    private val customClass = CustomClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupSpotify()
        initviews()
        initListeners()
        modifyViews()

    }

    private fun modifyViews() {
        if(checkForPermissions()){
            permissionButton.visibility = View.GONE
        }
        progressBar.keepScreenOn = true
    }

    private fun initListeners() {
        downloadButton.setOnClickListener {
            errorTextView.text = ""
            customClass.closeKeyboard(this)
            if(isRequirementsMet()){
                beginTheProcess()
            }
        }
        permissionButton.setOnClickListener {
            customClass.closeKeyboard(this)
            askForPermissions()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun beginTheProcess() {
        queryNumber = 0
        spotifyList.clear()
        createDirectory(subfolderText.text.toString())
        thread {
            spotifyList = getPlaylistItems(spotifylinkedittext.text.toString())
            while (queryNumber < spotifyList.size){
                query = spotifyList[queryNumber]
                getVideoLink()
                getDownloadLink()
                startDownload()
                if (isDownloadConfirmed()){
                    title = ""
                    videoLink = ""
                    downloadLink = ""
                    query = ""
                    queryNumber+=1
                }else{
                    thread {
                        runOnUiThread {
                            errorTextView.text = "Error, retrying in 3 seconds"
                        }
                    }.join(3000)
                    runOnUiThread {
                        errorTextView.text = ""
                    }
                }
            }
        }
    }

    private fun isRequirementsMet(): Boolean {
        if (!requests.isInternetConnection()){
            customClass.snackBarMessage(this,"Couldn't connect to Internet")
        } else if(!checkForPermissions()){
            customClass.snackBarMessage(this,"Please grant the permission")
        } else if(subfolderText.text.isEmpty() || spotifylinkedittext.text.isEmpty()){
            customClass.snackBarMessage(this,"Empty field")
        } else if (checkForPermissions() && spotifyHasSetup){
            return true
        }
        return false
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

    private fun checkForPermissions() : Boolean{

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Environment.isExternalStorageManager()
        }else{
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )== PackageManager.PERMISSION_GRANTED
        }
    }

    private fun initviews() {
        spotifylinkedittext  = findViewById(R.id.spotifylinkedittext)
        downloadButton = findViewById(R.id.downloadButton)
        permissionButton = findViewById(R.id.permissionButton)
        progressBar = findViewById(R.id.progressBar)
        songTitle = findViewById(R.id.songTitile)
        songDownloadProgress = findViewById(R.id.songDownloadProgress)
        spotifyList = mutableListOf()
        subfolderText = findViewById(R.id.subfolderName)
        totalDownloadProgress = findViewById(R.id.totalProgress)
        errorTextView = findViewById(R.id.errorTextView)
    }

    private fun setupSpotify() {
        thread {
            try {
                if (requests.isInternetConnection()) {
                    spotifyApi = SpotifyApi.Builder().setClientId(getString(R.string.clientId))
                        .setClientSecret(
                            getString(R.string.clientSecret)
                        ).build()
                    val clientCredentialsRequest = spotifyApi.clientCredentials().build()
                    val clientCredentials = clientCredentialsRequest.execute()
                    spotifyApi.accessToken = clientCredentials.accessToken
                    spotifyHasSetup = true
                } else{
                    customClass.snackBarMessage(this,"Can't connect to the Internet")
                }
            }catch (e:Exception){
                runOnUiThread {
                    customClass.snackBarMessage(this, e.toString())
                }
            }
        }.join()
    }

    private fun getPlaylistItems(link: String): MutableList<String> {
        val arrayList: MutableList<String> = ArrayList()
        thread {
            try {
                customClass.snackBarMessage(this,"Gathering information")
                val playlistId= link.split("playlist/")[1].split("?")[0]
                val getPlaylistsItemsRequests = spotifyApi.getPlaylistsItems(playlistId).build()
                var playlistTrackPaging: Paging<PlaylistTrack>?
                playlistTrackPaging = getPlaylistsItemsRequests.execute()
                for (i in 0..playlistTrackPaging!!.total / 100) {
                    try {
                        playlistTrackPaging =
                            spotifyApi.getPlaylistsItems(playlistId).offset(i * 100).build().execute()
                    } catch (e: Exception) {
                        customClass.snackBarMessage(this,e.toString())
                    }
                    for (j in playlistTrackPaging!!.items.indices) {
                        val temp =playlistTrackPaging.items[j].track
                        val songName = temp.name
                        val artistName = temp.toString().split("artists=")[1].split("name=")[1].split(",")[0]
                        arrayList.add("$songName by $artistName")
                    }
                }
                customClass.snackBarMessage(this,"Information gathered successfully")
            }catch (e:Exception){
                runOnUiThread {
                    customClass.snackBarMessage(this,e.toString())
                }
            }
        }.join()

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

    private fun getVideoLink(){
        thread {
            try {
                val temp  = requests.get("https://www.youtube.com/results?search_query=$query Auto-generated by YouTube&sp=EgIQAQ%253D%253D")!!
                val videoID = temp.split("estimatedResults")[1].split("videoId\":\"")[1].split(
                    "\""
                )[0]
                videoLink = "https://youtu.be/$videoID"
            } catch (e:Exception){}
        }.join()
    }

    private fun  getDownloadLink() {
        thread {
            try {
                val s1= requests.post("https://yt1s.com/api/ajaxSearch/index", getmapped())!!
                title = s1.split("title\":\"")[1].split("\"")[0]
                runOnUiThread {
                    songTitle.text = renameTitle()
                    songDownloadProgress.text = "0%"
                    progressBar.progress = 0
                }
                val videoID = s1.split("vid\":\"")[1].split("\"")[0]
                val k = s1.split("k\":\"")[1].split("\"")[0]
                val s2 = requests.post("https://yt1s.com/api/ajaxConvert/convert", getmapped2(k,videoID))!!
                downloadLink= s2.split("dlink\":\"")[1].split("\"")[0].replace("\\", "")
            }catch (e:Exception){
                println(e)
            }
        }.join()
    }
    @SuppressLint("SetTextI18n")
    private fun startDownload(){
        thread {
            try {
                var count: Int
                val url = URL(downloadLink)
                val connection: URLConnection = url.openConnection()
                connection.connect()
                val lengthOfFile: Int = connection.contentLength
                val input: InputStream = BufferedInputStream(url.openStream())
                val output: OutputStream = FileOutputStream(File(folderpath, "${renameTitle()}.mp3"))
                val data = ByteArray(1024)
                var total: Long = 0
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    val downloadedprogress = "" + (total * 100 / lengthOfFile).toInt()
                    output.write(data, 0, count)
                    if (downloadedprogress == "100"){
                        println("${queryNumber+1} ${renameTitle()} Download Complete")
                        runOnUiThread {
                            totalDownloadProgress.text = "${queryNumber+1}/${spotifyList.size}"
                        }
                    }
                    runOnUiThread {
                        progressBar.progress = downloadedprogress.toInt()
                        songDownloadProgress.text = "$downloadedprogress%"
                    }
                }
                output.flush()
                output.close()
                input.close()
            } catch (e:Exception){}
        }.join()
    }

    fun getmapped() : MultipartBody{
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("q",videoLink)
            .addFormDataPart("vt","mp3")
            .build()

        return requestBody
    }
    fun getmapped2(string1: String, string2: String) : MultipartBody{
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("k", string1)
            .addFormDataPart("vid", string2)
            .build()

        return requestBody
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

    private fun isDownloadConfirmed(): Boolean {
        val files = folderpath.listFiles()
        for(i in files!!){
            if (renameTitle() in i.name){
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        requests.clearCookieManager()
        super.onDestroy()
    }
}
