package com.android.agrihealth.data.model.authentification

import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PermissionsFirestoreTest : FirebaseEmulatorsTest() {
  val auth = FirebaseAuth.getInstance()

  @Before
  override fun setUp() {
    super.setUp()
    authRepository.signOut()
  }

  private fun createAccount(user: User, password: String) = runTest {
    authRepository.signUpWithEmailAndPassword(user.email, password, user)
    assertNotNull(auth.currentUser)
  }

  private fun checkFirestorePermissionDenied(result: Result<User>) = runTest {
    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertNotNull(exception)
    assertTrue(exception is FirebaseFirestoreException)
    assertEquals(
        (exception as FirebaseFirestoreException).code,
        FirebaseFirestoreException.Code.PERMISSION_DENIED)
  }

  @Test
  fun canAccessOwnData() = runTest {
    createAccount(user1, password1)

    val userData = userRepository.getUserFromId(user1.uid).getOrThrow()
    assertEquals(user1.uid, userData.uid)
    assertEquals(user1.firstname, userData.firstname)
    assertEquals(user1.lastname, userData.lastname)
    assertEquals(user1.role, userData.role)
    assertEquals(user1.email, userData.email)
  }

  @Test
  fun failToAccessDataWhileLoggedOut() = runTest {
    createAccount(user1, password1)
    authRepository.signOut()

    val result = userRepository.getUserFromId(user1.uid)
    checkFirestorePermissionDenied(result)
  }

  @Test
  fun failToAccessOtherUsersData() = runTest {
    createAccount(user2, password2)
    authRepository.signOut()
    createAccount(user1, password1)

    val result = userRepository.getUserFromId(user2.uid)
    checkFirestorePermissionDenied(result)
  }

  @Test
  fun canUpdateName() = runTest {
    createAccount(user1, password1)
    userRepository.updateUser(
        user1.copy(firstname = "new", lastname = "name", email = "newemail@thing.com"))
  }

  @Test
  fun failToUpdateOwnRole() = runTest {
    createAccount(user1, password1)

    try {
      userRepository.updateUser(
          Vet(user1.uid, user1.lastname, user1.firstname, user1.email, user1.address))
      fail("User should not be able to change their role")
    } catch (e: IllegalArgumentException) {
      assertEquals(e.message, "Permission denied")
    }
  }

  @Test
  fun failToUpdateOwnUid() = runTest {
    createAccount(user1, password1)

    try {
      // A bit counter intuitive, this will return PERMISSION_DENIED because updateUser() tries to
      // getUserFromUid() first, and user is not allowed to get others
      userRepository.updateUser(user1.copy(uid = "newUid"))
      fail("User should not be able to change their uid")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(e.code, FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }
  }

  @Test
  fun canDeleteAccount() = runTest {
    createAccount(user1, password1)
    val result = authRepository.deleteAccount()
    assertTrue(result.isSuccess)
  }

  @Test
  fun failToDeleteWhileLoggedOut() = runTest {
    createAccount(user1, password1)

    authRepository.signOut()
    assertNull(auth.currentUser)

    val result = authRepository.deleteAccount()
    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertNotNull(exception)
    assertTrue(exception is NullPointerException)
    assertEquals(exception!!.message, "User not logged in")
  }
}
