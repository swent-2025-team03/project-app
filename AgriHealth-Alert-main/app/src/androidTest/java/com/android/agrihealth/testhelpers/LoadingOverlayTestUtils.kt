package com.android.agrihealth.testhelpers

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.agrihealth.ui.loading.LoadingTestTags

object LoadingOverlayTestUtils {

  fun ComposeContentTestRule.assertLoadingOverlayVisible() {
    onNodeWithTag(LoadingTestTags.SCRIM).assertIsDisplayed()
    onNodeWithTag(LoadingTestTags.SPINNER).assertIsDisplayed()
  }

  fun ComposeContentTestRule.assertLoadingOverlayHidden() {
    onNodeWithTag(LoadingTestTags.SCRIM).assertDoesNotExist()
    onNodeWithTag(LoadingTestTags.SPINNER).assertDoesNotExist()
  }

  fun ComposeContentTestRule.waitForLoadingToStart(timeoutMillis: Long, isLoading: () -> Boolean) {
    waitUntil(timeoutMillis) { isLoading() }
  }

  fun ComposeContentTestRule.waitForLoadingToFinish(timeoutMillis: Long, isLoading: () -> Boolean) {
    waitUntil(timeoutMillis) { !isLoading() }
  }

  /**
   * Orchestrates the full cycle:
   * - waits for isLoading to become true
   * - asserts overlay is visible
   * - waits for isLoading to become false
   * - asserts overlay is hidden
   */
  fun ComposeContentTestRule.assertOverlayDuringLoading(
      isLoading: () -> Boolean,
      timeoutStart: Long,
      timeoutEnd: Long,
  ) {
    waitForLoadingToStart(timeoutStart, isLoading)
    assertLoadingOverlayVisible()
    waitForLoadingToFinish(timeoutEnd, isLoading)
    assertLoadingOverlayHidden()
  }
}
