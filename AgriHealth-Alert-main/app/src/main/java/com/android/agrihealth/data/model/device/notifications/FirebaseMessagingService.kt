package com.android.agrihealth.data.model.device.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.agrihealth.MainActivity
import com.android.agrihealth.R
import com.android.agrihealth.data.model.device.notifications.Notification.NewReport
import com.android.agrihealth.data.model.device.notifications.Notification.VetAnswer
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService() : NotificationHandler, FirebaseMessagingService() {
  private val messaging = FirebaseMessaging.getInstance()
  private val functions = FirebaseFunctions.getInstance()

  private val channelNewReport = "new_report_channel"
  private val channelVetAnswer = "vet_answer_channel"

  override fun onCreate() {
    super.onCreate()
    createNotificationChannels()
  }

  private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val nm = getSystemService(NotificationManager::class.java) ?: return

      nm.createNotificationChannel(NotificationChannel(
        channelNewReport,
        "New reports",
        NotificationManager.IMPORTANCE_HIGH
      ))

      nm.createNotificationChannel(NotificationChannel(
        channelVetAnswer,
        "Vet answers",
        NotificationManager.IMPORTANCE_HIGH
      ))
    }
  }

  /** WILL CRASH IF RAN FROM A COMPOSABLE
   *
   * Shows a notification on the user's current device. Used when a new notification is received */
  override fun showNotification(notification: Notification) {
    val nm = getSystemService(NotificationManager::class.java) ?: return

    val (title, body, channelId) = when (notification) {
      is NewReport -> Triple(
        "New report from ${notification.authorUid}",
        notification.reportTitle,
        channelNewReport
      )
      is VetAnswer -> Triple(
        "A vet has answered your report",
        "${notification.authorUid}: ${notification.answer}",
        channelVetAnswer
      )
    }

    val systemNotification = NotificationCompat.Builder(this, channelId)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle(title)
      .setContentText(body)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    val id = System.currentTimeMillis().toInt()
    nm.notify(id, systemNotification)
  }

  /** Empty function. Runs when a new FCM token is detected, but that is handled in MainActivity */
  override fun onNewToken(token: String) {
    // Current implementation expects token handling to be in MainActivity, when the app loads
    // This is because a given user could have multiple devices
  }

  /** WILL CRASH IF RAN FROM A COMPOSABLE
   *
   * Shows a new notification to the recipient's devices.
   * Runs when a new notification is received in Firebase Messaging
   */
  override fun onMessageReceived(message: RemoteMessage) {
    //message.notification?.let { actualShowNotification() }
    Log.d("FCM", "onMessageReceived called with id ${message.messageId}")

    val notification =
        message.data.toNotification()
            ?: throw IllegalArgumentException("Error while deserializing notification message")

    showNotification(notification)
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
      } else onComplete(null)
    }
  }

  override fun setupDevice(onComplete: (token: String) -> Unit) {
    getToken { token ->
      checkNotNull(token)
      onComplete(token)
    }
  }

  /** Uploads a notification to Firebase, to be received by the specified destination UID */
  override fun uploadNotification(
      notification: Notification,
      onComplete: (success: Boolean) -> Unit
  ) {
    val data = notification.toDataMap()

    val uploader = functions.getHttpsCallable("sendNotification")

    Log.d("FCM", "data to backend: $data")

    uploader
        .call(data)
        .addOnSuccessListener { result ->
          val data = result.data as Map<String, Any>
          val success = data["success"] as Boolean
          val message = data["message"] as String

          Log.d("FCM", "Response: Success: $success, message: $message")
          onComplete(success)
        }
        .addOnFailureListener { exception ->
          Log.e("FCM", "Failed to send notification", exception)
          onComplete(false)
        }
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
