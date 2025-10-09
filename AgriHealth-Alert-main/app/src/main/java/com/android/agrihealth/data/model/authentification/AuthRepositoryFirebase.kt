package com.android.agrihealth.data.model.authentification

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
        val loginResult = auth.signInWithEmailAndPassword(email, password).await()
        val user = loginResult.user ?: return Result.failure(NullPointerException("Log in failed"))

        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
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
          val user = creationResult.user ?: return Result.failure(NullPointerException("Account creation failed"))

          userRepository.addUser(userData.copy(uid = user.uid, email = email))

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
          val user = auth.currentUser ?: return Result.failure(NullPointerException("User not logged in"))

          userRepository.deleteUser(user.uid)
          user.delete().await()

          Result.success(Unit)
      } catch (e: Exception) {
          Result.failure(e)
      }
  }
}
