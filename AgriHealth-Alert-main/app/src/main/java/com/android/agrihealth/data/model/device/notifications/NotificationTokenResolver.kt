package com.android.agrihealth.data.model.device.notifications

import com.google.android.gms.tasks.Task

interface NotificationTokenResolver {
  /** Gets a token for the current device to identify it on the notification messaging service */
  val token: Task<String>
}
