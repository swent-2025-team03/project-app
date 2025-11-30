package com.android.agrihealth.data.model.device.notifications

import android.content.Context

interface NotificationHandler {
  fun setupDevice(onComplete: (token: String) -> Unit)

  fun uploadNotification(notification: Notification, onComplete: (success: Boolean) -> Unit = {})

  fun showNotification(notification: Notification)
}
