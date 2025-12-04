package com.android.agrihealth.data.model.authentification

import com.android.agrihealth.data.model.location.locationFromMap
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
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

    val illegalKeys = setOf("uid")
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

  private fun mapFromUser(user: User): Map<String, Any?> {
    val base =
        mutableMapOf<String, Any?>(
            "firstname" to user.firstname,
            "lastname" to user.lastname,
            "email" to user.email,
            "role" to
                when (user) { // keep for type reconstruction
                  is Farmer -> "Farmer"
                  is Vet -> "Vet"
                },
            "isGoogleAccount" to user.isGoogleAccount,
            "description" to user.description,
            "collected" to user.collected)

    // Add type-specific fields
    when (user) {
      is Farmer -> {
        base["address"] = user.address
        base["linkedOffices"] = user.linkedOffices
        base["defaultOffice"] = user.defaultOffice
      }
      is Vet -> {
        base["address"] = user.address
        base["validCodes"] = user.validCodes
        base["officeId"] = user.officeId
      }
    }

    return base
  }

  private fun userFromData(uid: String, data: Map<String, Any>): User {
    val firstname = data["firstname"] as? String ?: throw Exception("Missing firstname")
    val lastname = data["lastname"] as? String ?: throw Exception("Missing lastname")
    val email = data["email"] as? String ?: throw Exception("Missing email")
    val roleStr = data["role"] as? String ?: throw Exception("Missing role")
    val collected = data["collected"] as? Boolean ?: throw Exception("Missing data collect flag")
    val isGoogleAccount = data["isGoogleAccount"] as? Boolean ?: false
    val description = data["description"] as? String?
    val addressData = data["address"] as? Map<*, *>
    val address = locationFromMap(addressData)

    return when (roleStr) {
      "Farmer" ->
          Farmer(
              uid = uid,
              firstname = firstname,
              lastname = lastname,
              email = email,
              address = address,
              linkedOffices = data["linkedOffices"] as? List<String> ?: emptyList(),
              defaultOffice = data["defaultOffice"] as? String,
              isGoogleAccount = isGoogleAccount,
              description = description,
              collected = collected)
      "Vet" ->
          Vet(
              uid = uid,
              firstname = firstname,
              lastname = lastname,
              email = email,
              address = address,
              validCodes = data["validCodes"] as? List<String> ?: emptyList(),
              officeId = data["officeId"] as? String?,
              isGoogleAccount = isGoogleAccount,
              description = description,
              collected = collected)
      else -> throw Exception("Unknown user type: $roleStr")
    }
  }

  private fun getUpdateMap(old: User, new: User): Map<String, Any?> {
    val changes = mutableMapOf<String, Any?>()

    if (old.uid != new.uid) changes["uid"] = new.uid
    if (old.firstname != new.firstname) changes["firstname"] = new.firstname
    if (old.lastname != new.lastname) changes["lastname"] = new.lastname
    if (old.email != new.email) changes["email"] = new.email
    if (old.isGoogleAccount != new.isGoogleAccount) changes["isGoogleAccount"] = new.isGoogleAccount
    if (old.description != new.description) changes["description"] = new.description
    if (old.collected != new.collected) changes["collected"] = new.collected

    when {
      old is Farmer && new is Farmer -> {
        if (old.address != new.address) changes["address"] = new.address
        if (old.linkedOffices != new.linkedOffices) changes["linkedOffices"] = new.linkedOffices
        if (old.defaultOffice != new.defaultOffice) changes["defaultOffice"] = new.defaultOffice
      }
      old is Vet && new is Vet -> {
        if (old.address != new.address) changes["address"] = new.address
        if (old.validCodes != new.validCodes) changes["validCodes"] = new.validCodes
        if (old.officeId != new.officeId) changes["officeId"] = new.officeId
      }
      else -> throw IllegalArgumentException("Permission denied")
    }

    return changes
  }
}
