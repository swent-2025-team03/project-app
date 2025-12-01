package com.android.agrihealth.data.model.device.notifications

import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseTokenResolver : NotificationTokenResolver {
  private val messaging = FirebaseMessaging.getInstance()

  override val token: Task<String>
    get() = messaging.token
}
