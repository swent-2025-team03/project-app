package com.android.agrihealth.data.model.device.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.agrihealth.R
import com.android.agrihealth.data.model.device.notifications.Notification.NewReport
import com.android.agrihealth.data.model.device.notifications.Notification.VetAnswer
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// Control panel imports
/*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.authentification.USERS_COLLECTION_PATH
*/

@SuppressLint("MissingFirebaseInstanceTokenRefresh") // Tokens are handled in MainActivity
class FirebaseMessagingService(
    private val tokenResolver: NotificationTokenResolver = FirebaseTokenResolver(),
    private val sender: NotificationSender = FirebaseNotificationSender()
) : NotificationHandler, FirebaseMessagingService() {

  private val channelNewReport = "new_report_channel"
  private val channelVetAnswer = "vet_answer_channel"

  override fun onCreate() {
    super.onCreate()
    createNotificationChannels()
  }

  /**
   * Gets the FCM token of the current device. This is what Firebase uses to know who to send a
   * notification to. Use it to associate users with their tokens
   *
   * @param onComplete Action to take with the returned token. May be null
   */
  override fun getToken(onComplete: (token: String?) -> Unit) {
    tokenResolver.token.addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val token = task.result
        onComplete(token)
      } else onComplete(null)
    }
  }

  /** Uploads a notification to Firebase, to be received by the specified destination UID */
  override fun uploadNotification(
      notification: Notification,
      onComplete: (success: Boolean) -> Unit
  ) {
    val data = notification.toDataMap()

    sender.sendNotification(data) { success -> onComplete(success) }
  }

  /**
   * WILL CRASH IF RAN FROM A COMPOSABLE
   *
   * Shows a notification on the user's current device. Used when a new notification is received
   */
  override fun showNotification(notification: Notification) {
    val nm = getSystemService(NotificationManager::class.java) ?: return

    val (title, body, channelId) =
        when (notification) {
          is NewReport -> Triple("New report", notification.reportTitle, channelNewReport)
          is VetAnswer ->
              Triple("A vet has answered your report", notification.answer, channelVetAnswer)
        }

    val systemNotification =
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

    val id = System.currentTimeMillis().toInt()
    nm.notify(id, systemNotification)
  }

  /**
   * WILL CRASH IF RAN FROM A COMPOSABLE
   *
   * Shows a new notification to the recipient's devices. Runs when a new notification is received
   * in Firebase Messaging
   */
  override fun onMessageReceived(message: RemoteMessage) {
    message.data.toNotification()?.let { notification -> showNotification(notification) }
        ?: Log.w("FirebaseMessagingService", "Error while deserializing notification message")
  }

  @SuppressLint("ObsoleteSdkInt")
  private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val nm = getSystemService(NotificationManager::class.java) ?: return

      nm.createNotificationChannel(
          NotificationChannel(channelNewReport, "New reports", NotificationManager.IMPORTANCE_HIGH))

      nm.createNotificationChannel(
          NotificationChannel(channelVetAnswer, "Vet answers", NotificationManager.IMPORTANCE_HIGH))
    }
  }
}

// Serialization and Deserialization

fun Notification.toDataMap(): Map<String, String> =
    when (this) {
      is NewReport ->
          mapOf(
              "type" to type.toName(),
              "destinationUid" to destinationUid,
              "reportTitle" to reportTitle)
      is VetAnswer ->
          mapOf("type" to type.toName(), "destinationUid" to destinationUid, "answer" to answer)
    }

fun Map<String, String>.toNotification(): Notification? {
  val type = NotificationType.fromName(this["type"] ?: return null)
  val destinationUid = this["destinationUid"] ?: return null

  return when (type) {
    NotificationType.NEW_REPORT ->
        NewReport(destinationUid = destinationUid, reportTitle = this["reportTitle"] ?: return null)
    NotificationType.VET_ANSWER ->
        VetAnswer(destinationUid = destinationUid, answer = this["answer"] ?: return null)
    null -> null
  }
}

/*
@Composable
@Preview
fun NotificationTestControlPanel() {
  AgriHealthAppTheme {
    var debugText by remember { mutableStateOf("") }

    val uid = Firebase.auth.currentUser?.uid
    if (uid == null) {
      debugText = "You are not logged in, open MainActivity and try again"
    }

    val notificationHandler = NotificationHandlerProvider.handler
    NotificationsPermissionsRequester(
      onDenied = { debugText = "You need to grant notifications access for this screen to work" },
      onGranted = {
        notificationHandler.getToken { token ->
          if(uid == null) return@getToken

          if (token == null) {
            debugText = "Could not get token for some reason, try again maybe"
            return@getToken
          }

          val map = mapOf("deviceTokensFCM" to listOf(token))
          CoroutineScope(Dispatchers.IO).launch {
            Firebase.firestore.collection(USERS_COLLECTION_PATH).document(uid).update(map).await()
          }
        }
      })

    val messagingService = com.android.agrihealth.data.model.device.notifications.FirebaseMessagingService()

    val destinationUid = uid ?: ""
    val reportTitle = "maldie animal"
    val answer = "unlucky bro unlucky"

    val notificationNR = NewReport(destinationUid, reportTitle)
    val notificationVA = VetAnswer(destinationUid, answer)

    var actualNotification by remember { mutableStateOf<Notification>(notificationNR) }

    Box {
      Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).fillMaxSize()) {
        TextButton(
          onClick = { messagingService.getToken { debugText = it!! } },
          modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
          Text("See messaging token", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        // Notification type switch

          Row {
            RadioButton(
              selected = actualNotification == notificationNR,
              onClick = { actualNotification = notificationNR }
            )
            Text("New report", color = MaterialTheme.colorScheme.onPrimaryContainer)
          }

          Row {
            RadioButton(
              selected = actualNotification == notificationVA,
              onClick = { actualNotification = notificationVA }
            )
            Text("Vet answer", color = MaterialTheme.colorScheme.onPrimaryContainer)
          }

        TextButton(
          onClick = {
            messagingService.uploadNotification(
              actualNotification,
              onComplete = { success ->
                debugText =
                  if (success) "Notification sent" else "Failed to send notification"
              })
            debugText = "Uploading..."
          },
          modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
          Text("Upload test notification", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        Text(debugText, color = MaterialTheme.colorScheme.onSurface)
      }
    }
  }
}
*/
