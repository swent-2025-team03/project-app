package com.android.agrihealth.testhelpers

import androidx.test.platform.app.InstrumentationRegistry

object NetworkTestUtils {
  fun setNetworkEnabled(enabled: Boolean) {
    val state = if (enabled) "enable" else "disable"
    val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
    uiAutomation.executeShellCommand("svc wifi $state").close()
    uiAutomation.executeShellCommand("svc data $state").close()
  }
}
