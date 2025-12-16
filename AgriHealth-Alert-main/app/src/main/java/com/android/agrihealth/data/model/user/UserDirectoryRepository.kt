package com.android.agrihealth.data.model.user

import android.util.Log
import com.android.agrihealth.data.helper.runWithTimeout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

fun interface UserDirectoryDataSource {
  suspend fun getUserSummary(uid: String): UserDirectoryRepository.UserSummary?
}

/**
 * Repository to fetch and cache minimal user info (firstname/lastname/role) by uid. Uses an
 * in-memory cache to avoid repeated Firestore reads.
 */
class UserDirectoryRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val usersCollection: String = "users"
) : UserDirectoryDataSource {
  data class UserSummary(
      val uid: String,
      val firstname: String,
      val lastname: String,
      val role: UserRole?
  )

  companion object {
    private const val TAG = "UserDirectoryRepository"
  }

  // In-memory cache; stores null for negative lookups (deleted or missing users)
  private val cache = mutableMapOf<String, UserSummary?>()

  override suspend fun getUserSummary(uid: String): UserSummary? {
    // Check cache first, including null entries
    if (cache.containsKey(uid)) {
      return cache[uid]
    }

    return try {
      val snap =
          try {
            runWithTimeout(db.collection(usersCollection).document(uid).get())
          } catch (_: Exception) {
            db.collection(usersCollection).document(uid).get(Source.CACHE).await()
          }
      if (!snap.exists()) {
        cache[uid] = null // negative cache
        return null
      }

      val firstname = snap.getString("firstname") ?: ""
      val lastname = snap.getString("lastname") ?: ""
      val roleStr = snap.getString("role")
      val role =
          roleStr?.let {
            // Accepts 'Farmer', 'farmer', 'FARMER', etc.
            runCatching { UserRole.valueOf(it.uppercase()) }.getOrNull()
          }
      val summary = UserSummary(uid, firstname, lastname, role)
      cache[uid] = summary
      summary
    } catch (t: Exception) {
      Log.e(TAG, "Error reading user $uid from Firestore: ${t.message}", t)
      // On error: no caching, allow retry later
      null
    }
  }
}
