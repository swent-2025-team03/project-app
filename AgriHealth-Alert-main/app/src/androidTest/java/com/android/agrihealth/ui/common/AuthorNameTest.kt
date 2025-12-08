package com.android.agrihealth.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
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

  companion object {
    private const val WAIT_TIMEOUT = 2_000L
  }

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
    composeRule.setContent { AuthorName(uid = null, viewModel = vm) }
    composeRule.waitUntil(WAIT_TIMEOUT) {
      composeRule.onAllNodes(hasText("Unassigned")).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText("Unassigned").assertIsDisplayed()
  }

  @Test
  fun showsNameOnly_whenUserExists_andShowRoleFalse() {
    val vm = AuthorNameViewModel(repo = FakeUserDirectoryDataSource())
    composeRule.setContent { AuthorName(uid = "farmer_1", viewModel = vm) }
    composeRule.waitUntil(WAIT_TIMEOUT) {
      composeRule.onAllNodes(hasText("Fara Mer")).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText("Fara Mer").assertIsDisplayed()
  }

  @Test
  fun showsDeletedUser_whenUserMissing() {
    val vm = AuthorNameViewModel(repo = FakeUserDirectoryDataSource())
    composeRule.setContent { AuthorName(uid = "missing", viewModel = vm) }
    composeRule.waitUntil(WAIT_TIMEOUT) {
      composeRule.onAllNodes(hasText("Deleted user")).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText("Deleted user").assertIsDisplayed()
  }

  private fun setRememberUserNameContent(uid: String?) {
    val repo = FakeUserDirectoryDataSource()
    composeRule.setContent {
      val vm = AuthorNameViewModel(repo)
      val name = with(vm) { rememberUserName(uid) }
      Text(name)
    }
  }

  @Test
  fun rememberUserName_showsUnassigned_whenUidNull() {
    setRememberUserNameContent(null)

    composeRule.waitUntil(WAIT_TIMEOUT) {
      composeRule.onAllNodes(hasText("Unassigned")).fetchSemanticsNodes().isNotEmpty()
    }

    composeRule.onNodeWithText("Unassigned").assertIsDisplayed()
  }

  @Test
  fun rememberUserName_showsDeletedUser_whenMissing() {
    setRememberUserNameContent("missing")

    composeRule.waitUntil(WAIT_TIMEOUT) {
      composeRule.onAllNodes(hasText("Deleted user")).fetchSemanticsNodes().isNotEmpty()
    }

    composeRule.onNodeWithText("Deleted user").assertIsDisplayed()
  }
}
