package dev.sumanth.spd.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sumanth.spd.model.AppStatus
import dev.sumanth.spd.ui.component.Idle
import dev.sumanth.spd.ui.component.Queue
import dev.sumanth.spd.ui.component.SpotifyDialog
import dev.sumanth.spd.ui.viewmodel.HomeScreenViewModel

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = viewModel()) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (viewModel.appStatus) {
            AppStatus.IDLE -> {
                Idle(viewModel)
            }
            AppStatus.SCRAPING -> {
                SpotifyDialog(viewModel)
            }
            else -> {
                Queue(viewModel)
            }
        }

    }
}
