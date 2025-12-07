package com.android.agrihealth.testhelpers

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.agrihealth.ui.loading.LoadingTestTags

object LoadingOverlayTestUtils {

  private const val TAG = "LoadingOverlayTest"

  fun ComposeContentTestRule.assertLoadingOverlayVisible() {
    Log.d(TAG, "assertLoadingOverlayVisible: checking SCRIM & SPINNER are displayed")
    onNodeWithTag(LoadingTestTags.SCRIM).assertIsDisplayed()
    onNodeWithTag(LoadingTestTags.SPINNER).assertIsDisplayed()
  }

  fun ComposeContentTestRule.assertLoadingOverlayHidden() {
    Log.d(TAG, "assertLoadingOverlayHidden: checking SCRIM & SPINNER are not present")
    onNodeWithTag(LoadingTestTags.SCRIM).assertDoesNotExist()
    onNodeWithTag(LoadingTestTags.SPINNER).assertDoesNotExist()
  }

  fun ComposeContentTestRule.waitForLoadingToStart(timeoutMillis: Long, isLoading: () -> Boolean) {
    Log.d(
        TAG,
        "waitForLoadingToStart: timeout=${'$'}timeoutMillis, initial isLoading=${'$'}{isLoading()}")
    val start = System.currentTimeMillis()
    waitUntil(timeoutMillis) { isLoading() }
        .also {
          val took = System.currentTimeMillis() - start
          Log.d(
              TAG,
              "waitForLoadingToStart: finished in ${'$'}took ms, final isLoading=${'$'}{isLoading()}")
        }
  }

  fun ComposeContentTestRule.waitForLoadingToFinish(timeoutMillis: Long, isLoading: () -> Boolean) {
    Log.d(
        TAG,
        "waitForLoadingToFinish: timeout=${'$'}timeoutMillis, initial isLoading=${'$'}{isLoading()}")
    val start = System.currentTimeMillis()
    waitUntil(timeoutMillis) { !isLoading() }
        .also {
          val took = System.currentTimeMillis() - start
          Log.d(
              TAG,
              "waitForLoadingToFinish: finished in ${'$'}took ms, final isLoading=${'$'}{isLoading()}")
        }
  }

  /**
   * Enchaîne tout le cycle :
   * - attend que isLoading passe à true
   * - vérifie que l'overlay est visible
   * - attend que isLoading repasse à false
   * - vérifie que l'overlay a disparu
   */
  fun ComposeContentTestRule.assertOverlayDuringLoading(
      isLoading: () -> Boolean,
      timeoutStart: Long,
      timeoutEnd: Long,
  ) {
    Log.d(
        TAG,
        "assertOverlayDuringLoading: START, timeoutStart=${'$'}timeoutStart, timeoutEnd=${'$'}timeoutEnd, initial isLoading=${'$'}{isLoading()}")
    val totalStart = System.currentTimeMillis()

    waitForLoadingToStart(timeoutStart, isLoading)
    Log.d(
        TAG,
        "assertOverlayDuringLoading: after waitForLoadingToStart, isLoading=${'$'}{isLoading()}")
    assertLoadingOverlayVisible()

    waitForLoadingToFinish(timeoutEnd, isLoading)
    Log.d(
        TAG,
        "assertOverlayDuringLoading: after waitForLoadingToFinish, isLoading=${'$'}{isLoading()}")
    assertLoadingOverlayHidden()

    val total = System.currentTimeMillis() - totalStart
    Log.d(TAG, "assertOverlayDuringLoading: END, total=${'$'}total ms")
  }
}
