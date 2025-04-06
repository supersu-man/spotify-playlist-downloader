package dev.sumanth.spd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.supersuman.apkupdater.ApkUpdater
import dev.sumanth.spd.model.NavigationItem
import dev.sumanth.spd.ui.component.Background
import dev.sumanth.spd.ui.component.BottomBar
import dev.sumanth.spd.ui.component.PermissionDialog
import dev.sumanth.spd.ui.component.TopBar
import dev.sumanth.spd.ui.component.UpdateDialog
import dev.sumanth.spd.ui.screen.HomeScreen
import dev.sumanth.spd.ui.screen.PreferencesScreen
import dev.sumanth.spd.ui.viewmodel.UpdaterViewModel
import dev.sumanth.spd.utils.Downloader
import dev.sumanth.spd.utils.Spotify
import dev.sumanth.spd.utils.spotify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.NewPipe


class MainActivity : ComponentActivity() {


    private val navigationItems = listOf(
        NavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem("Preferences", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        spotify = Spotify(this@MainActivity)

        NewPipe.init(Downloader.getInstance())
        val updateViewModel = UpdaterViewModel()
        checkForUpdate(updateViewModel)

        setContent {

            var title by remember { mutableStateOf("Home") }
            val pagerState = rememberPagerState( pageCount = { navigationItems.size })

            Background {
                Scaffold(topBar = { TopBar(title) }, bottomBar = { BottomBar(navigationItems, pagerState) }) { innerpadding ->

                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize().padding(innerpadding)) { page ->
                        title = navigationItems[pagerState.currentPage].title
                        if(page==0) {
                            HomeScreen()
                        } else if (page==1) {
                            PreferencesScreen()
                        }
                    }

                    PermissionDialog(this)
                    UpdateDialog(updateViewModel)
                }
            }

        }
    }

    private fun checkForUpdate(viewModel: UpdaterViewModel) = CoroutineScope(Dispatchers.IO).launch {
        try {
            println("Checking for update")
            val updater = ApkUpdater(this@MainActivity, getString(R.string.update_url))
            updater.threeNumbers = true
            viewModel.updater = updater
            if (updater.isInternetConnection() && updater.isNewUpdateAvailable() == true) {
                viewModel.updateFound = true
                println("Update found")
            }
        } catch (e: Exception) {
            println(e)
        }

    }

}