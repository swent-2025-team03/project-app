package com.android.agrihealth.data.model.authentification

import com.android.agrihealth.data.model.user.UserRepositoryFirestore
import com.android.agrihealth.testhelpers.TestPassword.password1
import com.android.agrihealth.testhelpers.TestUser.farmer1
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

  @Before
  fun setUp() {
    runTest { authRepository.signUpWithEmailAndPassword(farmer1.email, password1, farmer1) }
  }

  @Test
  fun canGetCorrectUserData() = runTest {
    userRepository.addUser(farmer1)
    val userData = userRepository.getUserFromId(farmer1.uid).getOrThrow()
    assertEquals(farmer1.uid, userData.uid)
    assertEquals(farmer1.firstname, userData.firstname)
    assertEquals(farmer1.lastname, userData.lastname)
    assertEquals(farmer1.role, userData.role)
    assertEquals(farmer1.email, userData.email)
  }

  @Test
  fun canUpdateSingleField() = runTest {
    userRepository.addUser(farmer1)
    val newEmail = "newemail@thing.com"
    userRepository.updateUser(farmer1.copy(email = newEmail))
    val userData = userRepository.getUserFromId(farmer1.uid).getOrThrow()
    assertEquals(userData.email, newEmail)
  }

  @Test
  fun canUpdateMultipleFields() = runTest {
    userRepository.addUser(farmer1)
    val newEmail = "newemail@thing.com"
    val newName = "newName"
    userRepository.updateUser(farmer1.copy(email = newEmail, firstname = newName))
    val userData = userRepository.getUserFromId(farmer1.uid).getOrThrow()
    assertEquals(userData.email, newEmail)
    assertEquals(userData.firstname, newName)
  }

  @Test
  fun canDeleteAccountData() = runTest {
    userRepository.addUser(farmer1)
    val userData = userRepository.getUserFromId(farmer1.uid).getOrThrow()
    assertNotNull(userData)
    userRepository.deleteUser(farmer1.uid)
    val result = userRepository.getUserFromId(farmer1.uid).exceptionOrNull()
    assertNotNull(result) // not a success
    assertTrue(result is NullPointerException)
    assertEquals(result?.message, "No such user found")
  }

  @Test
  fun failToUpdateNonExistingAccount() = runTest {
    userRepository.deleteUser(farmer1.uid)
    try {
      userRepository.updateUser(farmer1)
      fail("Expected FirebaseFirestoreException, but code ran fine")
    } catch (e: NullPointerException) {
      assertEquals(e.message, "No such user found")
    }
  }
}
