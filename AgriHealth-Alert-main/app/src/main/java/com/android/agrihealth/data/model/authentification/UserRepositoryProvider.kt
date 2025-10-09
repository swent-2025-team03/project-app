package com.android.agrihealth.data.model.authentification

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object UserRepositoryProvider {
  private val _repository: UserRepository by lazy { UserRepositoryFirestore() }

  var repository: UserRepository = _repository
}
