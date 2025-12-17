package com.android.agrihealth.zztemp.ui.common

import androidx.compose.material3.Text
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.testhelpers.fakes.FakeOfficeRepository
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.common.resolver.OfficeName
import com.android.agrihealth.ui.common.resolver.OfficeNameViewModel
import com.android.agrihealth.ui.common.resolver.rememberOfficeName
import org.junit.Test

class OfficeNameTest : UITest() {
  private val fakeRepo =
      FakeOfficeRepository(
          initialOffices =
              listOf(Office(id = "office", name = "name", address = null, ownerId = "uid")))

  private val vm = OfficeNameViewModel(repository = fakeRepo)

  private fun setOfficeNameContent(uid: String?) = setContent { OfficeName(uid = uid, vm = vm) }

  override fun displayAllComponents() {}

  @Test
  fun showsNoneOfficeWhenUidIsNull() {
    setOfficeNameContent(null)
    textIsDisplayed("Not assigned to an office")
  }

  @Test
  fun showsOfficeNameWhenOfficeExists() {
    setOfficeNameContent("office")
    textIsDisplayed("name")
  }

  @Test
  fun showsDeletedOfficeWhenOfficeDoesNotExist() {
    setOfficeNameContent("missingOffice")
    textIsDisplayed("Deleted office")
  }

  private fun setContentWithRemember(officeId: String?) {
    setContent {
      val officeName = rememberOfficeName(officeId)
      Text(officeName)
    }
  }

  @Test
  fun rememberOfficeName_showsNoneOfficeWhenIdIsNull() {
    setContentWithRemember(null)
    textIsDisplayed("Not assigned to an office")
  }

  @Test
  fun rememberOfficeName_showsDeletedOfficeWhenOfficeMissing() {
    setContentWithRemember("missingOffice")
    textIsDisplayed("Deleted office")
  }
}
