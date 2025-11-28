package com.android.agrihealth.ui.alert

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.testutil.FakeAlertRepository
import com.android.agrihealth.testutil.TestConstants
import com.android.agrihealth.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test

class AlertViewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setAlertViewScreen(
      alertId: String = "1",
      viewModel: AlertViewModel = AlertViewModel(FakeAlertRepository())
  ) {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      AlertViewScreen(
          navigationActions = navigationActions, viewModel = viewModel, alertId = alertId)
    }
  }

  @Test
  fun alertDetails_areDisplayed() {
    setAlertViewScreen("1")

    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.ALERT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.ALERT_DATE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.ALERT_REGION).assertIsDisplayed()
  }

  @Test
  fun chevronArrows_areDisplayed() {
    setAlertViewScreen("1")

    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.PREVIOUS_ALERT_ARROW).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.NEXT_ALERT_ARROW).assertIsDisplayed()
  }

  @Test
  fun chevronArrows_enabledState_reflectsRepository() {
    val viewModel = AlertViewModel(FakeAlertRepository())
    setAlertViewScreen("1", viewModel)

    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.PREVIOUS_ALERT_ARROW).assertIsNotEnabled()
    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.NEXT_ALERT_ARROW).assertIsEnabled()
  }

  @Test
  fun clickingNextChevron_loadsNextAlert() {
    val viewModel = AlertViewModel(FakeAlertRepository())
    setAlertViewScreen("1", viewModel)

    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.NEXT_ALERT_ARROW).performClick()

    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.containerTag(1)).assertIsDisplayed()
  }

  @Test
  fun clickingPreviousChevron_loadsPreviousAlert() {
    val viewModel = AlertViewModel(FakeAlertRepository())
    setAlertViewScreen("2", viewModel)

    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.PREVIOUS_ALERT_ARROW).performClick()

    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.containerTag(0)).assertIsDisplayed()
  }

  @Test
  fun viewOnMapButton_isDisplayed() {
    setAlertViewScreen("1")
    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.VIEW_ON_MAP).assertIsDisplayed()
  }

  @Test
  fun longTitle_showsFullTitleInBody() {
    val viewModel = AlertViewModel(FakeAlertRepository())
    setAlertViewScreen(alertId = "4", viewModel = viewModel)
    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      composeTestRule
          .onAllNodesWithTag(AlertViewScreenTestTags.ALERT_FULL_TITLE)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag(AlertViewScreenTestTags.ALERT_FULL_TITLE).assertIsDisplayed()
  }
}
