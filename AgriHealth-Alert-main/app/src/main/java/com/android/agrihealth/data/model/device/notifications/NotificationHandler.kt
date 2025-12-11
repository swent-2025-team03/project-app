package com.android.agrihealth.data.model.device.notifications

interface NotificationHandler {
  /** Gets a unique identifier for the current device to receive notifications */
  fun getToken(onComplete: (deviceToken: String?) -> Unit)

  /** Sends a notification to the backend, to be then transmitted to the receiver */
  fun uploadNotification(notification: Notification, onComplete: (success: Boolean) -> Unit = {})

  /** Shows a notification to the current user's device */
  fun showNotification(notification: Notification)
}
