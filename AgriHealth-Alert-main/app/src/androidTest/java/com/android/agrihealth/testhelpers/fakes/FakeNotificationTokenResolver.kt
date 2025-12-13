package com.android.agrihealth.testhelpers.fakes

import com.android.agrihealth.data.model.device.notifications.DeviceNotificationToken
import com.android.agrihealth.data.model.device.notifications.NotificationTokenResolver
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class FakeNotificationTokenResolver(private val fakeToken: String?) : NotificationTokenResolver {
  override val deviceToken: Task<DeviceNotificationToken>
    get() =
        if (fakeToken != null)
            Tasks.forResult(
                DeviceNotificationToken(
                    token = fakeToken,
                    timestamp = 0L,
                    deviceName = "Fake Device",
                    platform = "Fake Platform"))
        else Tasks.forException(Exception("Token get failure"))
}
