package com.android.agrihealth.ui.overview

import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testhelpers.TestTimeout.DEFAULT_TIMEOUT
import com.android.agrihealth.testhelpers.TestUser
import com.android.agrihealth.testhelpers.fakes.FakeAlertRepository
import com.android.agrihealth.testhelpers.fakes.FakeOverviewViewModel
import com.android.agrihealth.testhelpers.fakes.FakeReportRepository
import com.android.agrihealth.testhelpers.templates.UITest
import junit.framework.TestCase.assertEquals
import org.junit.Test

class OverviewScreenTest : UITest() {
  val farmer1 = TestUser.FARMER1.copy()
  val vet1 = TestUser.VET1.copy()

  private fun setFarmerScreen() {
    setContent {
      OverviewScreen(
          userRole = UserRole.FARMER,
          user = farmer1,
          overviewViewModel = OverviewViewModel(FakeReportRepository(), FakeAlertRepository()))
    }
  }

  private fun setVetScreen() {
    setContent {
      OverviewScreen(
          userRole = UserRole.VET,
          user = vet1,
          overviewViewModel = OverviewViewModel(FakeReportRepository()))
    }
  }

  override fun displayAllComponents() {}

  fun assertOverviewScreenComponentsVisibility(role: UserRole) {
    with(OverviewScreenTestTags) {
      nodesAreDisplayed(TOP_APP_BAR_TITLE, LOGOUT_BUTTON, PROFILE_BUTTON)
      textIsDisplayed("Latest News / Alerts")

      when (role) {
        UserRole.FARMER -> nodeIsDisplayed(ADD_REPORT_BUTTON)
        UserRole.VET -> nodeNotDisplayed(ADD_REPORT_BUTTON)
      }
    }
  }

  @Test
  fun displayAllFarmerComponents() {
    setFarmerScreen()
    assertOverviewScreenComponentsVisibility(UserRole.FARMER)
  }

  @Test
  fun displayAllVetComponents() {
    setVetScreen()
    assertOverviewScreenComponentsVisibility(UserRole.VET)
  }

  @Test
  fun dropdownMenuWrapper_selectOption_callsOnOptionSelected() {
    var selectedOption: String? = null
    val options = listOf("Option 1", "Option 2")

    composeTestRule.setContent {
      DropdownMenuWrapper(
          options = options,
          selectedOption = null,
          onOptionSelected = { selectedOption = it },
          placeholder = "All")
    }
    clickOnText("All")
    clickOnText("Option 1")

    assertEquals("Option 1", selectedOption)
  }

  @Test
  fun firstAlert_isCloseForFarmer_flagCheck() {
    val fakeOverviewVM =
        FakeOverviewViewModel(user = farmer1, alertRepository = FakeAlertRepository())
    val sortedAlerts = fakeOverviewVM.uiState.value.sortedAlerts

    setFarmerScreen()

    var inZoneFlag = true
    var changeCount = 0

    for (alertUiState in sortedAlerts) {
      val isInZone = alertUiState.distanceToAddress != null
      if (isInZone != inZoneFlag) {
        changeCount++
        assert(changeCount <= 1) { "Alert list is not properly sorted by proximity" }
        inZoneFlag = false
      }
    }
  }

  @Test
  fun overviewScreen_showsAndHidesLoadingOverlay_duringLoadAlerts() {
    val alertRepo = FakeAlertRepository(delayMs = DEFAULT_TIMEOUT)
    val reportRepo = FakeReportRepository(delayMs = DEFAULT_TIMEOUT)

    val viewModel = OverviewViewModel(reportRepository = reportRepo, alertRepository = alertRepo)

    setContent {
      OverviewScreen(userRole = UserRole.FARMER, user = farmer1, overviewViewModel = viewModel)
    }

    composeTestRule.assertOverlayDuringLoading(
        isLoading = {
          viewModel.uiState.value.isAlertLoading || viewModel.uiState.value.isReportLoading
        })
  }

  @Test
  fun assigneeFilterDropdown_isDisplayedAndSelectable() {
    setVetScreen()

    with(OverviewScreenTestTags) {
      clickOn(FILTERS_TOGGLE)

      clickOn(ASSIGNEE_FILTER)
      clickOnText("Assigned to Me")
      textContains(ASSIGNEE_FILTER, "Assigned to Me")

      clickOn(ASSIGNEE_FILTER)
      clickOnText("Unassigned")
      textContains(ASSIGNEE_FILTER, "Unassigned")

      clickOn(ASSIGNEE_FILTER)
      clickOnText("Assigned to Others")
      textContains(ASSIGNEE_FILTER, "Assigned to Others")
    }
  }
}
