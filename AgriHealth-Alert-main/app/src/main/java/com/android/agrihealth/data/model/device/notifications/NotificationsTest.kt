package com.android.agrihealth.data.model.device.notifications

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme

val TAG = "NotificationTester"

@Composable
@Preview
fun NotificationTest() {
  AgriHealthAppTheme {
    NotificationsPermissionsRequester(onGranted = { Log.d(TAG, "Granted permissions") })
    // Firebase.functions.useEmulator("192.168.1.62", 5001)

    //val messagingService = FirebaseMessagingService()
    var token by remember { mutableStateOf("") }
    var uploadResult by remember { mutableStateOf("") }

    val context = LocalContext.current
    //val notificationHandler = remember { NotificationHandlerTest(context) }
    val messagingService = remember { FirebaseMessagingService() }

    val testNotification =
        Notification.NewReport(
            authorUid = "sgHb1hb8fDa7mU6EtdF63eJC9j32",
            destinationUid = "sgHb1hb8fDa7mU6EtdF63eJC9j32",
            reportTitle = "maldie animal")

    Box {
      Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).fillMaxSize()) {
        TextButton(
            onClick = { messagingService.setupDevice { token = it } },
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
              Text("See messaging token", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

        TextButton(
            onClick = {
              messagingService.uploadNotification(testNotification, onComplete = { success ->
                if (success) uploadResult = "Notification sent"
                else uploadResult = "Failed to send notification"
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
