package com.android.agrihealth.ui.farmer

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testutil.FakeAddReportViewModel
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class AddReportScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun displayAllFieldsAndButtons() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
      }
    }
    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).assertIsDisplayed()
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun createButton_showsSnackbar_onEmptyFields() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
      }
    }
    // Click with fields empty
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()
    composeRule.onNodeWithText(AddReportFeedbackTexts.FAILURE).assertIsDisplayed()
  }

  @Test
  fun selectingVet_updatesDisplayedOption() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
      }
    }
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

  @Test
  fun enteringTitleDescription_showsSuccessDialog() {
    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
      }
    }

    // Fill in valid fields
    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).performTextInput("Title")
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD)
        .performTextInput("Description")

    // Click create
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()

    // Check that dialog appears
    composeRule.onNodeWithText(AddReportFeedbackTexts.SUCCESS).assertIsDisplayed()
    composeRule.onNodeWithText("OK").assertIsDisplayed()
  }

  @Test
  fun dismissingDialog_callsOnCreateReport() {
    var called = false

    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = { called = true },
            addReportViewModel = FakeAddReportViewModel())
      }
    }

    composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD).performTextInput("Valid Title")
    composeRule
        .onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD)
        .performTextInput("Some description")
    composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()

    composeRule.onNodeWithText("OK").assertIsDisplayed()
    composeRule.onNodeWithText("OK").performClick()

    Assert.assertTrue(called)
  }
}
