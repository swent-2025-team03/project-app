package com.android.agrihealth.testhelpers

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

private val functions = FirebaseFunctions.getInstance()

suspend fun verifyCurrentUser() {
  val uploader = functions.getHttpsCallable("verifyEmail")
  uploader.call().await()
}
