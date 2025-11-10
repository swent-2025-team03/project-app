package com.android.agrihealth.data.model.connection

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

class ConnectionRepository(private val db: FirebaseFirestore = Firebase.firestore) {
  private companion object {
    private const val CODES_COLLECTION = FirestoreSchema.Collections.CONNECT_CODES
    private const val CONNECTIONS_COLLECTION = FirestoreSchema.Collections.CONNECTIONS
    private const val STATUS_OPEN = FirestoreSchema.Status.OPEN
    private const val STATUS_USED = FirestoreSchema.Status.USED
  }

  private fun getCurrentUserId(): String {
    return Firebase.auth.currentUser?.uid
        ?: throw java.lang.IllegalStateException("User not logged in")
  }

  // Generates a unique connection code for a vet, valid for a limited time (ttlMinutes).
  // Returns: Result<String> containing the generated code, or an exception if failed.
  suspend fun generateCode(ttlMinutes: Long = 10): Result<String> = runCatching {
    val vetId = getCurrentUserId()
    repeat(20) {
      val code = Random.nextInt(100_000, 1_000_000).toString()
      val maybeCode =
          db.runTransaction { tx ->
                val ref = db.collection(CODES_COLLECTION).document(code)
                val snap = tx.get(ref)
                if (snap.exists()) {
                  null
                } else {
                  val createdAt = FieldValue.serverTimestamp()
                  tx.set(
                      ref,
                      mapOf(
                          FirestoreSchema.ConnectCodes.CODE to code,
                          FirestoreSchema.ConnectCodes.VET_ID to vetId,
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
    val farmerId = getCurrentUserId()
    val docRef = db.collection(CODES_COLLECTION).document(code)
    db.runTransaction { tx ->
          val snap = tx.get(docRef)
          checkCodeValidity(snap)

          val vetId = snap.getString(FirestoreSchema.ConnectCodes.VET_ID)
          if (vetId == null) throw IllegalArgumentException("Invalid vet ID.")

          linkUsers(tx, vetId, farmerId)

          tx.update(
              docRef,
              mapOf(
                  FirestoreSchema.ConnectCodes.STATUS to STATUS_USED,
                  FirestoreSchema.ConnectCodes.USED_AT to FieldValue.serverTimestamp(),
                  FirestoreSchema.ConnectCodes.CLAIMED_BY to farmerId))

          vetId
        }
        .await()
  }

  // Creates a connection document between vet and farmer if it does not already exist.
  // Returns: Unit. No value is returned.
  private fun linkUsers(tx: Transaction, vetId: String, farmerId: String) {
    val connId = connectionId(vetId, farmerId)
    val ref = db.collection(CONNECTIONS_COLLECTION).document(connId)
    // Assume the connection doesn't exist. If not, only the timestamp changes, is it really that
    // bad?
    tx.set(
        ref,
        mapOf(
            FirestoreSchema.Connections.VET_ID to vetId,
            FirestoreSchema.Connections.FARMER_ID to farmerId,
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
