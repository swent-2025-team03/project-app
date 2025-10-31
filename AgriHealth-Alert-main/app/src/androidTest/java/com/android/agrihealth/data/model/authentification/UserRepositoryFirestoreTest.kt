package com.android.agrihealth.data.model.authentification

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class UserRepositoryFirestoreTest : FirebaseEmulatorsTest() {

  @Before
  override fun setUp() {
    super.setUp()
    runTest { authRepository.signUpWithEmailAndPassword(user1.email, password1, user1) }
  }

  @Test
  fun canGetCorrectUserData() = runTest {
    userRepository.addUser(user1)
    val userData = userRepository.getUserFromId(user1.uid).getOrThrow()
    assertEquals(user1.uid, userData.uid)
    assertEquals(user1.firstname, userData.firstname)
    assertEquals(user1.lastname, userData.lastname)
    assertEquals(user1.role, userData.role)
    assertEquals(user1.email, userData.email)
  }

  @Test
  fun canUpdateSingleField() = runTest {
    userRepository.addUser(user1)
    val newEmail = "newemail@thing.com"
    userRepository.updateUser(user1.copy(email = newEmail))
    val userData = userRepository.getUserFromId(user1.uid).getOrThrow()
    assertEquals(userData.email, newEmail)
  }

  @Test
  fun canUpdateMultipleFields() = runTest {
    userRepository.addUser(user1)
    val newEmail = "newemail@thing.com"
    val newName = "newName"
    userRepository.updateUser(user1.copy(email = newEmail, firstname = newName))
    val userData = userRepository.getUserFromId(user1.uid).getOrThrow()
    assertEquals(userData.email, newEmail)
    assertEquals(userData.firstname, newName)
  }

  @Test
  fun canDeleteAccountData() = runTest {
    userRepository.addUser(user1)
    val userData = userRepository.getUserFromId(user1.uid).getOrThrow()
    assertNotNull(userData)
    userRepository.deleteUser(user1.uid)
    val result = userRepository.getUserFromId(user1.uid).exceptionOrNull()
    assertNotNull(result) // not a success
    assertTrue(result is NullPointerException)
    assertEquals(result?.message, "No such user found")
  }

  @Test
  fun failToUpdateNonExistingAccount() = runTest {
    userRepository.deleteUser(user1.uid)
    try {
      userRepository.updateUser(user1)
      fail("Expected FirebaseFirestoreException, but code ran fine")
    } catch (e: NullPointerException) {
      assertEquals(e.message, "No such user found")
    }
  }
}
