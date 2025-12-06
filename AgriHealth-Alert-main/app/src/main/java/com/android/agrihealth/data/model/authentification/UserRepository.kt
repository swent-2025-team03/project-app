package com.android.agrihealth.data.model.authentification

import com.android.agrihealth.data.model.user.User

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

  /**
   * Gets the IDs of all Vet users belonging to the given office ID.
   *
   * @param officeId The ID of the office to filter Vets by.
   * @return A list of Vet user IDs assigned to that office.
   */
  suspend fun getVetsInOffice(officeId: String): List<String>

  companion object
}
