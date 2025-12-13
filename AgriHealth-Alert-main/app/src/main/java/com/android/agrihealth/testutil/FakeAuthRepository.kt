package com.android.agrihealth.testutil

import androidx.credentials.Credential
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.ui.authentification.EmailSendStatus
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.delay

class FakeAuthRepository(
    private var isOnline: Boolean = true,
    private var resetPasswordResult: EmailSendStatus = EmailSendStatus.Success,
    private val delayMs: Long = 0L
) : AuthRepository {

  private val credentials = mutableMapOf<String, String>()

  private var currentUser: String? = null

  fun switchConnection(state: Boolean) {
    isOnline = state
  }

  override suspend fun changePassword(password: String): Result<Unit> {
    delay(delayMs)
    if (isOnline) {
      if (currentUser == null) {
        return Result.failure(IllegalStateException("no user logged in"))
      } else credentials[currentUser!!] = password
      return Result.success(Unit)
    }
    return Result.failure(IllegalStateException("timeout"))
  }

  override suspend fun sendResetPasswordEmail(email: String): Result<Unit> {
    delay(delayMs)
    return when (resetPasswordResult) {
      is EmailSendStatus.Success -> Result.success(Unit)
      is EmailSendStatus.Fail -> Result.failure(IllegalArgumentException())
      is EmailSendStatus.Waiting -> {
        delay(10000)
        Result.success(Unit)
      }
      is EmailSendStatus.None -> Result.failure(IllegalArgumentException())
    }
  }

  override suspend fun deleteAccount(): Result<Unit> {
    delay(delayMs)
    if (isOnline) {
      if (currentUser == null) {
        return Result.failure(IllegalStateException("no user logged in"))
      } else credentials.remove(currentUser)
      return Result.success(Unit)
    }
    return Result.failure(IllegalStateException("timeout"))
  }

  override suspend fun reAuthenticate(email: String, password: String): Result<Unit> {
    delay(delayMs)
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
    delay(delayMs)
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
    delay(delayMs)
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
    delay(delayMs)
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
    delay(delayMs)
    return true
  }

  override suspend fun sendVerificationEmail(): Result<Unit> {
    delay(delayMs)
    return Result.success(Unit)
  }
}
