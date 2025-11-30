package com.android.agrihealth.data.model.device.notifications

interface NotificationHandler {
  fun setupDevice(onComplete: (token: String) -> Unit)
  fun uploadNotification(notification: Notification)
  fun showNotification(notification: Notification)
}