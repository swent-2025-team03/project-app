package com.android.agrihealth.ui.navigation

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.agrihealth.AgriHealthApp
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.authentification.verifyUser
import com.android.agrihealth.testhelpers.TestPassword.password1
import com.android.agrihealth.testhelpers.TestTimeout.DEFAULT_TIMEOUT
import com.android.agrihealth.testhelpers.TestTimeout.SHORT_TIMEOUT
import com.android.agrihealth.testhelpers.TestTimeout.SUPER_LONG_TIMEOUT
import com.android.agrihealth.testhelpers.TestUser.farmer1
import com.android.agrihealth.testhelpers.templates.FirebaseUITest
import com.android.agrihealth.ui.authentification.SignInScreenTestTags
import com.android.agrihealth.ui.authentification.VerifyEmailScreenTestTags
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.map.MapScreenTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.overview.OverviewScreenTestTags
import com.android.agrihealth.ui.planner.PlannerScreenTestTags
import com.android.agrihealth.ui.profile.ProfileScreenTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.lang.Thread.sleep
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * First implementation of navigation tests. Doesn't cover all auth and assumes that the first
 * screen of AgrihealthApp is Overview.
 */
class NavigationTest :
    FirebaseUITest(
        grantedPermissions =
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS)) {

  @Before
  fun setUp() = runTest {
    // Set the content to the Overview screen before each test
    val repository = AuthRepositoryProvider.repository
    repository.signUpWithEmailAndPassword(farmer1.email, password1, farmer1)
    assert(Firebase.auth.currentUser != null)
    verifyUser(Firebase.auth.uid!!)
    setContent { AgriHealthApp() }

    nodeNotDisplayed(VerifyEmailScreenTestTags.WELCOME, SUPER_LONG_TIMEOUT)
    clickOn(NavigationTestTags.GO_BACK_BUTTON)
    clickOn(NavigationTestTags.GO_BACK_BUTTON)
    textContains(NavigationTestTags.TOP_BAR_TITLE, Screen.Overview.name)
  }

  @Test
  fun overviewScreen() {
    with(NavigationTestTags) {
      assertOnOverviewScreen()

      // Bottom bar
      nodeIsDisplayed(BOTTOM_NAVIGATION_MENU)

      clickOn(PLANNER_TAB)
      nodeIsDisplayed(PlannerScreenTestTags.SCREEN)

      clickOn(MAP_TAB)
      nodeIsDisplayed(MapScreenTestTags.GOOGLE_MAP_SCREEN)

      clickOn(OVERVIEW_TAB)
      assertOnOverviewScreen()

      // Reports
      clickOn(OverviewScreenTestTags.ADD_REPORT_BUTTON)
      textContains(TOP_BAR_TITLE, Screen.AddReport.name)

      clickOn(GO_BACK_BUTTON)
      assertOnOverviewScreen()

      // Profile
      clickOn(OverviewScreenTestTags.PROFILE_BUTTON)
      textContains(TOP_BAR_TITLE, Screen.Profile.name)

      clickOn(GO_BACK_BUTTON)
      assertOnOverviewScreen()

      // Log out
      clickOn(OverviewScreenTestTags.LOGOUT_BUTTON)
      nodeIsDisplayed(SignInScreenTestTags.SCREEN)
    }
  }

  private fun assertOnOverviewScreen() =
    textContains(NavigationTestTags.TOP_BAR_TITLE, Screen.Overview.name)

  override fun displayAllComponents() {}
}
