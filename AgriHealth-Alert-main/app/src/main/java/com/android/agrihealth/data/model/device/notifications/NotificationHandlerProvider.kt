package com.android.agrihealth.data.model.device.notifications

/** Singleton providing the notification handler used throughout the app */
object NotificationHandlerProvider {
  val handler: NotificationHandler = NotificationHandlerFirebase()
}
