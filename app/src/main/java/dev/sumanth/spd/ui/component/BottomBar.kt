package dev.sumanth.spd.ui.component

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.sumanth.spd.model.NavigationItem
import kotlinx.coroutines.launch


@Composable
fun BottomBar(navigationItems: List<NavigationItem>, pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()

    NavigationBar {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (pagerState.currentPage == index) item.selectedIcon else item.unselectedIcon,
                        contentDescription = null
                    )
                },
                label = { Text(item.title) },
                selected = index==pagerState.currentPage,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}