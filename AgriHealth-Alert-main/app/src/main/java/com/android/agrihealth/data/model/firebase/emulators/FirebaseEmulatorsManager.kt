package com.android.agrihealth.data.model.firebase.emulators

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.net.InetSocketAddress
import java.net.Socket

data class FirebaseEnvironment(
    val host: String,
    val firestorePort: Int,
    val authPort: Int,
    val storagePort: Int
)

/** Handles which Firebase emulator to run, and makes sure useEmulators() isn't called twice */
object FirebaseEmulatorsManager {
  private var emulatorInitialized = false
  private const val LOCAL_HOST = "10.0.2.2"
  private const val FIRESTORE_PORT = 8081
  private const val AUTH_PORT = 9099
  private const val STORAGE_PORT = 9199

  lateinit var environment: FirebaseEnvironment
    private set

  /**
   * Initializes Firebase emulators, prioritizing local emulators before falling back to the online
   * one on okau.moe
   */
  fun linkEmulators() {
    if (emulatorInitialized) return

    val shouldUseLocal =
        isLocalRunning(FIRESTORE_PORT) && isLocalRunning(AUTH_PORT) && isLocalRunning(STORAGE_PORT)

    check(shouldUseLocal) { "Firebase emulators not running locally, run: firebase emulators:start" }

    environment =
        FirebaseEnvironment(
            host = LOCAL_HOST,
            firestorePort = FIRESTORE_PORT,
            authPort = AUTH_PORT,
            storagePort = STORAGE_PORT)

    with(environment) {
      Firebase.firestore.useEmulator(host, firestorePort)
      Firebase.auth.useEmulator(host, authPort)
      Firebase.storage.useEmulator(host, storagePort)
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
