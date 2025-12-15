package com.android.agrihealth.testhelpers.templates

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.testhelpers.TestTimeout
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

abstract class UITest(private val grantedPermissions: Array<String> = emptyArray()) {
  val composeTestRule: ComposeContentTestRule = createComposeRule()
  // open fun permissions(): Array<String> = emptyArray()

  @get:Rule
  val ruleChain: TestRule
    get() {
      val filteredPerms =
          grantedPermissions
              .filterNot { // Granting notification perms crashes on Android 12 and under
                it == android.Manifest.permission.POST_NOTIFICATIONS &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
              }
              .toTypedArray()
      return RuleChain.outerRule(GrantPermissionRule.grant(*filteredPerms)).around(composeTestRule)
    }

  // Intentionally not a test, to make it optional to run as it can lose 1 second per test suite
  abstract fun displayAllComponents()

  /** Sets the content to be displayed in the Compose test, using the app's theme */
  protected fun setContent(content: @Composable () -> Unit) {
    composeTestRule.setContent { AgriHealthAppTheme { content() } }
  }

  /** Returns the node with the provided tag. This is simply a shorter notation for readability */
  protected fun node(tag: String): SemanticsNodeInteraction {
    return composeTestRule.onNodeWithTag(tag)
  }

  protected fun nodes(tag: String): List<SemanticsNode> {
    return composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes()
  }

  /** Make an action on a node (assertion most of the time) with proper CI error description */
  private fun <T> withNode(tag: String, action: SemanticsNodeInteraction.() -> T): T =
      try {
        node(tag).action()
      } catch (e: Throwable) {
        if (e is AssertionError || e is ComposeTimeoutException) {
          val method = Throwable().stackTrace[1].methodName
          val line1 = Throwable().stackTrace[3].lineNumber
          val line2 = Throwable().stackTrace[4].lineNumber
          throw AssertionError(
              "Component with tag \"$tag\" failed during action \"$method()\" (possibly @L$line1 or @L$line2)",
              e)
        } else throw e
      }

  private fun <T> withText(text: String, action: SemanticsNodeInteraction.() -> T): T =
      try {
        composeTestRule.onNodeWithText(text).action()
      } catch (e: Throwable) {
        if (e is AssertionError || e is ComposeTimeoutException) {
          val method = Throwable().stackTrace[1].methodName
          val line1 = Throwable().stackTrace[3].lineNumber
          val line2 = Throwable().stackTrace[4].lineNumber
          throw AssertionError(
              "Component with text \"$text\" failed during action \"$method()\" (possibly @L$line1 or @L$line2)",
              e)
        } else throw e
      }

  /**
   * Asserts that the node with the given test tag is displayed, within a reasonable timeout. The
   * timeout is here by default to make the tests more robust in case of performance drops
   *
   * @param tag Test tag for the node to check
   * @param timeout Number of milliseconds to wait for the node to be displayed before failure
   */
  protected fun nodeIsDisplayed(tag: String, timeout: Long = TestTimeout.DEFAULT_TIMEOUT) {
    withNode(tag) { composeTestRule.waitUntil(timeout) { isDisplayed() } }
  }

  protected fun nodeNotDisplayed(tag: String, timeout: Long = TestTimeout.DEFAULT_TIMEOUT) {
    withNode(tag) { composeTestRule.waitUntil(timeout) { isNotDisplayed() } }
  }

  protected fun textIsDisplayed(text: String, timeout: Long = TestTimeout.DEFAULT_TIMEOUT) {
    withText(text) { composeTestRule.waitUntil(timeout) { isDisplayed() } }
  }

  protected fun textNotDisplayed(text: String, timeout: Long = TestTimeout.DEFAULT_TIMEOUT) {
    withText(text) { composeTestRule.waitUntil(timeout) { isNotDisplayed() } }
  }

  private fun SemanticsNodeInteraction.hasTextContaining(
      text: String,
      ignoreCase: Boolean = false
  ): Boolean =
      fetchSemanticsNode().config.run {
        val normalText =
            getOrNull(SemanticsProperties.Text)?.any { it.text.contains(text, ignoreCase) } == true
        val editableText =
            getOrNull(SemanticsProperties.EditableText)?.text?.contains(text, ignoreCase) == true

        normalText || editableText
      }

  protected fun textContains(
      tag: String,
      text: String?,
      timeout: Long = TestTimeout.DEFAULT_TIMEOUT,
      ignoreCase: Boolean = false
  ) {
    withNode(tag) {
      composeTestRule.waitUntil(timeout) {
        text != null && isDisplayed() && hasTextContaining(text, ignoreCase)
      }
    }
  }

  protected fun nodesAreDisplayed(vararg tags: String) {
    for (tag in tags) nodeIsDisplayed(tag)
  }

  protected fun nodesNotDisplayed(vararg tags: String) {
    for (tag in tags) nodeNotDisplayed(tag)
  }

  /** Clicks on the node corresponding to the provided tag */
  protected fun clickOn(tag: String) {
    withNode(tag) { assertIsDisplayed().performClick() }
  }

  protected fun clickOnText(text: String) {
    withText(text) { assertIsDisplayed().performClick() }
  }

  /** Performs a text input of the given text on the node corresponding to the given tag */
  protected fun writeIn(tag: String, text: String, reset: Boolean = true) {
    withNode(tag) {
      if (reset) assertIsDisplayed().performTextClearance()
      assertIsDisplayed().performTextInput(text)
    }
  }

  /** Scrolls the scrollable until it reaches the target, and checks if it is displayed */
  protected fun scrollTo(
      scrollable: String,
      target: String,
      timeout: Long = TestTimeout.DEFAULT_TIMEOUT
  ) {
    withNode(scrollable) {
      composeTestRule.waitUntil(timeout) { performScrollToNode(hasTestTag(target)).isDisplayed() }
    }
  }

  protected fun nodeExists(tag: String): Boolean {
    return nodes(tag).firstOrNull() != null
  }

  protected fun debugFreeze() {
    composeTestRule.waitUntil(60 * 1_000) { false }
  }
}
