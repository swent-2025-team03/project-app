package com.android.agrihealth.testhelpers.templates

import com.android.agrihealth.testhelpers.FirebaseEmulatorsManager
import org.junit.After

abstract class FirebaseTest {
  init {
    FirebaseEmulatorsManager.linkEmulators()
  }

  @After fun clean() = FirebaseEmulatorsManager.clearEmulators()
}
