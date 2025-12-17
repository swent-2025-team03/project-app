package com.android.agrihealth.data.model.device.notifications

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions

/** Sends notifications to a Firebase Functions backend */
class NotificationSenderFirebase : NotificationSender {
  private val functions = FirebaseFunctions.getInstance()

  @Suppress("UNCHECKED_CAST")
  override fun sendNotification(data: Map<String, String>, onComplete: (success: Boolean) -> Unit) {
    val uploader = functions.getHttpsCallable("sendNotification")

    uploader
        .call(data)
        .addOnSuccessListener { result ->
          val data = result.data as Map<String, Any>
          val success = data["success"] as Boolean
          // val message = data["message"] as String // For debugging

          onComplete(success)
        }
        .addOnFailureListener { exception ->
          Log.e("FirebaseNotificationSender", "Failed to send notification", exception)
          onComplete(false)
        }
  }
}
