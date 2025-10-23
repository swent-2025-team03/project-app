package com.android.agrihealth.data.model.authentification

import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.data.model.user.displayString
import com.android.agrihealth.data.model.user.roleFromDisplayString
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
    val result = getUserFromId(user.uid)
    if (result.isFailure) throw result.exceptionOrNull()!!

    val oldData = result.getOrNull()
    val map = getUpdateMap(oldData!!, user) // cannot be null because result is success

    val illegalKeys = setOf("role", "uid")
    if (map.keys.intersect(illegalKeys).isNotEmpty())
        throw IllegalArgumentException("Permission denied")

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
        "firstname" to user.firstname,
        "lastname" to user.lastname,
        "role" to user.role.displayString(),
        "email" to user.email)
  }

  private fun userFromData(uid: String, data: Map<String, Any>): User {
    val firstname = data["firstname"] as? String ?: throw Exception("Missing firstname")
    val lastname = data["lastname"] as? String ?: throw Exception("Missing lastname")
    val email = data["email"] as? String ?: throw Exception("Missing email")
    val roleStr = data["role"] as? String ?: throw Exception("Missing role")
    val role = roleFromDisplayString(roleStr)

    if (role == UserRole.FARMER) {
      return Farmer(uid, firstname, lastname, email, null, emptyList(), null)
    } else {
      return Vet(uid, firstname, lastname, email, null)
    }
  }

  private fun getUpdateMap(old: User, new: User): Map<String, String> {
    val changes = mutableMapOf<String, String>()

    if (old.uid != new.uid) changes["uid"] = new.uid
    if (old.firstname != new.firstname) changes["name"] = new.firstname
    if (old.lastname != new.lastname) changes["surname"] = new.lastname
    if (old.role != new.role) changes["role"] = new.role.displayString()
    if (old.email != new.email) changes["email"] = new.email

    return changes
  }
}
