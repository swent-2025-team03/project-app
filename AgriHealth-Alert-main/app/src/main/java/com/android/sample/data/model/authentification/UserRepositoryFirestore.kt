package com.android.sample.data.model.authentification

const val USERS_COLLECTION_PATH = "users"

class UserRepositoryFirestore() : UserRepository {

  override fun getNewUid(): String {
    TODO("Not yet implemented")
  }

  override suspend fun addUser(user: User) {
    TODO("Not yet implemented")
  }
}
