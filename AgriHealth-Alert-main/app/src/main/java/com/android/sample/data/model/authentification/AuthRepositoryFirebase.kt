package com.android.sample.data.model.authentification

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class AuthRepositoryFirebase(private val auth: FirebaseAuth = Firebase.auth) : AuthRepository {

  override suspend fun signInWithEmailAndPassword(
      email: String,
      password: String
  ): Result<FirebaseUser> {
    return try {
      val user =
          auth.signInWithEmailAndPassword(email, password).await().user
              ?: return Result.failure(
                  IllegalStateException(
                      "sign in failed : Could not retrieve information from user"))

      Result.success(user)
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Login failed: ${e.localizedMessage ?: "Unexpected error."}"))
    }
  }

  override suspend fun signUpWithEmailAndPassword(
      email: String,
      password: String,
  ): Result<FirebaseUser> {
    return try {
      val user =
          auth.createUserWithEmailAndPassword(email, password).await().user
              ?: return Result.failure(
                  IllegalStateException("Sign up failed : Could not create account for user"))

      Result.success(user)
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Sign up failed: ${e.localizedMessage ?: "Unexpected error."}"))
    }
  }

  override fun signOut(): Result<Unit> {
    return try {
      auth.signOut()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Login failed: ${e.localizedMessage ?: "Unexpected error."}"))
    }
  }
}
