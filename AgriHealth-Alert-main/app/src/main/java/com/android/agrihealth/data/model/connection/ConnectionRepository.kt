package com.android.agrihealth.data.model.connection

import com.android.agrihealth.core.constants.FirestoreSchema
import com.android.agrihealth.data.helper.withDefaultTimeout
import com.android.agrihealth.data.model.user.UserRepository
import com.android.agrihealth.data.model.user.UserRepositoryProvider
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.profile.CodeType
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

/** Connects two users together by exchanging a connection code */
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

  val type: String
    get() = connectionType

  private fun humanReadableConnectionType(): String =
      when (connectionType) {
        "farmerToOffice" -> "farmer connection"
        "vetToOffice" -> "vet connection"
        else -> "connection"
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

  /** Generates a connection code, to be claimed by another user */
  suspend fun generateCode(): Result<String> = runCatching {
    val officeId = getCurrentUserOfficeId()

    val currentUser =
        userRepository.getUserFromId(getCurrentUserId()).getOrNull()
            ?: throw IllegalStateException("User not found")
    val vet = currentUser as? Vet ?: throw IllegalStateException("Only vets can generate codes")

    val activeCount = getActiveCodesCount(vet)
    if (activeCount >= 10) {
      throw IllegalStateException(
          "Cannot generate more than 10 active ${humanReadableConnectionType()} codes")
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
                          FirestoreSchema.ConnectCodes.CREATED_AT to createdAt))
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

  /** Claims a connection code and marks the code as used */
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

  /**
   * Retrieves the list of valid (OPEN) connection codes for the given vet and code type. Used by
   * the EditProfileScreen to display only active and usable codes to the user.
   */
  suspend fun getValidCodes(vet: Vet, type: CodeType): List<String> {
    val targetList =
        when (type) {
          CodeType.FARMER -> vet.farmerConnectCodes
          CodeType.VET -> vet.vetConnectCodes
        }

    val collectionName =
        when (type) {
          CodeType.FARMER -> "farmerToOfficeConnectCodes"
          CodeType.VET -> "vetToOfficeConnectCodes"
        }

    return try {
      val snapshot =
          try {
            withDefaultTimeout(
                db.collection(collectionName)
                    .whereIn(FieldPath.documentId(), targetList)
                    .whereEqualTo(FirestoreSchema.ConnectCodes.STATUS, FirestoreSchema.Status.OPEN)
                    .get())
          } catch (_: Exception) {
            db.collection(collectionName)
                .whereIn(FieldPath.documentId(), targetList)
                .whereEqualTo(FirestoreSchema.ConnectCodes.STATUS, FirestoreSchema.Status.OPEN)
                .get(Source.CACHE)
                .await()
          }

      snapshot.documents.map { it.id }
    } catch (_: Exception) {
      emptyList()
    }
  }

  /**
   * Returns the number of active (OPEN) connection codes for the given vet and code type. This is
   * used to determine whether the vet can generate additional codes based on the limit.
   */
  suspend fun getActiveCodesCount(vet: Vet): Int {
    val (targetList, collectionName) =
        when (connectionType) {
          "farmerToOffice" -> vet.farmerConnectCodes to "farmerToOfficeConnectCodes"
          "vetToOffice" -> vet.vetConnectCodes to "vetToOfficeConnectCodes"
          else -> return 0
        }

    if (targetList.isEmpty()) return 0

    return try {
      val snapshot =
          try {
            withDefaultTimeout(
                db.collection(collectionName)
                    .whereIn(FieldPath.documentId(), targetList)
                    .whereEqualTo(FirestoreSchema.ConnectCodes.STATUS, FirestoreSchema.Status.OPEN)
                    .get())
          } catch (_: Exception) {
            db.collection(collectionName)
                .whereIn(FieldPath.documentId(), targetList)
                .whereEqualTo(FirestoreSchema.ConnectCodes.STATUS, FirestoreSchema.Status.OPEN)
                .get(Source.CACHE)
                .await()
          }
      snapshot.size()
    } catch (_: Exception) {
      0
    }
  }
}
