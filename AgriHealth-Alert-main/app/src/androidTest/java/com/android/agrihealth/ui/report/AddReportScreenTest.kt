package com.android.agrihealth.ui.report

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testutil.FakeAddReportViewModel
import com.android.agrihealth.ui.loading.LoadingTestTags
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

private fun fakeFarmerViewModel(): UserViewModel {
  return object : UserViewModel() {
    private val fakeUserFlow =
        MutableStateFlow(
            Farmer(
                uid = "test_user",
                firstname = "Farmer",
                lastname = "Joe",
                email = "email@email.com",
                address = Location(0.0, 0.0, "123 Farm Lane"),
                linkedVets = listOf("Best Vet Ever!", "Meh Vet", "Great Vet"),
                defaultVet = null))

    override var user: StateFlow<User> = fakeUserFlow.asStateFlow()
  }
}

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
    val fakeUserViewModel = fakeFarmerViewModel()

    composeRule.setContent {
      MaterialTheme {
        AddReportScreen(
            userViewModel = fakeUserViewModel,
            userRole = UserRole.FARMER,
            userId = "test_user",
            onCreateReport = {},
            addReportViewModel = FakeAddReportViewModel())
      }
    }

    composeRule.onNodeWithTag(AddReportScreenTestTags.VET_DROPDOWN).performClick()
    val firstVet = "Best Vet Ever!"
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


    @Test
    fun createReport_showsLoadingOverlay() {
        // Fake ViewModel that simulates slow repository
        val slowViewModel = object : FakeAddReportViewModel() {
            override suspend fun createReport(): Boolean {
                _uiState.value = _uiState.value.copy(isLoading = true)
                kotlinx.coroutines.delay(1200) // simulate slow Firestore
                _uiState.value = _uiState.value.copy(isLoading = false)
                return true
            }
        }

        composeRule.setContent {
            MaterialTheme {
                AddReportScreen(
                    userRole = UserRole.FARMER,
                    userId = "test_user",
                    onCreateReport = {},
                    addReportViewModel = slowViewModel
                )
            }
        }

        // Initially overlay not visible
        composeRule.onNodeWithTag(LoadingTestTags.SCRIM).assertDoesNotExist()
        composeRule.onNodeWithTag(LoadingTestTags.SPINNER).assertDoesNotExist()

        // Enter valid input
        composeRule.onNodeWithTag(AddReportScreenTestTags.TITLE_FIELD)
            .performTextInput("Slow Test Title")
        composeRule.onNodeWithTag(AddReportScreenTestTags.DESCRIPTION_FIELD)
            .performTextInput("Desc")

        // Click create
        composeRule.onNodeWithTag(AddReportScreenTestTags.CREATE_BUTTON).performClick()

        // Wait until loading state becomes true (defensive, though immediate)
        composeRule.waitUntil(timeoutMillis = 1000) { slowViewModel.uiState.value.isLoading }

        // Assert that loading overlay appears
        composeRule.onNodeWithTag(LoadingTestTags.SCRIM).assertIsDisplayed()
        composeRule.onNodeWithTag(LoadingTestTags.SPINNER).assertIsDisplayed()

        // Wait until loading finishes
        composeRule.waitUntil(timeoutMillis = 3000) { !slowViewModel.uiState.value.isLoading }

        // Overlay should be gone
        composeRule.onNodeWithTag(LoadingTestTags.SCRIM).assertDoesNotExist()
        composeRule.onNodeWithTag(LoadingTestTags.SPINNER).assertDoesNotExist()
    }

}

