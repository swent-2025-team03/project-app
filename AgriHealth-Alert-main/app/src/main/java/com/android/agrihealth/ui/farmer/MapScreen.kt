package com.android.agrihealth.ui.farmer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.agrihealth.ui.navigation.BottomNavigationMenu
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.navigation.Tab

@Composable
fun MapScreen(
    navigationActions: NavigationActions? = null,
) {
  // Temp Implementation of the Map Screen
  Scaffold(
      topBar = {
        Row { Text(Screen.Map.name, modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE)) }
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Map,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { pd -> Column { Text(text = "Map Screen", modifier = Modifier.padding(pd)) } })
}
