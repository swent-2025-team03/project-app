package com.android.agrihealth.data.model.device.notifications

/** Stores device information for notification handling */
data class DeviceNotificationToken(
    val token: String,
    val timestamp: Long,
    val deviceName: String,
    val platform: String
)
