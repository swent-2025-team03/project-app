package com.android.agrihealth.data.model.authentification

import androidx.credentials.Credential
import com.android.agrihealth.data.model.user.User

interface AuthRepository {
  /**
   * Signs in the user using their email and password.
   *
   * @return A [Result] containing a [Boolean] on success representing the verification status of
   *   the user, or an exception on failure.
   */
  suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Boolean>

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
   * @return A [Result] containing a [String] on success representing the uid of the user, or an
   *   exception on failure.
   */
  suspend fun signInWithGoogle(credential: Credential): Result<String>
  /**
   * Signs up the user using their email, password and profile data.
   *
   * @return A [Result] containing a [String] on success representing the uid of the user, or an
   *   exception on failure.
   */
  suspend fun signUpWithEmailAndPassword(
      email: String,
      password: String,
      userData: User
  ): Result<String>

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

  /**
   * Checks if the current user has verified their email address.
   *
   * @return A [Boolean] indicating if the user's email is verified or not.
   */
  suspend fun checkIsVerified(): Boolean

  /**
   * Sends a verification email to the user.
   *
   * @return A [Result] indicating success or failure.
   */
  suspend fun sendVerificationEmail(): Result<Unit>
}
