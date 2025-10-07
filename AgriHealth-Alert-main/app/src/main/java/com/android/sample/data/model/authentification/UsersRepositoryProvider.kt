package com.android.sample.data.model.authentification

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object UsersRepositoryProvider {
  private val _repository: UserRepository by lazy { UserRepositoryFirestore() }

  var repository: UserRepository = _repository
}
