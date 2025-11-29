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
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme

val TAG = "NotificationTester"

@Composable
@Preview
fun NotificationTest() {
  AgriHealthAppTheme {
    NotificationsPermissionsRequester(onGranted = { Log.d(TAG, "Granted permissions") })

    val messagingService = FirebaseMessagingService()
    var token by remember { mutableStateOf("") }

    Box {
      Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).fillMaxSize()) {
        TextButton(
            onClick = { messagingService.registerDevice { token = it } },
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
              Text(
                  "See messaging token",
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

        Text(token, color = MaterialTheme.colorScheme.onSurface)
      }
    }
  }
}
