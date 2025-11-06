package com.android.agrihealth.data.model.user

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository to fetch and cache minimal user info (firstname/lastname/role) by uid. Uses an
 * in-memory cache to avoid repeated Firestore reads.
 */
class UserDirectoryRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val usersCollection: String = "users"
) {
  data class UserSummary(
      val uid: String,
      val firstname: String,
      val lastname: String,
      val role: UserRole?
  )

  // Simple in-memory cache; stores null for negative lookups (deleted users)
  private val cache = mutableMapOf<String, UserSummary?>()

  suspend fun getUserSummary(uid: String): UserSummary? {
    cache[uid]?.let {
      return it
    }
    val snap = db.collection(usersCollection).document(uid).get().await()
    if (!snap.exists()) {
      cache[uid] = null
      return null
    }
    val firstname = snap.getString("firstname") ?: ""
    val lastname = snap.getString("lastname") ?: ""
    val roleStr = snap.getString("role")
    val role = roleStr?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
    val summary = UserSummary(uid, firstname, lastname, role)
    cache[uid] = summary
    return summary
  }
}
