package com.android.agrihealth.zztemp.data.authentification

import com.android.agrihealth.data.model.authentification.AuthRepositoryFirebase
import com.android.agrihealth.data.model.user.UserRepositoryFirestore
import com.android.agrihealth.testhelpers.TestPassword
import com.android.agrihealth.testhelpers.TestUser
import com.android.agrihealth.testhelpers.templates.FirebaseTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class UserRepositoryFirestoreTest : FirebaseTest() {

  val authRepository = AuthRepositoryFirebase()
  val userRepository = UserRepositoryFirestore()

  val user = TestUser.FARMER1.copy()
  val password = TestPassword.PASSWORD1

  @Before
  fun setUp() {
    runTest { authRepository.signUpWithEmailAndPassword(user.email, password, user) }
  }

  @Test
  fun canGetCorrectUserData() = runTest {
    userRepository.addUser(user)
    val userData = userRepository.getUserFromId(user.uid).getOrThrow()
    assertEquals(user.uid, userData.uid)
    assertEquals(user.firstname, userData.firstname)
    assertEquals(user.lastname, userData.lastname)
    assertEquals(user.role, userData.role)
    assertEquals(user.email, userData.email)
  }

  @Test
  fun canUpdateSingleField() = runTest {
    userRepository.addUser(user)
    val newEmail = "newemail@thing.com"
    userRepository.updateUser(user.copy(email = newEmail))
    val userData = userRepository.getUserFromId(user.uid).getOrThrow()
    assertEquals(userData.email, newEmail)
  }

  @Test
  fun canUpdateMultipleFields() = runTest {
    userRepository.addUser(user)
    val newEmail = "newemail@thing.com"
    val newName = "newName"
    userRepository.updateUser(user.copy(email = newEmail, firstname = newName))
    val userData = userRepository.getUserFromId(user.uid).getOrThrow()
    assertEquals(userData.email, newEmail)
    assertEquals(userData.firstname, newName)
  }

  @Test
  fun canDeleteAccountData() = runTest {
    userRepository.addUser(user)
    val userData = userRepository.getUserFromId(user.uid).getOrThrow()
    assertNotNull(userData)
    userRepository.deleteUser(user.uid)
    val result = userRepository.getUserFromId(user.uid).exceptionOrNull()
    assertNotNull(result) // not a success
    assertTrue(result is NullPointerException)
    assertEquals(result?.message, "No such user found")
  }

  @Test
  fun failToUpdateNonExistingAccount() = runTest {
    userRepository.deleteUser(user.uid)
    try {
      userRepository.updateUser(user)
      fail("Expected FirebaseFirestoreException, but code ran fine")
    } catch (e: NullPointerException) {
      assertEquals(e.message, "No such user found")
    }
  }
}
