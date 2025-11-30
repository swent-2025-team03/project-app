package com.android.agrihealth.data.model.device.notifications

import androidx.compose.ui.test.junit4.createComposeRule
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotificationsTest : FirebaseEmulatorsTest() {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun test() = runTest {
    authRepository.signUpWithEmailAndPassword(user1.email, password1, user1)
    val messagingService = FirebaseMessagingService()

    val testNotification =
        Notification.NewReport(
            authorUid = user1.uid, destinationUid = user1.uid, reportTitle = "maldie animal")

    messagingService.uploadNotification(testNotification) { assertTrue(it) }
  }
}
