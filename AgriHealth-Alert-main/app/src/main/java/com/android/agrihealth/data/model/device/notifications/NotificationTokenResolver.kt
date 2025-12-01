package com.android.agrihealth.data.model.device.notifications

import com.google.android.gms.tasks.Task

interface NotificationTokenResolver {
  val token: Task<String>
}
