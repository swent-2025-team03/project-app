package com.android.agrihealth.data.model.authentification

/** Represents a repository that manages public User item. * */
interface UserRepository {

  /** Generates a new unique identifier for a User item. */
  fun getNewUid(): String

  /**
   * Adds a new User item to the repository.
   *
   * @param user The User to add.
   */
  suspend fun addUser(user: User)
}
