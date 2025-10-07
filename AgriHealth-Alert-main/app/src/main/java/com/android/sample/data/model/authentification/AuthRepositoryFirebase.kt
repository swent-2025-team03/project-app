package com.android.sample.data.model.authentification

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class AuthRepositoryFirebase(private val auth: FirebaseAuth = Firebase.auth) : AuthRepository {

  override suspend fun signInWithEmailAndPassword(
      email: String,
      password: String
  ): Result<FirebaseUser> {
    TODO("Not yet implemented")
  }

  override suspend fun signUpWithEmailAndPassword(
      email: String,
      password: String,
      userData: User
  ): Result<FirebaseUser> {
    TODO("Not yet implemented")
  }

  override fun signOut(): Result<Unit> {
    TODO("Not yet implemented")
  }
}
