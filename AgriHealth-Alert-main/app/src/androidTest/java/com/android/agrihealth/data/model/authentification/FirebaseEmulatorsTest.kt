package com.android.agrihealth.data.model.authentification

import androidx.test.platform.app.InstrumentationRegistry
import com.android.agrihealth.R
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.Vet
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Before

open class FirebaseEmulatorsTest(shouldInitializeEmulators: Boolean = true) {
  val userRepository = UserRepositoryProvider.repository
  val authRepository = AuthRepositoryProvider.repository
  private val _shouldInitializeEmulators = shouldInitializeEmulators
  // from bootcamp
  val httpClient = OkHttpClient()
  val contextHost =
      androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
  val host = contextHost.getString(R.string.FIREBASE_EMULATORS_URL)
  val firestorePort = 8081
  val authPort = 9099

  private val firestoreEndpoint by lazy {
    "http://${host}:${firestorePort}/emulator/v1/projects/agrihealth-alert/databases/(default)/documents"
  }
  private val authEndpoint by lazy {
    "http://${host}:${authPort}/emulator/v1/projects/agrihealth-alert/accounts"
  }

  // Definition of test users
  val user1 = Farmer("abc123", "Rushia", "Uruha", "email1@thing.com", null, emptyList(), null)
  val user2 = Farmer("def456", "mike", "neko", "email2@aaaaa.balls", null, emptyList(), null)
  val user3 = Vet("ghi789", "Nazuna", "Amemiya", "email3@kms.josh", null)
  val user4 = Farmer("jklABC", "John", "Fake", "fakeUser.glorp", null, emptyList(), null)

  val password1 = "Password123"
  val password2 = "iamaweakpassword"
  val password3 = "12345678"
  val password4 = "weak"

  // from Bootcamp
  private fun clearEmulator(endpoint: String) {
    val client = httpClient
    val request = Request.Builder().url(endpoint).delete().build()
    val response = client.newCall(request).execute()

    assert(response.isSuccessful) { "Failed to clear emulator at $endpoint" }
  }

  companion object {
    var emulatorInitialized = false
  }

  @Before
  open fun setUp() {
    if (!emulatorInitialized && _shouldInitializeEmulators) {
      val context =
          androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
      val url = context.getString(R.string.FIREBASE_EMULATORS_URL)
      val firestorePort = context.resources.getInteger(R.integer.FIREBASE_EMULATORS_FIRESTORE_PORT)
      val authPort = context.resources.getInteger(R.integer.FIREBASE_EMULATORS_AUTH_PORT)

      // running all tests fails if e2e runs at the same time as this
      try {
        Firebase.firestore.useEmulator(url, firestorePort)
        Firebase.auth.useEmulator(url, authPort)
      } catch (e: IllegalStateException) {
        if (e.message != "Cannot call useEmulator() after instance has already been initialized.")
            throw e
      } finally {
        emulatorInitialized = true
      }
    }

    runTest {
      clearEmulator(authEndpoint)
      clearEmulator(firestoreEndpoint)
    }
  }
}
