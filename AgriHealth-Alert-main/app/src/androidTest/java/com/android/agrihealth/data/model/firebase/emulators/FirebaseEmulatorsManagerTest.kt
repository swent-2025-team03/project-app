package com.android.agrihealth.data.model.firebase.emulators

import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsManager.shouldUseLocal
import com.android.agrihealth.testhelpers.NetworkTestUtils.setNetworkEnabled
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirebaseEmulatorsManagerTest {
  @Test
  fun localHost_ifLocalRunning() {
    FirebaseEmulatorsManager.linkEmulators()

    val environment = FirebaseEmulatorsManager.environment
    val usedHost = environment.host
    val localHost = "10.0.2.2"

    if (shouldUseLocal()) assertEquals(localHost, usedHost)
    else assertNotEquals(localHost, usedHost)
  }

  @Test
  fun throwsIfNotFound() = runTest {
    setNetworkEnabled(false)

    try {
      FirebaseEmulatorsManager.linkEmulators(force = true)
      fail("Somehow found an emulator despite no internet connection")
    } catch (_: IllegalStateException) {
      assertTrue(true)
    }
  }

  @After
  fun enableNetwork() {
    setNetworkEnabled(true)
  }
}
