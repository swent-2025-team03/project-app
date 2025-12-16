package com.android.agrihealth.data.model.device.notifications

/** Handles sending notification to a specific backend */
@FunctionalInterface
interface NotificationSender {
  /** Sends the given notification to the backend server. Returns success status in the lambda */
  fun sendNotification(data: Map<String, String>, onComplete: (success: Boolean) -> Unit)
}
