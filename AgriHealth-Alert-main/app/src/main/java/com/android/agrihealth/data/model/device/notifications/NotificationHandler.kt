package com.android.agrihealth.data.model.device.notifications

interface NotificationHandler {
  fun setupDevice(onComplete: (token: String) -> Unit)

  fun uploadNotification(notification: Notification, onComplete: (success: Boolean) -> Unit = {})

  fun showNotification(notification: Notification)
}
