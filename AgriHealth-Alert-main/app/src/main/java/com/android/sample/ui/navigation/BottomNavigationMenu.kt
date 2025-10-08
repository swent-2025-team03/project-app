package com.android.sample.ui.navigation

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// file taken from https://github.com/swent-epfl/bootcamp-25-B3-Solution/tree/main
sealed class Tab(val name: String, val icon: ImageVector, val destination: Screen) {
  object Overview : Tab("Overview", Icons.Outlined.Menu, Screen.Overview)

  object Map : Tab("Map", Icons.Outlined.Place, Screen.Map)
}

private val tabs =
    listOf(
        Tab.Overview,
        Tab.Map,
    )

/**
 * A reusable Bottom Navigation Bar built with Material 3 [NavigationBar].
 *
 * This composable provides a simple way to add a consistent bottom navigation menu
 * across multiple screens in your app.
 *
 * It displays navigation items (icons and labels) and highlights the currently selected route.
 * Each item triggers navigation when clicked.
 *
 * Example usage:
 * ```
 * Scaffold(
 *     bottomBar = {
 *         BottomNavigationBar(
 *              selectedTab = Tab.Overview,
 *              onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
 *              modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
 *         )
 *     }
 * ) { innerPadding ->
 *     // Screen content here
 * }
 * ```
 *
 * @param selectedTab The route (or destination) that is currently active.
 * Used to highlight the selected navigation item.
 *
 * @param onTabSelected Callback triggered when a navigation item is clicked.
 * Receives the target route as a parameter.
 *
 * @param modifier Optional [Modifier] for styling or to add test tags.
 */
@Composable
fun BottomNavigationMenu(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
  NavigationBar(
      modifier =
          modifier.fillMaxWidth().height(60.dp).testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
      containerColor = MaterialTheme.colorScheme.surface,
      content = {
        tabs.forEach { tab ->
          NavigationBarItem(
              icon = { Icon(tab.icon, contentDescription = null) },
              label = { Text(tab.name) },
              selected = tab == selectedTab,
              onClick = { onTabSelected(tab) },
              modifier =
                  Modifier.clip(RoundedCornerShape(50.dp))
                      .testTag(NavigationTestTags.getTabTestTag(tab)))
        }
      },
  )
}
