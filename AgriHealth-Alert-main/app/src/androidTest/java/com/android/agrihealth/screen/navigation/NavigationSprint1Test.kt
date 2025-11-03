package com.android.agrihealth.screen.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.agrihealth.AgriHealthApp
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.ui.authentification.SignInScreenTestTags
import com.android.agrihealth.ui.map.MapScreenTestTags
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.lang.Thread.sleep
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * First implementation of navigation tests. Doesn't cover all auth and assumes that the first
 * screen of AgrihealthApp is Overview.
 */
class NavigationSprint1Test : FirebaseEmulatorsTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  override fun setUp() {
    // Set the content to the Overview screen before each test
    super.setUp()
    val repository = AuthRepositoryProvider.repository
    runTest { repository.signUpWithEmailAndPassword("navigation@test.ff", "123456", user1) }
    assert(Firebase.auth.currentUser != null)
    composeTestRule.setContent { AgriHealthApp() }
    sleep(2000)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains(Screen.Overview.name)
  }

  @Test
  fun overviewScreen_displaysCorrectTitle() {
    // Assert that the title of the top bar is "Overview"
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertIsDisplayed()
        .assertTextContains(Screen.Overview.name)
  }

  @Test
  fun overviewScreen_displaysBottomBar() {
    // Assert that the bottom navigation bar is displayed
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).isDisplayed()
    }
  }

  @Test
  fun overviewScreen_navigateToMap() {
    // Click on the Map tab in the bottom navigation bar
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).performClick()
    // Assert that the Map screen is displayed
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).isDisplayed()
    }
  }

  @Test
  fun overviewScreen_navigateToAddReport() {
    // Click on the "Add Report" button
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ADD_REPORT_BUTTON).performClick()
    // Assert that the Add Report screen is displayed
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(Screen.AddReport.name).isDisplayed()
    }
  }

  @Test
  fun overviewScreen_navigateToAuth() {
    // Click on the "Sign Out" button
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.LOGOUT_BUTTON).performClick()
    // Assert that the Auth screen is displayed
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).isDisplayed()
    }
  }

  @Test
  fun addReportScreen_goBackToOverview() {
    // Navigate to the Add Report screen
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ADD_REPORT_BUTTON).performClick()
    // Click on the "Go Back" button
    composeTestRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).performClick()
    // Assert that the Overview screen is displayed
    sleep(2000)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains(Screen.Overview.name)
  }

  @Test
  fun addReportScreen_navigateToOverviewUsingSystemBack() {
    // Navigate to the Add Report screen
    composeTestRule.onNodeWithTag(OverviewScreenTestTags.ADD_REPORT_BUTTON).performClick()
    composeTestRule.waitUntil(3000) {
      composeTestRule.onNodeWithText(Screen.AddReport.name).isDisplayed()
    }
    // Simulate system back press
    pressBack(false)
    // Assert that the Overview screen is displayed
    sleep(2000)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertTextContains(Screen.Overview.name)
  }

  private fun pressBack(shouldFinish: Boolean) {
    composeTestRule.activityRule.scenario.onActivity { activity ->
      activity.onBackPressedDispatcher.onBackPressed()
    }
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.activity.isFinishing == shouldFinish
    }
    TestCase.assertEquals(shouldFinish, composeTestRule.activity.isFinishing)
  }
}
