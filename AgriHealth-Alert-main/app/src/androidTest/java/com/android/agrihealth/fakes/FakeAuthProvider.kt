package com.android.agrihealth.fakes

import com.android.agrihealth.data.model.auth.AuthProvider

class FakeAuthProvider(private val uid: String = "fake-user") : AuthProvider {

  override fun currentUserId(): String? = uid
}
