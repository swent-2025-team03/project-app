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

class FirebaseMessagingService : FirebaseMessagingService() {
  private val messaging = FirebaseMessaging.getInstance()

  /**
   * Gets the FCM token of the current device. This is what Firebase uses to know who to send a
   * notification to. Unsure about how useful this function is
   *
   * @param onComplete Action to take with the returned token
   */
  fun getToken(onComplete: (token: String) -> Unit) {
    messaging.token.addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val token = task.result
        Log.d("FCM", "token from getToken: $token")
        onComplete(token)
      }
    }
  }

  override fun onNewToken(token: String) {
    // TODO overwrite doc
    Log.d("FCM", "New token: $token")

    // TODO
    // Upload token to Firestore, users -> uid -> deviceFCMTokens = []
  }

  override fun onMessageReceived(message: RemoteMessage) {
    // TODO overwrite doc
    Log.d("FCM", "onMessageReceived called with id ${message.messageId}")

    // TODO
    // Convert RemoteMessage into a simple to understand Message class for the app, easy building

    message.notification?.let { sendNotification(it.title, it.body) }
  }

  // TODO figure this out
  override fun onDeletedMessages() {
    super.onDeletedMessages()
  }

  // Probably change to take a Message?
  private fun sendNotification(title: String?, message: String?) {
    Log.d("FCM", "Sending notification $title")
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
