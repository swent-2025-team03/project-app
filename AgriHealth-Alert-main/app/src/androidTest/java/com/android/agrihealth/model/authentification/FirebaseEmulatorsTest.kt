package com.android.agrihealth.model.authentification

import com.android.agrihealth.R
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.authentification.USERS_COLLECTION_PATH
import com.android.agrihealth.data.model.authentification.User
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.authentification.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before

open class FirebaseEmulatorsTest {
  val userRepository = UserRepositoryProvider.repository
  val authRepository = AuthRepositoryProvider.repository

  val user1 = User("abc123", "Rushia", "Uruha", UserRole.FARMER, "email1@thing.com")
  val user2 = User("def456", "mike", "neko", UserRole.FARMER, "email2@aaaaa.balls")
  val user3 = User("ghj789", "Nazuna", "Amemiya", UserRole.VETERINARIAN, "email3@kms.josh")

  private suspend fun clearUsers() {
    val usersCollection = Firebase.firestore.collection(USERS_COLLECTION_PATH).get().await()

    // Inspired from bootcamp
    if (usersCollection.count() > 0) {
      val batch = Firebase.firestore.batch()
      usersCollection.documents.forEach { batch.delete(it.reference) }
      batch.commit().await()
    }
  }

  companion object {
    var emulatorInitialized = false
  }

  @Before
  open fun setUp() {
    if (!emulatorInitialized) {
      val context =
          androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
      val url = context.getString(R.string.FIREBASE_EMULATORS_URL)
      val firestorePort = context.resources.getInteger(R.integer.FIREBASE_EMULATORS_FIRESTORE_PORT)
      val authPort = context.resources.getInteger(R.integer.FIREBASE_EMULATORS_AUTH_PORT)

      Firebase.firestore.useEmulator(url, firestorePort)
      Firebase.auth.useEmulator(url, authPort)

      emulatorInitialized = true
    }

    runTest { clearUsers() }
  }
}
