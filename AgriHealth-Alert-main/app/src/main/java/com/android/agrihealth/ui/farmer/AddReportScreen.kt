package com.android.agrihealth.ui.farmer

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
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen

object AddReportScreenTestTags {
  const val SCREEN = "AddReportScreen"
}

@Preview
@Composable
fun AddReportScreen(
    onDone: () -> Unit = {},
    onGoBack: () -> Unit = {},
) {
  // Implementation of the Add Report Screen
  Scaffold(
      topBar = {
        Row {
          Text(Screen.AddReport.name, modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
          Button(
              onClick = onGoBack, modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {}
        }
      },
      content = { pd ->
        Column {
          Text(text = "Add Report Screen")
          Button(onClick = onDone, modifier = Modifier.padding(pd)) { Text(text = "Done") }
        }
      })
}
