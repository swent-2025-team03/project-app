package com.android.agrihealth.ui.authentification

import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.ui.user.defaultUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

  val user1 = Farmer("abc123", "Rushia", "Uruha", "email1@thing.com", null, emptyList(), null)
  val user3 = Vet("ghi789", "Nazuna", "Amemiya", "email3@kms.josh", null)

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repository: FakeUserRepository

  val auth = FirebaseAuth.getInstance()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    repository = FakeUserRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initKeepsDefaultUserWhenNoUserLoggedIn() = runTest {
    // Given no current user
    assertEquals(auth.currentUser, null)

    val viewModel = UserViewModel(repository, auth)
    advanceUntilIdle()

    // Default role = FARMER
    val role = viewModel.user.first()
    assertEquals(defaultUser, role)
  }

  @Test
  fun loadUserUpdatesStateFlow() = runTest {
    // Given a user in the repository
    repository.addUser(user3)

    val viewModel = UserViewModel(repository, auth)
    advanceUntilIdle()

    // When loading user role
    viewModel.loadUser(user3.uid)
    advanceUntilIdle()

    // Then userRole StateFlow should be updated
    val role = viewModel.user.first()
    assertEquals(user3, role)
  }

  @Test
  fun loadUserHandlesNonExistingUser() = runTest {
    // Given no user in the repository

    val viewModel = UserViewModel(repository, auth)
    advanceUntilIdle()

    // When loading user role for non-existing user
    viewModel.loadUser("nonExistingUid")
    advanceUntilIdle()

    // Then userRole StateFlow should remain default
    val role = viewModel.user.first()
    assertEquals(defaultUser, role)
  }
}

class FakeUserRepository : UserRepository {
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

  override suspend fun getVetsInOffice(officeId: String): List<String> {
    return listOf()
  }
}
