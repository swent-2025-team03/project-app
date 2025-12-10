package com.android.agrihealth.testutil

import androidx.credentials.Credential
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.user.User
import com.google.firebase.auth.FirebaseAuthException

class FakeAuthRepository(private var isOnline: Boolean = true) : AuthRepository {

  private val credentials = mutableMapOf<String, String>()

  private var currentUser: String? = null

  fun switchConnection(state: Boolean) {
    isOnline = state
  }

  override suspend fun changePassword(password: String): Result<Unit> {
    if (isOnline) {
      if (currentUser == null) {
        return Result.failure(IllegalStateException("no user logged in"))
      } else credentials[currentUser!!] = password
      return Result.success(Unit)
    }
    return Result.failure(IllegalStateException("timeout"))
  }

  override suspend fun deleteAccount(): Result<Unit> {
    if (isOnline) {
      if (currentUser == null) {
        return Result.failure(IllegalStateException("no user logged in"))
      } else credentials.remove(currentUser)
      return Result.success(Unit)
    }
    return Result.failure(IllegalStateException("timeout"))
  }

  override suspend fun reAuthenticate(email: String, password: String): Result<Unit> {
    if (isOnline) {
      // not important
      return Result.success(Unit)
    }
    return Result.failure(IllegalStateException("timeout"))
  }

  override suspend fun signInWithEmailAndPassword(
      email: String,
      password: String
  ): Result<Boolean> {
    if (isOnline) {
      if (currentUser != null) {
        return Result.failure(IllegalStateException("user $currentUser already logged in"))
      } else if (credentials[email] == password) {
        currentUser = "testUser"
        return Result.success(true)
      } else
          return Result.failure(
              FirebaseAuthException("invalid credentials", "we don't have this user"))
    }
    return Result.failure(IllegalStateException("timeout"))
  }

  override suspend fun signInWithGoogle(credential: Credential): Result<String> {
    if (isOnline) {
      // no-op because no
      return Result.success("success!!")
    }
    return Result.failure(IllegalStateException("timeout"))
  }

  override fun signOut(): Result<Unit> {
    currentUser = null
    return Result.success(Unit)
  }

  override suspend fun signUpWithEmailAndPassword(
      email: String,
      password: String,
      userData: User
  ): Result<String> {
    if (isOnline) {
      if (credentials[email] != null) {
        return Result.failure(
            FirebaseAuthException("already used email", "this throws if I leave it empty"))
      }
      credentials[email] = password
      currentUser = "testUser"
      return Result.success("testUser")
    }
    return Result.failure(IllegalStateException("timeout"))
  }

  override suspend fun checkIsVerified(): Boolean {
    return true
  }

  override suspend fun sendVerificationEmail(): Result<Unit> {
    return Result.success(Unit)
  }
}
