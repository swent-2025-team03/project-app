package com.android.agrihealth.testhelpers.templates

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.testhelpers.TestTimeout
import org.junit.Rule
import org.junit.Test

abstract class BaseUITest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  abstract fun displayAllComponents()

  /** Sets the content to be displayed in the Compose test, using the app's theme */
  protected fun setContent(content: @Composable () -> Unit) {
    composeTestRule.setContent {
      AgriHealthAppTheme {
        content()
      }
    }
  }

  /** Returns the node with the provided tag. This is simply a shorter notation for readability */
  protected fun node(tag: String): SemanticsNodeInteraction {
    return composeTestRule.onNodeWithTag(tag)
  }

  /** Asserts that the node with the given test tag is displayed, within a reasonable timeout.
   * The timeout is here by default to make the tests more robust in case of performance drops
   * @param tag Test tag for the node to check
   * @param timeout Number of milliseconds to wait for the node to be displayed before failure
   */
  protected fun assertNodeIsDisplayed(tag: String, timeout: Long = TestTimeout.DEFAULT_TIMEOUT) {
    composeTestRule.waitUntil(timeout) {
      node(tag).isDisplayed()
    }
  }

  /** Clicks on the node corresponding to the provided tag */
  protected fun clickOn(tag: String) {
    node(tag).assertIsDisplayed().performClick()
  }
}