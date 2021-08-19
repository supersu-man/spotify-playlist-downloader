package com.supersuman.spd

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.supersuman.githubapkupdater.Updater
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.Paging
import com.wrapper.spotify.model_objects.specification.PlaylistTrack
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.*
import java.net.URL
import java.net.URLConnection
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var folderpath : File
    private lateinit var spotifyApi : SpotifyApi
    private var title = ""
    private var queryNumber = 0
    private var retries = 0
    private var maxRetries = 0

    private lateinit var permissionButton : Button
    private lateinit var subfolderText : EditText
    private lateinit var spotifylinkedittext : EditText
    private lateinit var songTitle : TextView
    private lateinit var songDownloadProgress : TextView
    private lateinit var progressBar : ProgressBar
    private lateinit var downloadButton : Button
    private lateinit var totalDownloadProgress : TextView
    private lateinit var errorTextView : TextView
    private lateinit var rootLayout: CoordinatorLayout
    private lateinit var slider : Slider

    private lateinit var spotifyList : MutableList<String>

    private var spotifyHasSetup = false
    private var youtubeDlHasSetup = false
    private val requests = Requests()
    private val customClass = CustomClass()
    private val updater = Updater(this,"https://github.com/supersu-man/SpotifyPlaylistDownloader/releases/latest")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initviews()
        thread {
            checkForUpdates(updater)
        }
        setupYoutubeDL()
        setupSpotify()
        initListeners()
        modifyViews()
    }

    private fun initviews() {
        rootLayout = findViewById(R.id.rootLayout)
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
        slider = findViewById(R.id.retrySlider)
    }

    private fun checkForUpdates(updater: Updater){
        if (updater.isInternetConnection()){
            updater.init()
            updater.isNewUpdateAvailable {
                Snackbar.make(rootLayout,"New Update Found",Snackbar.LENGTH_INDEFINITE).setAction("Download"){
                    if (updater.hasPermissionsGranted()){
                        updater.requestDownload()
                    } else{
                        updater.requestMyPermissions {
                            updater.requestDownload()
                        }
                    }
                }.show()
            }
        }else{
            runOnUiThread {
                Snackbar.make(rootLayout,"Unable To Check For Updates",Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpotify() {
        thread {
            try {
                if (requests.isInternetConnection()){
                    spotifyApi = SpotifyApi.Builder().setClientId(getString(R.string.clientId))
                        .setClientSecret(
                            getString(R.string.clientSecret)
                        ).build()
                    val clientCredentialsRequest = spotifyApi.clientCredentials().build()
                    val clientCredentials = clientCredentialsRequest.execute()
                    spotifyApi.accessToken = clientCredentials.accessToken
                    spotifyHasSetup = true
                }
            }catch (e:Exception){
                runOnUiThread {
                    Snackbar.make(rootLayout,e.toString(),Snackbar.LENGTH_SHORT).show()
                }
            }
        }.join()
    }

    private fun setupYoutubeDL(){
        thread {
            try {
                YoutubeDL.getInstance().init(this.applicationContext)
                youtubeDlHasSetup = true
            } catch (e: YoutubeDLException) { println(e) }
        }
    }

    private fun initListeners() {
        slider.addOnChangeListener { _, value, _ ->
            maxRetries = value.toInt()
        }
        
        downloadButton.setOnClickListener {
            errorTextView.text = ""
            customClass.closeKeyboard(this)
            if(isRequirementsMet()){
                queryNumber = 0
                spotifyList.clear()
                createDirectory(subfolderText.text.toString())
                beginTheProcess()
            }
        }

        permissionButton.setOnClickListener {
            customClass.closeKeyboard(this)
            askForPermissions()
        }
    }

    private fun modifyViews() {
        if(checkForPermissions()){
            permissionButton.visibility = View.GONE
        }
        progressBar.keepScreenOn = true
    }

    @SuppressLint("SetTextI18n")
    private fun beginTheProcess() {
        thread {
            spotifyList = getPlaylistItems(spotifylinkedittext.text.toString())
            while (queryNumber < spotifyList.size){
                val id = getVideoId(spotifyList[queryNumber])
                val downloadLink = getDownloadLink(id)
                startDownload(downloadLink)
                if (isDownloadConfirmed()){
                    queryNumber+=1
                    retries=0
                }else{
                    runOnUiThread {
                        retries+=1
                        errorTextView.text = "Error, retrying in 3 seconds. ${maxRetries-retries} retries left."
                    }
                    Thread.sleep(3000)
                    runOnUiThread {
                        errorTextView.text = ""
                    }
                    if (retries>=maxRetries){
                        queryNumber+=1
                        retries=0
                    }
                }
            }
        }
    }

    private fun isRequirementsMet(): Boolean {
        if (!requests.isInternetConnection()){
            Snackbar.make(rootLayout,"Couldn't connect to Internet",Snackbar.LENGTH_SHORT).show()
        } else if(!checkForPermissions()){
            Snackbar.make(rootLayout,"Please grant the permission",Snackbar.LENGTH_SHORT).show()
        } else if(subfolderText.text.isEmpty() || spotifylinkedittext.text.isEmpty()){
            Snackbar.make(rootLayout,"Empty field",Snackbar.LENGTH_SHORT).show()
        } else if (checkForPermissions() && spotifyHasSetup && youtubeDlHasSetup){
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

    private fun getPlaylistItems(link: String): MutableList<String> {
        val arrayList: MutableList<String> = ArrayList()
        thread {
            try {
                Snackbar.make(rootLayout,"Gathering information",Snackbar.LENGTH_SHORT).show()
                val playlistId= link.split("playlist/")[1].split("?")[0]
                val getPlaylistsItemsRequests = spotifyApi.getPlaylistsItems(playlistId).build()
                var playlistTrackPaging: Paging<PlaylistTrack>?
                playlistTrackPaging = getPlaylistsItemsRequests.execute()
                for (i in 0..playlistTrackPaging!!.total / 100) {
                    playlistTrackPaging =
                        spotifyApi.getPlaylistsItems(playlistId).offset(i * 100).build().execute()
                    for (j in playlistTrackPaging!!.items.indices) {
                        val temp =playlistTrackPaging.items[j].track
                        val songName = temp.name
                        val artistName = temp.toString().split("artists=")[1].split("name=")[1].split(",")[0]
                        arrayList.add("$songName by $artistName")
                    }
                }
                Snackbar.make(rootLayout,"Information gathered successfully",Snackbar.LENGTH_SHORT).show()
            }catch (e:Exception){
                runOnUiThread {
                    Snackbar.make(rootLayout,e.toString(),Snackbar.LENGTH_SHORT).show()
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

    private fun getVideoId(query: String): String {
        var videoId = ""
        thread {
            try {
                val response = requests.get("https://music.youtube.com/search?q=${query.replace(" ","+")}")!!
                val jsonString = response.split("JSON.parse('\\x7b\\x22query\\x22")[1].split("data:")[1].split("'")[1]
                val replace = jsonString
                    .replace("\\x22","\"")
                val y = replace.split("musicShelfRenderer")
                val z = y.subList(1,3)
                for (i in z){
                    val category = i.split("text\"")[1].split("\"")[1]
                    val type = i.split("\"musicVideoType\"")[1].split("\"")[1]
                    if ("Top result" == category && type == "MUSIC_VIDEO_TYPE_ATV" ){
                        videoId = i.split("\"videoId\"")[1].split("\"")[1]
                        break
                    }else if ("Songs" == category){
                        videoId = i.split("\"videoId\"")[1].split("\"")[1]
                    }
                }
            } catch (e:Exception){
                println(e)
            }
        }.join()
        return videoId
    }

    private fun  getDownloadLink(id: String): String {
        var downloadLink =""
        thread {
            try {
                val request = YoutubeDLRequest("https://www.youtube.com/watch?v=$id")
                request.addOption("-f", "best")
                val streamInfo = YoutubeDL.getInstance().getInfo(request)
                title = streamInfo.title
                    runOnUiThread {
                    songTitle.text = streamInfo.title
                    songDownloadProgress.text = "0%"
                    progressBar.progress = 0
                }
                downloadLink = streamInfo.url
            }catch (e:Exception){
                println(e)
            }
        }.join()
        return downloadLink
    }

    @SuppressLint("SetTextI18n")
    private fun startDownload(downloadLink: String) {
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
            if (renameTitle() != "" && "${renameTitle()}.mp3" in i.name){
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
