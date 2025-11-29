package com.android.agrihealth.data.model.device.notifications

interface PushNotificationsProvider {
  fun registerDevice(onComplete: (String) -> Unit)

  suspend fun sendMessage()

  fun onMessageReceived()
}
