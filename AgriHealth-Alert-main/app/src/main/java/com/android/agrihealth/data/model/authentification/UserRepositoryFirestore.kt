package com.android.agrihealth.data.model.authentification

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

const val USERS_COLLECTION_PATH = "users"

class UserRepositoryFirestore(private val db: FirebaseFirestore = Firebase.firestore) : UserRepository {
  override suspend fun addUser(user: User) {
    val map = mapFromUser(user)
    db.collection(USERS_COLLECTION_PATH).document(user.uid).set(map).await()
  }

  override suspend fun updateUser(user: User) {
    val map = mapFromUser(user)
    db.collection(USERS_COLLECTION_PATH).document(user.uid).update(map).await()
  }

  override suspend fun deleteUser(uid: String) {
    db.collection(USERS_COLLECTION_PATH).document(uid).delete().await()
  }

  private fun mapFromUser(user: User): Map<String, String> {
    return mapOf(
      "name" to user.name,
      "surname" to user.surname,
      "role" to user.role.displayString(),
      "email" to user.email
    )
  }
}
