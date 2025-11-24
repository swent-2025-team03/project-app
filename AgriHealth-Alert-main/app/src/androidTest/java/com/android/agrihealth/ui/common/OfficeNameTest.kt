package com.android.agrihealth.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepositoryProvider
import com.android.agrihealth.testutil.TestConstants
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OfficeNameTest() : FirebaseEmulatorsTest() {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    super.setUp()
    val vet = user3.copy(officeId = "office")
    var office = Office("office", "name", null, ownerId = vet.uid)
    val authRepo = AuthRepositoryProvider.repository
    val officeRepo = OfficeRepositoryProvider.get()
    runTest {
      authRepo.signUpWithEmailAndPassword(vet.email, "123456", vet)
      office = office.copy(ownerId = Firebase.auth.uid!!)
      officeRepo.addOffice(office)
    }
  }

  @Test
  fun showsNoneOfficeWhenUidIsNull() {
    composeRule.setContent { OfficeName(null) }
    composeRule.waitUntil(TestConstants.SHORT_TIMEOUT) {
      composeRule.onNodeWithText("Not assigned to an office").isDisplayed()
    }
  }

  @Test
  fun showsOfficeNameWhenOfficeExists() {
    composeRule.setContent { OfficeName("office") }
    composeRule.waitUntil(TestConstants.SHORT_TIMEOUT) {
      composeRule.onNodeWithText("name").isDisplayed()
    }
  }

  @Test
  fun showsDeletedOfficeWhenOfficeDoesNotExist() {
    composeRule.setContent { OfficeName("off1ce") }
    composeRule.waitUntil(TestConstants.SHORT_TIMEOUT) {
      composeRule.onNodeWithText("Deleted office").isDisplayed()
    }
  }
}
