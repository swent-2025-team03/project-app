package com.android.agrihealth.model.authentification

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PermissionsFirestoreTest : FirebaseEmulatorsTest() {
  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun canAccessOwnData() {
    TODO()
  }

  @Test
  fun failToAccessOtherUsersData() {
    TODO()
  }

  @Test
  fun failToAccessDataWhileLoggedOut() {
    TODO()
  }

  @Test
  fun failToUpdateOwnRole() {
    TODO()
  }

  @Test fun failToDeleteWhileLoggedOut() = runTest { TODO() }
}
