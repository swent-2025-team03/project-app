package com.android.agrihealth.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.testutil.FakeOfficeRepository
import com.android.agrihealth.testutil.TestConstants
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OfficeNameTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var fakeRepo: FakeOfficeRepository

  @Before
  fun setup() {
    fakeRepo =
        FakeOfficeRepository(
            initialOffices =
                listOf(Office(id = "office", name = "name", address = null, ownerId = "uid")))
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
}
