package com.android.agrihealth.data.model.authentification

import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.android.agrihealth.data.model.helpers.runWithTimeout
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class AuthRepositoryFirebase(
    private val auth: FirebaseAuth = Firebase.auth,
    private val helper: GoogleSignInHelper = DefaultGoogleSignInHelper()
) : AuthRepository {

  override suspend fun signInWithEmailAndPassword(
      email: String,
      password: String
  ): Result<Boolean> {
    return try {
      val loginResult = runWithTimeout(auth.signInWithEmailAndPassword(email, password))
      val user = loginResult.user ?: return Result.failure(NullPointerException("Log in failed"))

      Result.success(user.isEmailVerified)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun reAuthenticate(email: String, password: String): Result<Unit> {
    return try {
      val credential = EmailAuthProvider.getCredential(email, password)
      runWithTimeout(auth.currentUser!!.reauthenticate(credential))
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun changePassword(password: String): Result<Unit> {
    return try {
      auth.currentUser!!.updatePassword(password)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun sendResetPasswordEmail(email: String): Result<Unit> {
    return try {
      runWithTimeout(auth.sendPasswordResetEmail(email))
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun signInWithGoogle(credential: Credential): Result<String> {
    return try {
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val idToken = helper.extractIdTokenCredential(credential.data).idToken
        val firebaseCred = helper.toFirebaseCredential(idToken)

        // Sign in with Firebase
        val user =
            runWithTimeout(auth.signInWithCredential(firebaseCred)).user
                ?: return Result.failure(
                    IllegalStateException("Login failed : Could not retrieve user information"))

        verifyUser(user.uid)
        Result.success(user.uid)
      } else {
        return Result.failure(
            IllegalStateException("Login failed: Credential is not of type Google ID"))
      }
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Login failed: ${e.localizedMessage ?: "Unexpected error."}"))
    }
  }

  override suspend fun signUpWithEmailAndPassword(
      email: String,
      password: String,
      userData: User
  ): Result<String> {
    val userRepository = UserRepositoryProvider.repository

    return try {
      val creationResult = runWithTimeout(auth.createUserWithEmailAndPassword(email, password))
      val user =
          creationResult.user
              ?: return Result.failure(NullPointerException("Account creation failed"))

      userData.uid = user.uid

      val updatedUser =
          when (userData) {
            is Farmer,
            is Vet -> userData
          }

      try {
        userRepository.addUser(updatedUser)
      } catch (_: Exception) {
        runWithTimeout(user.delete())
        Result.failure<FirebaseUser>(NullPointerException("Account creation failed"))
      }

      Result.success(user.uid)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override fun signOut(): Result<Unit> {
    return try {
      auth.signOut()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun deleteAccount(): Result<Unit> {
    val userRepository = UserRepositoryProvider.repository

    return try {
      val user =
          auth.currentUser ?: return Result.failure(NullPointerException("User not logged in"))

      userRepository.deleteUser(user.uid)
      runWithTimeout(user.delete())

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun checkIsVerified(): Boolean {
    runWithTimeout(auth.currentUser?.reload() ?: return false)
    return auth.currentUser?.isEmailVerified ?: false
  }

  override suspend fun sendVerificationEmail(): Result<Unit> {
    try {
      runWithTimeout(
          auth.currentUser?.sendEmailVerification()
              ?: throw IllegalStateException("You are not logged in"))
    } catch (e: Exception) {
      return Result.failure(e)
    }
    return Result.success(Unit)
  }
}
