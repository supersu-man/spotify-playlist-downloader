package dev.sumanth.spd

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import dev.sumanth.spd.model.NavigationItem
import dev.sumanth.spd.ui.component.Background
import dev.sumanth.spd.ui.component.BottomBar
import dev.sumanth.spd.ui.component.TopBar
import dev.sumanth.spd.ui.component.UpdateDialog
import dev.sumanth.spd.ui.screen.HomeScreen
import dev.sumanth.spd.ui.screen.PreferencesScreen
import dev.sumanth.spd.ui.viewmodel.UpdaterViewModel
import dev.sumanth.spd.utils.NewPipeDownloader
import dev.sumanth.spd.utils.SharedPref
import org.schabi.newpipe.extractor.NewPipe

class MainActivity : ComponentActivity() {

    private val navigationItems = listOf(
        NavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem("Preferences", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    private val updateViewModel: UpdaterViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val sharedPref = SharedPref(this)
        if (sharedPref.getAutoUpdateCheck()) {
            updateViewModel.checkForUpdate(this)
        }

        NewPipe.init(NewPipeDownloader.getInstance())

        setContent {

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

                    UpdateDialog(updateViewModel)
                }
            }
        }
    }
}
