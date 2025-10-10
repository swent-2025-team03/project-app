package com.android.agrihealth.data.model.authentification

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object AuthRepositoryProvider {
  private val _repository: AuthRepository by lazy { AuthRepositoryFirebase() }

  var repository: AuthRepository = _repository
}
