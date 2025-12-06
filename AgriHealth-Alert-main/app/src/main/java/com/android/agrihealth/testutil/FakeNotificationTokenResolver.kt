package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.device.notifications.NotificationTokenResolver
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class FakeNotificationTokenResolver(private val fakeToken: String?) : NotificationTokenResolver {
  override val token: Task<String>
    get() =
        if (fakeToken != null) Tasks.forResult(fakeToken)
        else Tasks.forException(Exception("Token get failure"))
}
