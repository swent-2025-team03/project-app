package com.android.agrihealth.data.model.firebase.emulators

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.net.InetSocketAddress
import java.net.Socket

data class FirebaseEnvironment(val host: String, val firestorePort: Int, val authPort: Int)

/** Handles which Firebase emulator to run, and makes sure useEmulators() isn't called twice */
object FirebaseEmulatorsManager {
  private var emulatorInitialized = false
  private const val LOCAL_HOST = "10.0.2.2"
  private const val REMOTE_HOST = "firebase.okau.moe"
  private const val FIRESTORE_PORT = 8081
  private const val AUTH_PORT = 9099

  lateinit var environment: FirebaseEnvironment
    private set

  /**
   * Initializes Firebase emulators, prioritizing local emulators before falling back to the online
   * one on okau.moe
   */
  fun linkEmulators() {
    if (emulatorInitialized) return

    val shouldUseLocal = isLocalRunning(FIRESTORE_PORT) && isLocalRunning(AUTH_PORT)

    environment =
        FirebaseEnvironment(
            host = if (shouldUseLocal) LOCAL_HOST else REMOTE_HOST,
            firestorePort = FIRESTORE_PORT,
            authPort = AUTH_PORT)

    with(environment) {
      Firebase.firestore.useEmulator(host, firestorePort)

      Firebase.auth.useEmulator(host, authPort)
      Firebase.auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    }

    emulatorInitialized = true
  }

  fun isLocalRunning(port: Int, timeoutMs: Int = 1000): Boolean {
    return try {
      Socket().use { socket ->
        socket.connect(InetSocketAddress(LOCAL_HOST, port), timeoutMs)
        true
      }
    } catch (_: Exception) {
      false
    }
  }
}
