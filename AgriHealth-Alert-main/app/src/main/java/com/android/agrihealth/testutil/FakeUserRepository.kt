package com.android.agrihealth.fakes

import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User

class FakeUserRepository(private val userAddress: Location? = null) : UserRepository {

  private val store = mutableMapOf<String, User>()

  override suspend fun getUserFromId(uid: String): Result<User> {
    val user =
        store[uid]
            ?: Farmer(
                uid = uid,
                firstname = "Test",
                lastname = "User",
                email = "test@example.com",
                address = userAddress,
                linkedVets = emptyList(),
                defaultVet = null,
                isGoogleAccount = false,
                description = null)
    return Result.success(user)
  }

  override suspend fun addUser(user: User) {
    store[user.uid] = user
  }

  override suspend fun updateUser(user: User) {
    // Remplace ou insère l'utilisateur
    store[user.uid] = user
  }

  override suspend fun deleteUser(uid: String) {
    store.remove(uid)
  }
}
