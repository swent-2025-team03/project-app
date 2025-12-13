package com.android.agrihealth.testhelpers

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.agrihealth.ui.loading.LoadingTestTags

object LoadingOverlayTestUtils {

  fun ComposeContentTestRule.assertLoadingOverlayVisible() {
    // Assert that both scrim and spinner are visible
    onNodeWithTag(LoadingTestTags.SCRIM).assertIsDisplayed()
    onNodeWithTag(LoadingTestTags.SPINNER).assertIsDisplayed()
  }

  fun ComposeContentTestRule.assertLoadingOverlayHidden() {
    // Assert that both scrim and spinner are not present
    onNodeWithTag(LoadingTestTags.SCRIM).assertDoesNotExist()
    onNodeWithTag(LoadingTestTags.SPINNER).assertDoesNotExist()
  }

  fun ComposeContentTestRule.waitForLoadingToStart(timeoutMillis: Long, isLoading: () -> Boolean) {
    // Wait until isLoading becomes true or timeout elapses
    waitUntil(timeoutMillis) { isLoading() }
  }

  fun ComposeContentTestRule.waitForLoadingToFinish(timeoutMillis: Long, isLoading: () -> Boolean) {
    // Wait until isLoading becomes false or timeout elapses
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
    // Wait for loading to start and verify overlay is visible
    waitForLoadingToStart(timeoutStart, isLoading)
    assertLoadingOverlayVisible()

    // Wait for loading to finish and verify overlay is hidden
    waitForLoadingToFinish(timeoutEnd, isLoading)
    assertLoadingOverlayHidden()
  }
}
