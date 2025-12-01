package com.android.agrihealth.data.model.device.notifications

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.functions.FirebaseFunctions

class FirebaseNotificationSender : NotificationSender {
  private val functions = FirebaseFunctions.getInstance()

  @Suppress("UNCHECKED_CAST")
  override fun sendNotification(data: Map<String, String>, onComplete: (success: Boolean) -> Unit) {
    val uploader = functions.getHttpsCallable("sendNotification")

    uploader
      .call(data)
      .addOnSuccessListener { result ->
        val data = result.data as Map<String, Any>
        val success = data["success"] as Boolean
        val message = data["message"] as String

        Log.d("FirebaseNotificationSender", "Response: Success: $success, message: $message")
        onComplete(success)
      }
      .addOnFailureListener { exception ->
        Log.e("FirebaseNotificationSender", "Failed to send notification", exception)
        onComplete(false)
      }
  }
}