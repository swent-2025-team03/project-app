package com.android.agrihealth.core

import android.app.Application
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class App : Application() {
  override fun onCreate() {
    super.onCreate()
    // Initialize Firebase once for the whole app
    FirebaseApp.initializeApp(this)

    // Link Firebase emulators BEFORE any Firestore instance is created
    // This ensures Firestore/Auth point to the emulator and avoids calling useEmulator() after init
    FirebaseEmulatorsManager.linkEmulators()

    // Enable local Firestore persistence (offline cache)
    val db = FirebaseFirestore.getInstance()
    val settings =
        FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // enable local cache
            .setCacheSizeBytes(100L * 1024 * 1024) // set cache size limit to 100 MB
            .build()
    db.firestoreSettings = settings
  }
}
