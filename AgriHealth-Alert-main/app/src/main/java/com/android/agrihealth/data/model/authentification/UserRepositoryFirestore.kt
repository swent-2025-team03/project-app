package com.android.agrihealth.data.model.authentification

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

const val USERS_COLLECTION_PATH = "users"

class UserRepositoryFirestore(private val db: FirebaseFirestore = Firebase.firestore) :
    UserRepository {
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

  override suspend fun getUserFromId(uid: String): Result<User> {
    return try {
      val snapshot = db.collection(USERS_COLLECTION_PATH).document(uid).get().await()

      if (!snapshot.exists()) return Result.failure(NullPointerException("No such user found"))

      val data = snapshot.data ?: return Result.failure(Exception("User has no data"))
      val user = userFromData(uid, data)

      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun mapFromUser(user: User): Map<String, String> {
    return mapOf(
        "name" to user.name,
        "surname" to user.surname,
        "role" to user.role.displayString(),
        "email" to user.email)
  }

  private fun userFromData(uid: String, data: Map<String, Any>): User {
    val name = data["name"] as? String ?: throw Exception("Missing name")
    val surname = data["surname"] as? String ?: throw Exception("Missing surname")
    val email = data["email"] as? String ?: throw Exception("Missing email")
    val roleStr = data["role"] as? String ?: throw Exception("Missing role")
    val role = roleFromDisplayString(roleStr)

    return User(uid, name, surname, role, email)
  }
}
