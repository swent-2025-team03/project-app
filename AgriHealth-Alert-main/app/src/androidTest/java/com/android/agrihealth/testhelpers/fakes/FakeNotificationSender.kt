package com.android.agrihealth.testhelpers.fakes

import com.android.agrihealth.data.model.device.notifications.NotificationSender

class FakeNotificationSender(private val userRepository: FakeUserRepository) : NotificationSender {
  override fun sendNotification(data: Map<String, String>, onComplete: (Boolean) -> Unit) {
    fun fail() = onComplete(false)

    val recipient = data["destinationUid"] ?: return fail() // Malformed request

    val user =
        userRepository.getUserFromIdSync(recipient).getOrNull() ?: return fail() // User not found

    val tokens = user.deviceTokensFCM
    if (tokens.isEmpty()) return fail()

    onComplete(true)
  }
}
