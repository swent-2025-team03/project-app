package com.android.agrihealth.ui.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class LoadingScreenTest {

  @get:Rule val compose = createComposeRule()

  // -----------------
  // TEST 1 — Not loading
  // -----------------
  @Test
  fun overlay_notLoading_showsOnlyContent() {
    compose.setContent { LoadingOverlay(isLoading = false) { Box(Modifier.testTag("content")) } }

    compose.onNodeWithTag("content").assertExists()
    compose.onNodeWithTag("loading_overlay_scrim").assertDoesNotExist()
    compose.onNodeWithTag("loading_overlay_spinner").assertDoesNotExist()
  }

  // -----------------
  // TEST 2 — Loading state
  // -----------------
  @Test
  fun overlay_loading_showsScrimAndSpinner() {
    compose.setContent { LoadingOverlay(isLoading = true) { Box(Modifier.testTag("content")) } }

    compose.onNodeWithTag("content").assertExists()
    compose.onNodeWithTag("loading_overlay_scrim").assertExists()
    compose.onNodeWithTag("loading_overlay_spinner").assertExists()
  }
}
