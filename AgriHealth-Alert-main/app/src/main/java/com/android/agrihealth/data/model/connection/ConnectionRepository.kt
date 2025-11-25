package com.android.agrihealth.data.model.connection

import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.user.Vet
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.firestore
import java.time.Instant
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
    private const val CONNECTIONS_COLLECTION = FirestoreSchema.Collections.CONNECTIONS
    private const val STATUS_OPEN = FirestoreSchema.Status.OPEN
    private const val STATUS_USED = FirestoreSchema.Status.USED
  }

  private suspend fun getCurrentUserOfficeId(): String {
    val currentUid =
        Firebase.auth.currentUser?.uid
            ?: throw java.lang.IllegalStateException("User not logged in")
    return userRepository.getUserFromId(currentUid).fold({ user ->
      (user as Vet).officeId ?: throw IllegalStateException("You need to join an office")
    }) {
      throw IllegalStateException()
    }
  }

  // Generates a unique connection code for a vet, valid for a limited time (ttlMinutes).
  // Returns: Result<String> containing the generated code, or an exception if failed.
  suspend fun generateCode(ttlMinutes: Long = 10): Result<String> = runCatching {
    val officeId = getCurrentUserOfficeId()
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
                          FirestoreSchema.ConnectCodes.TTL_MINUTES to ttlMinutes))
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
    if (createdAt == null) throw IllegalArgumentException("Missing createdAt")

    val ttlMinutes = snap.getLong(FirestoreSchema.ConnectCodes.TTL_MINUTES)
    if (ttlMinutes == null) throw IllegalArgumentException("Missing TTL value")

    val expiresAtMs = createdAt.toDate().time + ttlMinutes * 60_000
    if (Instant.now().toEpochMilli() > expiresAtMs) throw IllegalStateException("Code expired.")
  }

  // Claims a connection code for a farmer, links the vet and farmer, and marks the code as used.
  // Returns: Result<String> containing the vetId if successful, or an exception if failed.
  suspend fun claimCode(code: String): Result<String> = runCatching {
    val userId = Firebase.auth.uid!!
    val docRef = db.collection(connectionType + CODES_COLLECTION).document(code)
    db.runTransaction { tx ->
          val snap = tx.get(docRef)
          checkCodeValidity(snap)

          val officeId = snap.getString(FirestoreSchema.ConnectCodes.OFFICE_ID)
          if (officeId == null) throw IllegalArgumentException("Invalid office ID.")

          // linkUsers(tx, officeId, userId)

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

  // Creates a connection document between vet and farmer if it does not already exist.
  // Returns: Unit. No value is returned.
  private fun linkUsers(tx: Transaction, officeId: String, userId: String) {
    val connId = connectionId(officeId, userId)
    val ref = db.collection(connectionType + CONNECTIONS_COLLECTION).document(connId)
    // Assume the connection doesn't exist. If not, only the timestamp changes, is it really that
    // bad?
    tx.set(
        ref,
        mapOf(
            FirestoreSchema.Connections.OFFICE_ID to officeId,
            FirestoreSchema.Connections.USER_ID to userId,
            FirestoreSchema.Connections.CREATED_AT to FieldValue.serverTimestamp(),
            FirestoreSchema.Connections.ACTIVE to true)) // this line NEEDS to be here or tests fail
  }

  // Generates a unique connection ID by sorting and joining vet and farmer IDs.
  // Returns: String representing the connection ID.
  // Rationale: sorting the two IDs before joining ensures the produced ID is symmetric
  // and order-independent. This guarantees that connectionId(vet, farmer) ==
  // connectionId(farmer, vet) so the same Firestore document is used for a pair
  // of users regardless of call order, avoiding duplicate connection documents.
  private fun connectionId(a: String, b: String): String = listOf(a, b).sorted().joinToString("__")
}
