package com.android.agrihealth.zztemp.ui.common

import androidx.compose.material3.Text
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.common.resolver.AuthorName
import com.android.agrihealth.ui.common.resolver.AuthorNameViewModel
import com.android.agrihealth.ui.common.resolver.UserDirectoryDataSource
import com.android.agrihealth.ui.common.resolver.UserDirectoryRepository
import com.android.agrihealth.ui.common.resolver.rememberUserName
import org.junit.Test

class AuthorNameTest : UITest() {
  private class FakeUserDirectoryDataSource : UserDirectoryDataSource {
    override suspend fun getUserSummary(uid: String): UserDirectoryRepository.UserSummary? =
        when (uid) {
          "vet_1" ->
              UserDirectoryRepository.UserSummary(
                  uid = uid, firstname = "Nico", lastname = "Vet", role = UserRole.VET)
          "farmer_1" ->
              UserDirectoryRepository.UserSummary(
                  uid = uid, firstname = "Fara", lastname = "Mer", role = UserRole.FARMER)
          "missing" -> null
          else -> UserDirectoryRepository.UserSummary(uid, "John", "Doe", null)
        }
  }

  private val vm = AuthorNameViewModel(repo = FakeUserDirectoryDataSource())

  private fun setAuthorNameContent(uid: String?) = setContent { AuthorName(uid, viewModel = vm) }

  override fun displayAllComponents() {}

  @Test
  fun showsUnassigned_whenUidIsNull() {
    setAuthorNameContent(null)
    textIsDisplayed("Unassigned")
  }

  @Test
  fun showsNameOnly_whenUserExists_andShowRoleFalse() {
    setAuthorNameContent("farmer_1")
    textIsDisplayed("Fara Mer")
  }

  @Test
  fun showsDeletedUser_whenUserMissing() {
    setAuthorNameContent("missing")
    textIsDisplayed("Deleted user")
  }

  private fun setRememberUserNameContent(uid: String?) {
    setContent {
      val name = rememberUserName(uid)
      Text(name)
    }
  }

  @Test
  fun rememberUserName_showsUnassigned_whenUidNull() {
    setRememberUserNameContent(null)
    textIsDisplayed("Unassigned")
  }

  @Test
  fun rememberUserName_showsDeletedUser_whenMissing() {
    setRememberUserNameContent("missing")
    textIsDisplayed("Deleted user")
  }
}
