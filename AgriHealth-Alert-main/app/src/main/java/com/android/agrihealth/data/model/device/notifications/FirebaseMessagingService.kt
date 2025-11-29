package com.android.agrihealth.data.model.device.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.agrihealth.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : PushNotificationsProvider, FirebaseMessagingService() {
  private val messaging = FirebaseMessaging.getInstance()

  override fun registerDevice(onComplete: (String) -> Unit) {
    messaging.token.addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val token = task.result
        Log.d("FCM", "token from registerDevice: $token")
        onComplete(token)
      }
    }
  }

  override suspend fun sendMessage() {
    TODO("Not yet implemented")
  }

  override fun onMessageReceived() {
    TODO("Not yet implemented")
  }

  override fun onNewToken(token: String) {
    Log.d("FCM", "New token: $token")

    super.onNewToken(token)
  }

  override fun onMessageReceived(message: RemoteMessage) {
    message.notification?.let { sendNotification(it.title, it.body) }
    // super.onMessageReceived(message)
  }

  override fun onDeletedMessages() {
    super.onDeletedMessages()
  }

  private fun sendNotification(title: String?, message: String?) {
    val channelId = "your_app_channel"

    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
          NotificationChannel(channelId, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT)
      notificationManager.createNotificationChannel(channel)
    }

    val notification =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

    notificationManager.notify(0, notification)
  }
}
