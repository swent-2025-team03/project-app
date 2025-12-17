package com.android.agrihealth.ui.common.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.agrihealth.testhelpers.templates.UITest
import org.junit.Test

class LoadingScreenTest : UITest() {

  override fun displayAllComponents() {}

  private val testTag = "content"

  private fun setLoadingContent(isLoading: Boolean) {
    setContent {
      LoadingOverlay(isLoading) { Box(Modifier.Companion.testTag(testTag)) { Text(":)") } }
    }
  }

  @Test
  fun overlay_notLoading_showsOnlyContent() {
    setLoadingContent(false)

    with(LoadingTestTags) {
      nodeIsDisplayed(testTag)
      nodesNotDisplayed(SCRIM, SPINNER)
    }
  }

  @Test
  fun overlay_loading_showsScrimAndSpinner() {
    setLoadingContent(true)

    with(LoadingTestTags) { nodesAreDisplayed(testTag, SCRIM, SPINNER) }
  }
}
