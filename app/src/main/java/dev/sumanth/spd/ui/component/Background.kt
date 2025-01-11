package dev.sumanth.spd.ui.component

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import dev.sumanth.spd.ui.theme.AppTheme

@Composable
fun Background (callback: @Composable () -> Unit) {
    AppTheme {
        Surface {
            callback()
        }
    }
}