package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.user.User
import kotlinx.coroutines.delay

private const val USER_NOT_FOUND = "User not found"

class FakeUserRepository(private var targetUser: User? = null, private val delayMs: Long = 0L) :
    UserRepository {

  /** Returns true if the in-memory user matches the given uid. */
  private fun matches(uid: String): Boolean = targetUser?.uid == uid

  override suspend fun addUser(user: User) {
    delay(delayMs)
    targetUser = user
  }

  override suspend fun updateUser(user: User) {
    delay(delayMs)
    if (matches(user.uid)) {
      targetUser = user
    } else {
      throw NoSuchElementException(USER_NOT_FOUND)
    }
  }

  override suspend fun deleteUser(uid: String) {
    delay(delayMs)
    if (matches(uid)) {
      targetUser = null
    }
  }

  override suspend fun getUserFromId(uid: String): Result<User> {
    delay(delayMs)
    return getUserFromIdSync(uid)
  }

  fun getUserFromIdSync(uid: String): Result<User> {
    return if (matches(uid)) {
      Result.success(targetUser!!)
    } else {
      Result.failure(NoSuchElementException(USER_NOT_FOUND))
    }
  }
}
