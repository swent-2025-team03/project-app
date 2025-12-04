package com.android.agrihealth.data.model.connection

import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.user.Vet
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class ConnectionRepository(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val connectionType: String
) {
  private companion object {
    private const val CODES_COLLECTION = FirestoreSchema.Collections.CONNECT_CODES
    private const val STATUS_OPEN = FirestoreSchema.Status.OPEN
    private const val STATUS_USED = FirestoreSchema.Status.USED
  }

  private fun getCurrentUserId(): String {
    return Firebase.auth.currentUser?.uid
        ?: throw java.lang.IllegalStateException("User not logged in")
  }

  private suspend fun getCurrentUserOfficeId(): String {
    val currentUid = getCurrentUserId()
    return userRepository.getUserFromId(currentUid).fold({ user ->
      (user as Vet).officeId
          ?: throw IllegalStateException("You need to join an office before generating a code")
    }) { throwable ->
      throw throwable
    }
  }

  // Generates a unique connection code for an office, valid for a limited time (ttlMinutes).
  // Returns: Result<String> containing the generated code, or an exception if failed.
  suspend fun generateCode(type: String): Result<String> = runCatching {
    val officeId = getCurrentUserOfficeId()

    val currentUser =
        userRepository.getUserFromId(getCurrentUserId()).getOrNull()
            ?: throw IllegalStateException("User not found")
    val vet = currentUser as? Vet ?: throw IllegalStateException("Only vets can generate codes")

    val activeCount = getActiveCodesCount(vet, type)
    if (activeCount >= 10) {
      throw IllegalStateException("Cannot generate more than 10 active $type codes")
    }

    repeat(20) {
      val code = Random.nextInt(100_000, 1_000_000).toString()
      val maybeCode =
          db.runTransaction { tx ->
                val ref = db.collection(connectionType + CODES_COLLECTION).document(code)
                val snap = tx.get(ref)
                if (snap.exists()) {
                  null
                } else {
                  val createdAt = FieldValue.serverTimestamp()
                  tx.set(
                      ref,
                      mapOf(
                          FirestoreSchema.ConnectCodes.CODE to code,
                          FirestoreSchema.ConnectCodes.OFFICE_ID to officeId,
                          FirestoreSchema.ConnectCodes.STATUS to STATUS_OPEN,
                          FirestoreSchema.ConnectCodes.CREATED_AT to createdAt,
                          FirestoreSchema.ConnectCodes.TYPE to type))
                  code
                }
              }
              .await()

      if (maybeCode != null) return@runCatching maybeCode
      delay(Random.nextLong(50, 200))
    }
    throw IllegalStateException("Failed to generate a unique code.")
  }

  private fun checkCodeValidity(snap: DocumentSnapshot) {
    if (!snap.exists()) throw IllegalArgumentException("Code not found.")

    val status = snap.getString(FirestoreSchema.ConnectCodes.STATUS)
    if (status != STATUS_OPEN) throw IllegalStateException("Code already used.")

    val createdAt = snap.getTimestamp(FirestoreSchema.ConnectCodes.CREATED_AT)
    requireNotNull(createdAt) { "Missing createdAt" }
  }

  // Claims a connection code for a farmer, and marks the code as used.
  // Returns: Result<String> containing the vetId if successful, or an exception if failed.
  suspend fun claimCode(code: String): Result<String> = runCatching {
    val userId = getCurrentUserId()
    val docRef = db.collection(connectionType + CODES_COLLECTION).document(code)
    db.runTransaction { tx ->
          val snap = tx.get(docRef)
          checkCodeValidity(snap)

          val officeId = snap.getString(FirestoreSchema.ConnectCodes.OFFICE_ID)
          requireNotNull(officeId) { "Invalid office ID." }

          tx.update(
              docRef,
              mapOf(
                  FirestoreSchema.ConnectCodes.STATUS to STATUS_USED,
                  FirestoreSchema.ConnectCodes.USED_AT to FieldValue.serverTimestamp(),
                  FirestoreSchema.ConnectCodes.CLAIMED_BY to userId))

          officeId
        }
        .await()
  }

  suspend fun getValidCodes(vet: Vet, type: String): List<String> {
    val targetList =
        when (type) {
          "FARMER" -> vet.farmerConnectCodes
          "VET" -> vet.vetConnectCodes
          else -> return emptyList()
        }

    val collectionName =
        when (type) {
          "FARMER" -> "farmerToOfficeConnectCodes"
          "VET" -> "vetToOfficeConnectCodes"
          else -> return emptyList()
        }

    return try {
      val snapshot =
          db.collection(collectionName)
              .whereIn(FieldPath.documentId(), targetList)
              .whereEqualTo(FirestoreSchema.ConnectCodes.STATUS, FirestoreSchema.Status.OPEN)
              .get()
              .await()

      snapshot.documents.map { it.id }
    } catch (e: Exception) {
      emptyList()
    }
  }

  suspend fun getActiveCodesCount(vet: Vet, type: String): Int {
    val (targetList, collectionName) =
        when (type) {
          "FARMER" -> vet.farmerConnectCodes to "farmerToOfficeConnectCodes"
          "VET" -> vet.vetConnectCodes to "vetToOfficeConnectCodes"
          else -> return 0
        }

    if (targetList.isEmpty()) return 0

    return try {
      val snapshot =
          db.collection(collectionName)
              .whereIn(FieldPath.documentId(), targetList)
              .whereEqualTo(FirestoreSchema.ConnectCodes.STATUS, FirestoreSchema.Status.OPEN)
              .get()
              .await()

      snapshot.size()
    } catch (e: Exception) {
      0
    }
  }
}
