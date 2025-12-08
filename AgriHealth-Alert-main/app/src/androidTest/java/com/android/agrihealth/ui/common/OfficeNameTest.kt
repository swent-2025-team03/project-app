package com.android.agrihealth.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.testutil.FakeOfficeRepository
import com.android.agrihealth.testutil.TestConstants
import org.junit.Rule
import org.junit.Test

class OfficeNameTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val fakeRepo =
      FakeOfficeRepository(
          initialOffices =
              listOf(Office(id = "office", name = "name", address = null, ownerId = "uid")))

  private fun setContentWithRemember(officeId: String?) {
    composeRule.setContent {
      val vm = OfficeNameViewModel(repository = fakeRepo)
      val officeName = with(vm) { rememberOfficeName(officeId) }
      Text(officeName)
    }
  }

  private fun setOfficeNameContent(uid: String?) {
    composeRule.setContent {
      OfficeName(uid = uid, vm = OfficeNameViewModel(repository = fakeRepo))
    }
  }

  @Test
  fun showsNoneOfficeWhenUidIsNull() {
    setOfficeNameContent(null)

    composeRule.waitUntil(TestConstants.SHORT_TIMEOUT) {
      composeRule.onNodeWithText("Not assigned to an office").isDisplayed()
    }
  }

  @Test
  fun showsOfficeNameWhenOfficeExists() {
    setOfficeNameContent("office")

    composeRule.waitUntil(TestConstants.SHORT_TIMEOUT) {
      composeRule.onNodeWithText("name").isDisplayed()
    }
  }

  @Test
  fun showsDeletedOfficeWhenOfficeDoesNotExist() {
    setOfficeNameContent("off1ce")

    composeRule.waitUntil(TestConstants.SHORT_TIMEOUT) {
      composeRule.onNodeWithText("Deleted office").isDisplayed()
    }
  }

  @Test
  fun rememberOfficeName_showsNoneOfficeWhenIdIsNull() {
    setContentWithRemember(null)

    composeRule.waitUntil(TestConstants.SHORT_TIMEOUT) {
      composeRule.onNodeWithText("Not assigned to an office").isDisplayed()
    }
  }

  @Test
  fun rememberOfficeName_showsDeletedOfficeWhenOfficeMissing() {
    setContentWithRemember("missingOffice")

    composeRule.waitUntil(TestConstants.SHORT_TIMEOUT) {
      composeRule.onNodeWithText("Deleted office").isDisplayed()
    }
  }
}
