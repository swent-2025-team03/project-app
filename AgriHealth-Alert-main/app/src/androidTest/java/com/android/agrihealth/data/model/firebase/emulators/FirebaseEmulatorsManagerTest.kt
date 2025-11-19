package com.android.agrihealth.data.model.firebase.emulators

import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsManager.isLocalRunning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FirebaseEmulatorsManagerTest {
  @Test
  fun localEmulatorsAreRunning() {
    FirebaseEmulatorsManager.linkEmulators()

    val environment = FirebaseEmulatorsManager.environment
    val expectedHost = "10.0.2.2"

    assert(environment.host == expectedHost)
  }
}
