package com.android.agrihealth.data.model.device.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.agrihealth.R
import com.android.agrihealth.data.model.device.notifications.Notification.NewReport
import com.android.agrihealth.data.model.device.notifications.Notification.VetAnswer
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : NotificationHandler, FirebaseMessagingService() {
  private val messaging = FirebaseMessaging.getInstance()

  // TODO overwrite doc
  override fun onNewToken(token: String) {
    // Current implementation expected token handling to be in MainActivity, when the app loads
    // This is because a given user could have multiple devices
  }

  // TODO overwrite doc
  override fun onMessageReceived(message: RemoteMessage) {
    Log.d("FCM", "onMessageReceived called with id ${message.messageId}")

    // TODO
    // Convert RemoteMessage into a simple to understand Message class for the app, easy building

    val notification = message.data.toNotification() ?: throw IllegalArgumentException("Error while deserializing notification message")
    showNotification(notification)
    //message.notification?.let { showNotification(it.title, it.body) }
  }

  // TODO figure this out
  override fun onDeletedMessages() {
    super.onDeletedMessages()
  }

  /**
   * Gets the FCM token of the current device. This is what Firebase uses to know who to send a
   * notification to. Unsure about how useful this function is
   *
   * @param onComplete Action to take with the returned token
   */
  private fun getToken(onComplete: (token: String?) -> Unit) {
    messaging.token.addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val token = task.result
        onComplete(token)
      }
      else onComplete(null)
    }
  }

  override fun setupDevice(onComplete: (token: String) -> Unit) {
      getToken { token ->
        checkNotNull(token)
        onComplete(token)
      }
  }

  /** Uploads a notification to Firebase, to be received by the specified destination UID */
  override fun uploadNotification(notification: Notification) {
    val data = notification.toDataMap()
  }

  /** Shows a notification on the recipient's device. Used when a new notification is received */
  override fun showNotification(notification: Notification) { // TODO
    val title = notification.type.toName()
    val message = notification.destinationUid
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

// Serialization and Deserialization

private fun Notification.toDataMap(): Map<String, String> =
    when (this) {
      is NewReport ->
          mapOf(
              "type" to type.toName(),
              "authorUid" to authorUid,
              "destinationUid" to destinationUid,
              "reportTitle" to reportTitle)
      is VetAnswer ->
          mapOf(
              "type" to type.toName(),
              "authorUid" to authorUid,
              "destinationUid" to destinationUid,
              "answer" to answer)
    }

private fun Map<String, String>.toNotification(): Notification? {
  val type = NotificationType.fromName(this["type"] ?: return null)
  val authorUid = this["authorUid"] ?: return null
  val destinationUid = this["destinationUid"] ?: return null

  return when (type) {
    NotificationType.NEW_REPORT ->
        NewReport(
            authorUid = authorUid,
            destinationUid = destinationUid,
            reportTitle = this["reportTitle"] ?: return null)

    NotificationType.VET_ANSWER ->
        VetAnswer(
            authorUid = authorUid,
            destinationUid = destinationUid,
            answer = this["answer"] ?: return null)
    null -> null
  }
}
