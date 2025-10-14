package com.android.agrihealth.model.authentification

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PermissionsFirestoreTest : FirebaseEmulatorsTest() {
  @Before
  override fun setUp() {
    super.setUp()
  }

  // TODO: write tests

  @Test fun canAccessOwnData() {}

  @Test fun failToAccessOtherUsersData() {}

  @Test fun failToAccessDataWhileLoggedOut() {}

  @Test fun failToUpdateOwnRole() {}

  @Test fun failToDeleteWhileLoggedOut() = runTest {}
}
