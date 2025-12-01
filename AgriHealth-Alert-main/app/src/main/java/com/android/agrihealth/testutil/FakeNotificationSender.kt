package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.device.notifications.NotificationSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FakeNotificationSender(private val userRepository: FakeUserRepository) : NotificationSender {
  override fun sendNotification(data: Map<String, String>, onComplete: (Boolean) -> Unit) {
    val recipient = data["destinationUid"]

    if (recipient == null) onComplete(false) // Malformed request
    else {
      CoroutineScope(Dispatchers.IO).launch {
        val user = userRepository.getUserFromId(recipient).getOrNull()

        if (user == null) onComplete(false) // User not found
        else {
          val tokens = user.deviceTokensFCM

          if (tokens.isEmpty()) onComplete(false) // No tokens
          else onComplete(true)
        }
      }
    }
  }
}
