package com.android.agrihealth.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.android.agrihealth.data.model.user.UserDirectoryDataSource
import com.android.agrihealth.data.model.user.UserDirectoryRepository
import com.android.agrihealth.data.model.user.UserRole
import org.junit.Rule
import org.junit.Test

class AuthorNameTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

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

  @Test
  fun showsUnassigned_whenUidIsNull() {
    val vm = AuthorNameViewModel(repo = FakeUserDirectoryDataSource())
    composeRule.setContent { AuthorName(uid = null, showRole = true, viewModel = vm) }
    composeRule.waitUntil(2_000) {
      composeRule.onAllNodes(hasText("Unassigned")).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText("Unassigned").assertIsDisplayed()
  }

  @Test
  fun showsNameAndRole_whenUserExists_andShowRoleTrue() {
    val vm = AuthorNameViewModel(repo = FakeUserDirectoryDataSource())
    composeRule.setContent { AuthorName(uid = "vet_1", showRole = true, viewModel = vm) }
    composeRule.waitUntil(2_000) {
      composeRule.onAllNodes(hasText("Nico Vet (Vet)")).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText("Nico Vet (Vet)").assertIsDisplayed()
  }

  @Test
  fun showsNameOnly_whenUserExists_andShowRoleFalse() {
    val vm = AuthorNameViewModel(repo = FakeUserDirectoryDataSource())
    composeRule.setContent { AuthorName(uid = "farmer_1", showRole = false, viewModel = vm) }
    composeRule.waitUntil(2_000) {
      composeRule.onAllNodes(hasText("Fara Mer")).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText("Fara Mer").assertIsDisplayed()
  }

  @Test
  fun showsDeletedUser_whenUserMissing() {
    val vm = AuthorNameViewModel(repo = FakeUserDirectoryDataSource())
    composeRule.setContent { AuthorName(uid = "missing", showRole = true, viewModel = vm) }
    composeRule.waitUntil(2_000) {
      composeRule.onAllNodes(hasText("Deleted user")).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText("Deleted user").assertIsDisplayed()
  }
}
