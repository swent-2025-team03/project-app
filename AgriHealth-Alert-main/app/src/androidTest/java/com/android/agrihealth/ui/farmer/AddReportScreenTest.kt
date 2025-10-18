package com.android.agrihealth.ui.farmer

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class AddReportScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun displayAllFieldsAndButtons() {
    composeRule.setContent { MaterialTheme { AddReportScreen() } }
    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun createButton_showsSnackbar_onEmptyFields() {
    composeRule.setContent { MaterialTheme { AddReportScreen() } }
    // Click with fields empty
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()
    composeRule.onNodeWithText(AddReportFeedbackTexts.FAILURE).assertIsDisplayed()
  }

  @Test
  fun enteringTitleDescription_canCreateReport_showsSuccessSnackbar() {
    composeRule.setContent { MaterialTheme { AddReportScreen() } }
    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).performTextInput("Report Title")
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD)
        .performTextInput("Something happened")
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()
    composeRule.onNodeWithText(AddReportFeedbackTexts.SUCCESS).assertIsDisplayed()
  }

  @Test
  fun selectingVet_updatesDisplayedOption() {
    composeRule.setContent { MaterialTheme { AddReportScreen() } }
    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).performClick()
    val firstVet = AddReportConstants.vetOptions[0]
    composeRule.onNodeWithText(firstVet).assertIsDisplayed().performClick()
    composeRule.onNodeWithText(firstVet).assertIsDisplayed()
  }

  @Test
  fun previewComposable_rendersWithoutCrash() {
    composeRule.setContent { AddReportScreenPreview() }

    // Verify that essential UI components render (sample check)
    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
  }
}
