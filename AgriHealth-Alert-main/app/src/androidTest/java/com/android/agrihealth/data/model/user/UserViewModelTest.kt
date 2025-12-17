package com.android.agrihealth.data.model.user

import com.android.agrihealth.testhelpers.TestUser
import com.android.agrihealth.testhelpers.templates.FirebaseTest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest : FirebaseTest() {

  private val repository = FakeUserRepository()

  val auth = FirebaseAuth.getInstance()

  val vet = TestUser.VET1.copy()

  @Before
  fun signOut() {
    auth.signOut()
  }

  @Test
  fun initKeepsDefaultUserWhenNoUserLoggedIn() = runTest {
    // Given no current user
    assertEquals(auth.currentUser, null)

    val viewModel = UserViewModel(repository, auth)
    advanceUntilIdle()

    // Default role = FARMER
    val role = viewModel.uiState.value.user
    assertEquals(defaultUser, role)
  }

  @Test
  fun loadUserUpdatesStateFlow() = runTest {
    // Given a user in the repository
    repository.addUser(vet)

    val viewModel = UserViewModel(repository, auth)
    advanceUntilIdle()

    // When loading user role
    viewModel.loadUser(vet.uid)
    advanceUntilIdle()

    // Then user State should be updated
    val role = viewModel.uiState.value.user
    assertEquals(vet, role)
  }

  @Test
  fun loadUserHandlesNonExistingUser() = runTest {
    // Given no user in the repository

    val viewModel = UserViewModel(repository, auth)
    advanceUntilIdle()

    // When loading user role for non-existing user
    viewModel.loadUser("nonExistingUid")
    advanceUntilIdle()

    // Then user State should remain default
    val role = viewModel.uiState.value.user
    assertEquals(defaultUser, role)
  }
}

private class FakeUserRepository : UserRepository {
  private val users = mutableMapOf<String, User>()

  override suspend fun getUserFromId(uid: String): Result<User> {
    val user = users[uid]
    return if (user != null) Result.success(user) else Result.failure(Exception("User not found"))
  }

  override suspend fun addUser(user: User) {
    users[user.uid] = user
  }

  override suspend fun updateUser(user: User) {}

  override suspend fun deleteUser(uid: String) {}
}
