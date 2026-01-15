package dev.sumanth.spd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.sumanth.spd.model.NavigationItem
import dev.sumanth.spd.ui.component.Background
import dev.sumanth.spd.ui.component.BottomBar
import dev.sumanth.spd.ui.component.PermissionDialog
import dev.sumanth.spd.ui.component.TopBar
import dev.sumanth.spd.ui.component.UpdateDialog
import dev.sumanth.spd.ui.screen.HomeScreen
import dev.sumanth.spd.ui.screen.PreferencesScreen
import dev.sumanth.spd.ui.viewmodel.UpdaterViewModel
import dev.sumanth.spd.utils.NewPipeDownloader
import dev.sumanth.spd.utils.SharedPref
import dev.sumanth.spd.utils.SpotifyManager
import dev.sumanth.spd.utils.spotify
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.NewPipe

class MainActivity : ComponentActivity() {

    private val navigationItems = listOf(
        NavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem("Preferences", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    private val updateViewModel: UpdaterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = SharedPref(this)
        if (sharedPref.getAutoUpdateCheck()) {
            updateViewModel.checkForUpdate(this)
        }

        NewPipe.init(NewPipeDownloader.getInstance())

        setContent {
            val scope = rememberCoroutineScope()
            remember {
                scope.launch {
                    spotify = SpotifyManager(this@MainActivity)
                    spotify.init()
                }
            }

            val pagerState = rememberPagerState(pageCount = { navigationItems.size })
            val title by remember {
                derivedStateOf { navigationItems[pagerState.currentPage].title }
            }

            Background {
                Scaffold(
                    topBar = { TopBar(title) },
                    bottomBar = { BottomBar(navigationItems, pagerState) }
                ) { innerPadding ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { page ->
                        when (page) {
                            0 -> HomeScreen()
                            1 -> PreferencesScreen()
                        }
                    }

                    PermissionDialog(this)
                    UpdateDialog(updateViewModel)
                }
            }
        }
    }
}
