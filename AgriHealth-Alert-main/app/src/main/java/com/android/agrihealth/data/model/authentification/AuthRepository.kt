package com.android.agrihealth.data.model.authentification

import androidx.credentials.Credential
import com.android.agrihealth.data.model.user.User
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
  /**
   * Signs in the user using their email and password.
   *
   * @return A [Result] containing a [FirebaseUser] on success, or an exception on failure.
   */
  suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser>

  /**
   * Re-signs in the User for sensitive account modifications (ex: changing password)
   *
   * @return A [Result] indicating success or failure.
   */
  suspend fun reAuthenticate(email: String, password: String): Result<Unit>

  /**
   * Updates a User's password, the user needs be reauthenticated before this.
   *
   * @return A [Result] indicating success or failure.
   */
  suspend fun changePassword(password: String): Result<Unit>
  /**
   * Signs in the user using their google account.
   *
   * @return A [Result] containing a [FirebaseUser] on success, or an exception on failure.
   */
  suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser>
  /**
   * Signs up the user using their email, password and profile data.
   *
   * @return A [Result] containing a [FirebaseUser] on success, or an exception on failure.
   */
  suspend fun signUpWithEmailAndPassword(
      email: String,
      password: String,
      userData: User
  ): Result<FirebaseUser>

  /**
   * Signs out the currently authenticated user and clears the credential state.
   *
   * @return A [Result] indicating success or failure.
   */
  fun signOut(): Result<Unit>

  /**
   * Deletes the account of the authenticated user along with their data.
   *
   * @return A [Result] indicating success or failure.
   */
  suspend fun deleteAccount(): Result<Unit>
}
