package com.android.agrihealth.data.model.device.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotificationsTest : FirebaseEmulatorsTest() {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun uploadNotification_succeeds() = runTest {
    authRepository.signUpWithEmailAndPassword(user1.email, password1, user1)

    composeTestRule.setContent {
      val messagingService = FirebaseMessagingService()

      val testNotification =
          Notification.NewReport(
              authorUid = user1.uid, destinationUid = user1.uid, reportTitle = "maldie animal")

      messagingService.uploadNotification(testNotification) { assertTrue(it) }
    }
  }
}

@Composable
@Preview
fun NotificationTestScreen() {
  AgriHealthAppTheme {
    NotificationsPermissionsRequester()

    var token by remember { mutableStateOf("") }
    var uploadResult by remember { mutableStateOf("") }
    val messagingService = FirebaseMessagingService()

    val authorUid = "sgHb1hb8fDa7mU6EtdF63eJC9j32"
    val destinationUid = "sgHb1hb8fDa7mU6EtdF63eJC9j32"
    val reportTitle = "maldie animal"

    val testNotification = Notification.NewReport(authorUid, destinationUid, reportTitle)

    Box {
      Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).fillMaxSize()) {
        TextButton(
            onClick = { messagingService.getToken { token = it!! } },
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
              Text("See messaging token", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

        TextButton(
            onClick = {
              messagingService.uploadNotification(
                  testNotification,
                  onComplete = { success ->
                    uploadResult =
                        if (success) "Notification sent" else "Failed to send notification"
                  })
              uploadResult = "Uploading..."
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
              Text("Upload test notification", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

        Text(token, color = MaterialTheme.colorScheme.onSurface)
        Text(uploadResult, color = MaterialTheme.colorScheme.onSurface)
      }
    }
  }
}
