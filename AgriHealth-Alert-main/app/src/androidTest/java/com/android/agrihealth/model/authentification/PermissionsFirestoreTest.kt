package com.android.agrihealth.model.authentification

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert.*
import com.google.firebase.emulators.EmulatedServiceSettings
import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.After

class PermissionsFirestoreTest : FirebaseEmulatorsTest() {
  val auth = FirebaseAuth.getInstance()


  @Before
  override fun setUp() {
    super.setUp()
    authRepository.signOut()
  }

  @Test
  fun canAccessOwnData() = runTest {
    authRepository.signUpWithEmailAndPassword(user1.email, password1, user1)
    //authRepository.signInWithEmailAndPassword(user1.email, password1)

    assertNotNull(auth.currentUser)
    val uid = auth.currentUser?.uid
    assertNotNull(uid)

    val userData = userRepository.getUserFromId(uid!!).getOrThrow()
    assertEquals(uid, userData.uid)
    assertEquals(user1.name, userData.name)
    assertEquals(user1.surname, userData.surname)
    assertEquals(user1.role, userData.role)
    assertEquals(user1.email, userData.email)
  }

  @Test
  fun failToAccessOtherUsersData() = runTest {
    authRepository.signUpWithEmailAndPassword(user2.email, password2, user2)
    assertNotNull(auth.currentUser)
    val uid2 = auth.currentUser!!.uid

    authRepository.signOut()
    authRepository.signUpWithEmailAndPassword(user1.email, password1, user1)
    assertNotNull(auth.currentUser)

    val result = userRepository.getUserFromId(uid2)

    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertNotNull(exception)
    assertTrue(exception is FirebaseFirestoreException)
    assertEquals((exception as FirebaseFirestoreException).code, FirebaseFirestoreException.Code.PERMISSION_DENIED)
  }

  @Test
  fun failToAccessDataWhileLoggedOut() {
    //TODO()
  }

  @Test
  fun failToUpdateOwnRole() {
    TODO()
  }

  @Test fun failToDeleteWhileLoggedOut() = runTest { TODO() }
}
