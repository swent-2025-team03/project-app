package com.android.agrihealth.data.model.authentification

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

private val functions = FirebaseFunctions.getInstance()

suspend fun verifyUser(uid: String) {
  val data = mapOf("uid" to uid)
  val uploader = functions.getHttpsCallable("verifyEmail")
  uploader.call(data).await()
}
