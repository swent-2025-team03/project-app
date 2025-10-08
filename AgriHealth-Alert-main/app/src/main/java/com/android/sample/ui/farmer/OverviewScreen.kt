package com.android.sample.ui.farmer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.ui.navigation.BottomNavigationMenu
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.navigation.NavigationTestTags
import com.android.sample.ui.navigation.Tab

@Preview
@Composable
fun OverviewScreen(
    onAddReport: () -> Unit = {},
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {
  // temp implementation for testing
  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Overview,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { pd ->
        Column {
          Text(text = "Overview Screen")
          Button(onClick = onAddReport, modifier = Modifier.padding(pd)) {
            Text(text = "Add Report")
          }
          Button(onClick = onSignedOut, modifier = Modifier.padding(pd)) { Text(text = "Sign Out") }
        }
      })
}
