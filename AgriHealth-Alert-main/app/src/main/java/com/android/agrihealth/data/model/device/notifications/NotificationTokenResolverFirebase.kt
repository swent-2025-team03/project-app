package com.android.agrihealth.data.model.device.notifications

import android.os.Build
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging

/** Gets the device token for Firebase Messaging to send notifications to */
class NotificationTokenResolverFirebase : NotificationTokenResolver {
  private val messaging = FirebaseMessaging.getInstance()

  override val deviceToken: Task<DeviceNotificationToken>
    get() =
        messaging.token.continueWith { task ->
          val tokenString = task.result ?: ""
          DeviceNotificationToken(
              token = tokenString,
              timestamp = System.currentTimeMillis(),
              deviceName = Build.MODEL ?: "Unknown Device",
              platform = "Android ${Build.VERSION.SDK_INT}")
        }
}
