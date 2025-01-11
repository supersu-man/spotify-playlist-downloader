package dev.sumanth.spd.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import dev.sumanth.spd.model.NavigationItem


@Composable
fun BottomBar(navigationItems: List<NavigationItem>, navController: NavHostController) {
    var selectedItem by remember { mutableStateOf(navigationItems[0]) }

    NavigationBar {
        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selectedItem.title == item.title) item.selectedIcon else item.unselectedIcon,
                        contentDescription = null
                    )
                },
                label = { Text(item.title) },
                selected = selectedItem.title == item.title,
                onClick = {
                    selectedItem = item
                    navController.popBackStack()
                    navController.navigate(item.route)
                }
            )
        }
    }
}