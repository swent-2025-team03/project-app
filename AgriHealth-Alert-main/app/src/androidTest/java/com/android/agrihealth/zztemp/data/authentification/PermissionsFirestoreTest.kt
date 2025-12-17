package com.android.agrihealth.zztemp.data.authentification

import com.android.agrihealth.data.model.authentification.AuthRepositoryFirebase
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRepositoryFirestore
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testhelpers.TestPassword
import com.android.agrihealth.testhelpers.TestUser
import com.android.agrihealth.testhelpers.templates.FirebaseTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PermissionsFirestoreTest : FirebaseTest() {
  val auth = FirebaseAuth.getInstance()
  val userRepository = UserRepositoryFirestore()
  val authRepository = AuthRepositoryFirebase()

  val farmer1 = TestUser.FARMER1.copy()
  val farmer2 = TestUser.FARMER2.copy()
  val password1 = TestPassword.PASSWORD1
  val password2 = TestPassword.PASSWORD2

  @Before
  fun setUp() {
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
    createAccount(farmer1, password1)

    val userData = userRepository.getUserFromId(farmer1.uid).getOrThrow()
    assertEquals(farmer1.uid, userData.uid)
    assertEquals(farmer1.firstname, userData.firstname)
    assertEquals(farmer1.lastname, userData.lastname)
    assertEquals(farmer1.role, userData.role)
    assertEquals(farmer1.email, userData.email)
  }

  @Test
  fun failToAccessDataWhileLoggedOut() = runTest {
    createAccount(farmer1, password1)
    authRepository.signOut()

    val result = userRepository.getUserFromId(farmer1.uid)
    checkFirestorePermissionDenied(result)
  }

  @Test
  fun failToAccessOtherUsersData() = runTest {
    createAccount(farmer2, password2)
    authRepository.signOut()
    createAccount(farmer1, password1)

    val result = userRepository.getUserFromId(farmer2.uid)
    checkFirestorePermissionDenied(result)
  }

  @Test
  fun canUpdateName() = runTest {
    createAccount(farmer1, password1)
    userRepository.updateUser(
        farmer1.copy(firstname = "new", lastname = "name", email = "newemail@thing.com"))
  }

  @Test
  fun failToUpdateOwnRole() = runTest {
    createAccount(farmer1, password1)

    try {
      userRepository.updateUser(
          Vet(farmer1.uid, farmer1.lastname, farmer1.firstname, farmer1.email, farmer1.address))
      fail("User should not be able to change their role")
    } catch (e: IllegalArgumentException) {
      assertEquals(e.message, "Permission denied")
    }
  }

  @Test
  fun failToUpdateOwnUid() = runTest {
    createAccount(farmer1, password1)

    try {
      // A bit counter intuitive, this will return PERMISSION_DENIED because updateUser() tries to
      // getUserFromUid() first, and user is not allowed to get others
      userRepository.updateUser(farmer1.copy(uid = "newUid"))
      fail("User should not be able to change their uid")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(e.code, FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }
  }

  @Test
  fun canDeleteAccount() = runTest {
    createAccount(farmer1, password1)
    val result = authRepository.deleteAccount()
    assertTrue(result.isSuccess)
  }

  @Test
  fun failToDeleteWhileLoggedOut() = runTest {
    createAccount(farmer1, password1)

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
