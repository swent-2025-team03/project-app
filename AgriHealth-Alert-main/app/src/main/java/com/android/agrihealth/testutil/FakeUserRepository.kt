package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.user.User

class FakeUserRepository(private var targetUser: User? = null) : UserRepository {

  override suspend fun addUser(user: User) {
    targetUser = user
  }

  override suspend fun updateUser(user: User) {
    if (targetUser != null && targetUser?.uid == user.uid) {
      targetUser = user
    } else {
      throw NoSuchElementException("User not found")
    }
  }

  override suspend fun deleteUser(uid: String) {
    if (targetUser != null && targetUser?.uid == uid) {
      targetUser = null
    } else {
      throw NoSuchElementException("User not found")
    }
  }

  override suspend fun getUserFromId(uid: String): Result<User> {
    return if (targetUser != null && targetUser?.uid == uid) {
      Result.success(targetUser!!)
    } else {
      Result.failure(NoSuchElementException("User not found"))
    }
  }
}
