package com.android.sample.ui.farmer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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

object OverviewScreenTestTags {
  const val ADD_REPORT_BUTTON = "addReportFab"
  const val LOGOUT_BUTTON = "logoutButton"
}

@Preview
@Composable
fun OverviewScreen(
    onAddReport: () -> Unit = {},
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {
  // temp implementation for testing
  Scaffold(
      topBar = {
        Row { Text("Overview", modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE)) }
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Overview,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { pd ->
        Column {
          Text(text = "Overview Screen")
          Button(
              onClick = onAddReport,
              modifier = Modifier.padding(pd).testTag(OverviewScreenTestTags.ADD_REPORT_BUTTON)) {
                Text(text = "Add Report")
              }
          Button(onClick = onSignedOut, modifier = Modifier.padding(pd)) { Text(text = "Sign Out") }
        }
      })
}
