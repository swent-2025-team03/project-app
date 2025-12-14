package com.android.agrihealth.testhelpers.templates

import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsManager
import org.junit.Before

abstract class FirebaseTest {
  init {
    FirebaseEmulatorsManager.linkEmulators()
  }

  @Before fun clean() = FirebaseEmulatorsManager.clearEmulators()
}
