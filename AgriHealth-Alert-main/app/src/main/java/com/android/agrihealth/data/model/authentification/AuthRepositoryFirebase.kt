package com.android.agrihealth.data.model.authentification

import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class AuthRepositoryFirebase(
    private val auth: FirebaseAuth = Firebase.auth,
    private val helper: GoogleSignInHelper = DefaultGoogleSignInHelper()
) : AuthRepository {

  override suspend fun signInWithEmailAndPassword(
      email: String,
      password: String
  ): Result<FirebaseUser> {
    return try {
      val loginResult = auth.signInWithEmailAndPassword(email, password).await()
      val user = loginResult.user ?: return Result.failure(NullPointerException("Log in failed"))

      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> {
    return try {
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val idToken = helper.extractIdTokenCredential(credential.data).idToken
        val firebaseCred = helper.toFirebaseCredential(idToken)

        // Sign in with Firebase
        val user =
            auth.signInWithCredential(firebaseCred).await().user
                ?: return Result.failure(
                    IllegalStateException("Login failed : Could not retrieve user information"))

        return Result.success(user)
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
  ): Result<FirebaseUser> {
    val userRepository = UserRepositoryProvider.repository

    return try {
      val creationResult = auth.createUserWithEmailAndPassword(email, password).await()
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
      } catch (e: Exception) {
        user.delete().await()
        Result.failure<FirebaseUser>(NullPointerException("Account creation failed"))
      }

      Result.success(user)
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
      user.delete().await()

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
