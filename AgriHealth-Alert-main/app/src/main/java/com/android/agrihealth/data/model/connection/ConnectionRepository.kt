package com.android.agrihealth.data.model.connection

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

data class ConnectionCode(
    val code: String = "",
    val vetId: String = "",
    val status: String = "",
    val createdAt: Timestamp? = null,
    val expiresAtMs: Long? = null
)

class ConnectionRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private companion object {
        private const val CODES_COLLECTION = "connect_codes"
        private const val STATUS_OPEN = "OPEN"
        private const val STATUS_USED = "USED"
    }

    suspend fun generateCode(vetId: String, ttlMinutes: Long = 10): Result<String> = runCatching {
        repeat(8) {
            val code = Random.nextInt(100_000, 1_000_000).toString()
            val ref = db.collection(CODES_COLLECTION).document(code)
            val snap = ref.get().await()
            if (!snap.exists()) {
                val nowMs = System.currentTimeMillis()
                val expiresMs = nowMs + ttlMinutes * 60_000

                val codeObj = ConnectionCode(
                    code = code,
                    vetId = vetId,
                    status = STATUS_OPEN,
                    createdAt = null, // will be filled by serverTimestamp
                    expiresAtMs = expiresMs
                )

                ref.set(codeObj).await()
                // server time for createdAt ensures sync across clients
                ref.update("createdAt", FieldValue.serverTimestamp()).await()

                return@runCatching code
            }
        }
        throw IllegalStateException("Failed to generate a unique code.")
    }

    suspend fun claimCode(code: String, farmerId: String): Result<String> = runCatching {
        val docRef = db.collection(CODES_COLLECTION).document(code)
        db.runTransaction { tx ->
            val snap = tx.get(docRef)
            if (!snap.exists()) throw IllegalArgumentException("Code not found.")
            if (snap.getString("status") != STATUS_OPEN) throw IllegalStateException("Code already used.")
            val expiresAtMs = snap.getLong("expiresAtMs") ?: throw IllegalArgumentException("Invalid expiration date.")
            if (System.currentTimeMillis() > expiresAtMs) throw IllegalStateException("Code expired.")

            val vetId = snap.getString("vetId") ?: throw IllegalArgumentException("Invalid vet ID.")

            linkUsers(tx, vetId, farmerId)
            tx.update(docRef, "status", STATUS_USED)

            vetId
        }.await()
    }

    private fun linkUsers(
        tx: Transaction,
        vetId: String,
        farmerId: String
    ) {
        val connId = connectionId(vetId, farmerId)
        val ref = db.collection("connections").document(connId)
        val existing = tx.get(ref)
        if (!existing.exists()) {
            tx.set(
                ref,
                mapOf(
                    "vetId" to vetId,
                    "farmerId" to farmerId,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "active" to true
                )
            )
        }
    }

    private fun connectionId(a: String, b: String): String =
        listOf(a, b).sorted().joinToString("__")
}
