package com.android.agrihealth.ui.alert

import androidx.compose.ui.test.*
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.data.model.alert.FakeAlertRepository
import com.android.agrihealth.testhelpers.templates.BaseUITest
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.overview.AlertUiState
import org.junit.Test

class AlertViewScreenTest : BaseUITest() {
  private fun setAlertViewScreen() {
    val startAlertId = "1"
    val viewModel = AlertViewModel(FakeAlertRepository().allAlerts.map { AlertUiState(alert = it) }, startAlertId)

    setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      AlertViewScreen(navigationActions = navigationActions, viewModel = viewModel)
    }
  }

  override fun displayAllComponents() {
    setAlertViewScreen()

    with(AlertViewScreenTestTags) {
      assertNodeIsDisplayed(ALERT_DESCRIPTION)
      assertNodeIsDisplayed(ALERT_DATE)
      assertNodeIsDisplayed(ALERT_REGION)

      assertNodeIsDisplayed(PREVIOUS_ALERT_ARROW)
      assertNodeIsDisplayed(NEXT_ALERT_ARROW)

      assertNodeIsDisplayed(VIEW_ON_MAP)
    }
  }

  @Test
  fun chevronArrows_enabledState_reflectsRepository_loadsPreviousAndNextAlert() {
    setAlertViewScreen()

    with(AlertViewScreenTestTags) {
      node(PREVIOUS_ALERT_ARROW).assertIsNotEnabled()
      node(NEXT_ALERT_ARROW).assertIsEnabled()

      clickOn(NEXT_ALERT_ARROW)
      assertNodeIsDisplayed(containerTag(1))

      clickOn(PREVIOUS_ALERT_ARROW)
      assertNodeIsDisplayed(containerTag(0))
    }
  }
}
