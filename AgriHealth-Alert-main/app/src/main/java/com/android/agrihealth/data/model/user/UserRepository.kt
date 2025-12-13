package com.android.agrihealth.data.model.user

/** Represents a repository that manages public User item and its data. * */
interface UserRepository {
  /**
   * Adds a new User item to the repository.
   *
   * @param user The User to add.
   */
  suspend fun addUser(user: User)

  /**
   * Updates an existing User item to the repository.
   *
   * @param user The new User data to replace the old one.
   */
  suspend fun updateUser(user: User)

  /**
   * Removes a User from the repository. Note that this only removes the user data, not the account.
   *
   * @param uid The ID of the user to delete.
   */
  suspend fun deleteUser(uid: String)

  /**
   * Gets the data associated to the given user ID.
   *
   * @param uid The ID of the user to fetch.
   */
  suspend fun getUserFromId(uid: String): Result<User>

  companion object
}
