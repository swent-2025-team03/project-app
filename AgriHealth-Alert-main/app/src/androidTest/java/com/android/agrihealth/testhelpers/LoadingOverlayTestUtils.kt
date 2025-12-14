package com.android.agrihealth.testhelpers

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.agrihealth.ui.loading.LoadingTestTags

object LoadingOverlayTestUtils {

  private fun ComposeContentTestRule.assertLoadingOverlayVisible() {
    onNodeWithTag(LoadingTestTags.SCRIM).assertIsDisplayed()
    onNodeWithTag(LoadingTestTags.SPINNER).assertIsDisplayed()
  }

  private fun ComposeContentTestRule.assertLoadingOverlayHidden() {
    onNodeWithTag(LoadingTestTags.SCRIM).assertDoesNotExist()
    onNodeWithTag(LoadingTestTags.SPINNER).assertDoesNotExist()
  }

  private fun ComposeContentTestRule.waitForLoadingToStart(
      timeoutMillis: Long,
      isLoading: () -> Boolean
  ) {
    waitUntil(timeoutMillis) { isLoading() }
  }

  private fun ComposeContentTestRule.waitForLoadingToFinish(
      timeoutMillis: Long,
      isLoading: () -> Boolean
  ) {
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
      timeout: Long = TestTimeout.LONG_TIMEOUT,
  ) {
    waitForLoadingToStart(timeout, isLoading)
    assertLoadingOverlayVisible()
    waitForLoadingToFinish(timeout, isLoading)
    assertLoadingOverlayHidden()
  }
}
