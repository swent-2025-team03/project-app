package com.android.agrihealth.data.model.authentification

import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsManager
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.Vet
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Before

open class FirebaseEmulatorsTest() {
  val userRepository = UserRepositoryProvider.repository
  val authRepository = AuthRepositoryProvider.repository
  // from bootcamp
  val httpClient = OkHttpClient()

  init {
    FirebaseEmulatorsManager.linkEmulators()
  }

  private val host = FirebaseEmulatorsManager.environment.host
  private val firestorePort = FirebaseEmulatorsManager.environment.firestorePort
  private val authPort = FirebaseEmulatorsManager.environment.authPort

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

  @Before
  open fun setUp() {
    //FirebaseEmulatorsManager.linkEmulators()

    runTest {
      clearEmulator(authEndpoint)
      clearEmulator(firestoreEndpoint)
    }
  }
}
