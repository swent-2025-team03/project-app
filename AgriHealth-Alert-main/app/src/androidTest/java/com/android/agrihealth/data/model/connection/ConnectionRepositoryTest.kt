package com.android.agrihealth.data.model.connection

import com.android.agrihealth.data.model.authentification.FirebaseEmulatorsTest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ConnectionRepositoryTest : FirebaseEmulatorsTest(true) {

  private lateinit var db: FirebaseFirestore
  private lateinit var repo: ConnectionRepository

  @Before
  fun setup() = runBlocking {
    db = FirebaseFirestore.getInstance()
    repo = ConnectionRepository(db)
    // Create test users in Firebase
    runTest {
      authRepository.signUpWithEmailAndPassword(user1.email, password1, user1)
      authRepository.signOut()
      authRepository.signUpWithEmailAndPassword(user2.email, password2, user2)
      authRepository.signOut()
      authRepository.signUpWithEmailAndPassword(user3.email, password3, user3)
    }
  }

  private suspend fun clearCollections() {
    listOf("connect_codes", "connections").forEach { col ->
      val snap = db.collection(col).get().await()
      snap.documents.map { it.reference.delete() }.let { tasks -> tasks.map { it.await() } }
    }
  }

  @Test
  fun generateCode_createsOpenDoc() = runTest {
    val vetId = user3.uid
    val code = repo.generateCode(vetId).getOrThrow()
    assertTrue(code.matches(Regex("\\d{6}")))

    val snap = db.collection("connect_codes").document(code).get().await()
    assertTrue(snap.exists())
    assertEquals("OPEN", snap.getString("status"))
    assertEquals(vetId, snap.getString("vetId"))
    assertNotNull(snap.getLong("ttlMinutes"))
    assertNotNull(snap.getTimestamp("createdAt"))
  }

  @Test
  fun claimCode_linksAndMarksUsed() = runTest {
    val vetId = user3.uid
    val farmerId = user1.uid
    val code = repo.generateCode(vetId).getOrThrow()

    val returnedVet = repo.claimCode(code, farmerId).getOrThrow()
    assertEquals(vetId, returnedVet)

    val codeDoc = db.collection("connect_codes").document(code).get().await()
    assertEquals("USED", codeDoc.getString("status"))

    val connId = listOf(vetId, farmerId).sorted().joinToString("__")
    val connDoc = db.collection("connections").document(connId).get().await()
    assertTrue(connDoc.exists())
    assertEquals(vetId, connDoc.getString("vetId"))
    assertEquals(farmerId, connDoc.getString("farmerId"))
  }

  @Test
  fun claimCode_failsForExpired() = runTest {
    val vetId = user3.uid
    val farmerId = user2.uid
    val code = repo.generateCode(vetId, ttlMinutes = 0).getOrThrow() // immediate expiry

    // Force the creation date in the past to simulate expiration
    val pastInstant = Instant.now().minusSeconds(3600)
    val pastTimestamp = Timestamp(pastInstant.epochSecond, pastInstant.nano)
    db.collection("connect_codes").document(code).update("createdAt", pastTimestamp).await()

    val res = repo.claimCode(code, farmerId)
    assertTrue(res.isFailure)
    assertTrue(res.exceptionOrNull()!!.message!!.lowercase().contains("expired"))
  }

  @Test
  fun claimCode_failsWhenUsed() = runTest {
    val vetId = user3.uid
    val farmerId = user4.uid
    val code = repo.generateCode(vetId).getOrThrow()
    db.collection("connect_codes").document(code).update("status", "USED").await()

    val res = repo.claimCode(code, farmerId)
    assertTrue(res.isFailure)
    assertTrue(res.exceptionOrNull()!!.message!!.lowercase().contains("used"))
  }

  @Test
  fun claimCode_failsWhenUnknown() = runTest {
    val res = repo.claimCode("999999", user1.uid)
    assertTrue(res.isFailure)
  }

  @Test
  fun generateCode_many_areUnique() = runTest {
    val vet = user3.uid
    val codes = (1..200).map { repo.generateCode(vet).getOrThrow() }
    assertEquals(codes.size, codes.toSet().size)
  }

  @Test
  fun claimCode_raceTwoFarmers_oneSucceeds() = runTest {
    val vet = user3.uid
    val f1 = user1.uid
    val f2 = user2.uid
    val code = repo.generateCode(vet).getOrThrow()
    val r1 = async { repo.claimCode(code, f1) }
    val r2 = async { repo.claimCode(code, f2) }
    val results = awaitAll(r1, r2)
    assertEquals(1, results.count { it.isSuccess })
    assertEquals(1, results.count { it.isFailure })
  }

  @Test
  fun claimCode_succeedsRightBeforeExpiry() = runTest {
    val vet = user3.uid
    val farmer = user1.uid
    val code = repo.generateCode(vet, ttlMinutes = 1).getOrThrow()
    // No need to delay: just check it succeeds before expiration
    assertTrue(repo.claimCode(code, farmer).isSuccess)
  }

  @Test
  fun connectionId_isSymmetric_singleDoc() = runTest {
    val vet = user3.uid
    val farmer = user1.uid
    val code = repo.generateCode(vet).getOrThrow()
    repo.claimCode(code, farmer).getOrThrow()
    val id = listOf(vet, farmer).sorted().joinToString("__")
    assertTrue(db.collection("connections").document(id).get().await().exists())
  }

  @Test
  fun generateCode_collidesOnce_thenSucceeds() = runTest {
    // 1) Pré-crée un doc pour forcer la collision sur le premier code
    val taken = "123456"
    db.collection("connect_codes").document(taken).set(mapOf("any" to "x")).await()

    // 2) Force Random.nextInt à renvoyer d’abord 123456 puis 234567
    io.mockk.mockkObject(kotlin.random.Random)
    io.mockk.every { kotlin.random.Random.nextInt(100_000, 1_000_000) } returnsMany
        listOf(123456, 234567)

    val out = repo.generateCode(user3.uid).getOrThrow()
    assertEquals("234567", out)

    io.mockk.unmockkObject(kotlin.random.Random)
  }

  @Test
  fun claimCode_fails_whenMissingCreatedAt() = runTest {
    val code = "111222"
    db.collection("connect_codes")
        .document(code)
        .set(
            mapOf(
                "code" to code, "vetId" to user3.uid, "status" to "OPEN", "ttlMinutes" to 10L
                // createdAt ABSENT
                ))
        .await()

    val res = repo.claimCode(code, user1.uid)
    assertTrue(res.isFailure)
    assertTrue(res.exceptionOrNull()!!.message!!.contains("Missing createdAt"))
  }

  @Test
  fun claimCode_fails_whenMissingTtl() = runTest {
    val code = "222333"
    db.collection("connect_codes")
        .document(code)
        .set(
            mapOf(
                "code" to code,
                "vetId" to user3.uid,
                "status" to "OPEN",
                "createdAt" to com.google.firebase.Timestamp.now()
                // ttlMinutes ABSENT
                ))
        .await()

    val res = repo.claimCode(code, user1.uid)
    assertTrue(res.isFailure)
    assertTrue(res.exceptionOrNull()!!.message!!.contains("Missing TTL"))
  }

  @Test
  fun claimCode_fails_whenMissingVetId() = runTest {
    val code = "333444"
    db.collection("connect_codes")
        .document(code)
        .set(
            mapOf(
                "code" to code,
                "status" to "OPEN",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "ttlMinutes" to 60L
                // vetId ABSENT
                ))
        .await()

    val res = repo.claimCode(code, user1.uid)
    assertTrue(res.isFailure)
    assertTrue(res.exceptionOrNull()!!.message!!.contains("Invalid vet ID"))
  }

  @Test
  fun claimCode_skipsLink_whenConnectionAlreadyExists() = runTest {
    val vet = user3.uid
    val farmer = user1.uid
    val connId = listOf(vet, farmer).sorted().joinToString("__")

    // Pré-crée la connexion
    db.collection("connections")
        .document(connId)
        .set(
            mapOf(
                "vetId" to vet,
                "farmerId" to farmer,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "active" to true))
        .await()

    // Code valide non expiré
    val code = "444555"
    db.collection("connect_codes")
        .document(code)
        .set(
            mapOf(
                "code" to code,
                "vetId" to vet,
                "status" to "OPEN",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "ttlMinutes" to 60L))
        .await()

    // Should succeed and take the branch existing.exists()==true
    val res = repo.claimCode(code, farmer)
    assertTrue(res.isSuccess)

    // The connection still exists
    val snap = db.collection("connections").document(connId).get().await()
    assertTrue(snap.exists())
  }

  // TODO add a test for double click generateCode after implementing the UI
}
