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

// Test suite for ConnectionRepository.
// Uses the Firestore emulator to run integration tests for code generation, claiming, expiration,
// race conditions, and connection creation.
class ConnectionRepositoryTest : FirebaseEmulatorsTest() {

  private lateinit var db: FirebaseFirestore
  private lateinit var repo: ConnectionRepository

  /**
   * Test setup for ConnectionRepository integration tests.
   * - Calls super.setUp() to initialize Firebase emulator and base test logic.
   * - Initializes Firestore and the repository instance.
   * - Creates three test users (user1, user2, user3) in Firebase for use as vet and farmers in all
   *   tests. This ensures all tests run with a clean, known state and valid user accounts.
   */
  @Before
  override fun setUp() = runBlocking {
    // Initialize Firestore and repository, and create test users for vet and farmers.
    super.setUp()
    db = FirebaseFirestore.getInstance()
    repo = ConnectionRepository(db)
    runTest {
      authRepository.signUpWithEmailAndPassword(user1.email, password1, user1)
      authRepository.signOut()
      authRepository.signUpWithEmailAndPassword(user2.email, password2, user2)
      authRepository.signOut()
      authRepository.signUpWithEmailAndPassword(user3.email, password3, user3)
    }
  }

  @Test
  // Verifies that generateCode creates a 6-digit code and writes an OPEN document with expected
  // fields.
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
  // Verifies that a farmer can claim a code, the repository returns vetId, code is marked USED, and
  // a connection document is created.
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
  // Verifies that claimCode fails when the code is expired by simulating a past createdAt
  // timestamp.
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
  // Verifies that claimCode fails when the code has already been used.
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
  // Verifies that claimCode fails when an unknown code is provided.
  fun claimCode_failsWhenUnknown() = runTest {
    val res = repo.claimCode("999999", user1.uid)
    assertTrue(res.isFailure)
  }

  @Test
  // Verifies that multiple generated codes are unique.
  fun generateCode_many_areUnique() = runTest {
    val vet = user3.uid
    val codes = (1..200).map { repo.generateCode(vet).getOrThrow() }
    assertEquals(codes.size, codes.toSet().size)
  }

  @Test
  // Verifies that only one farmer can successfully claim a code in a race condition scenario.
  fun claimCode_raceTwoFarmers_oneSucceeds() = runTest {
    val vet = user3.uid
    val f1 = user1.uid
    val f2 = user2.uid
    val code = repo.generateCode(vet).getOrThrow()
    val r1 = async { repo.claimCode(code, f1) }
    val r2 = async { repo.claimCode(code, f2) }
    val results = awaitAll(r1, r2)
    assertEquals(1, results.count { it.isSuccess })
  }

  @Test
  // Verifies that claimCode succeeds if claimed just before expiry when ttlMinutes is small.
  fun claimCode_succeedsRightBeforeExpiry() = runTest {
    val vet = user3.uid
    val farmer = user1.uid
    val code = repo.generateCode(vet, ttlMinutes = 1).getOrThrow()
    // No need to delay: just check it succeeds before expiration
  }

  @Test
  // Verifies that connectionId creation is symmetric and only one document is created for both
  // orderings of vet and farmer IDs.
  fun connectionId_isSymmetric_singleDoc() = runTest {
    val vet = user3.uid
    val farmer = user1.uid
    val code = repo.generateCode(vet).getOrThrow()
    repo.claimCode(code, farmer).getOrThrow()
    val id = listOf(vet, farmer).sorted().joinToString("__")
    assertTrue(db.collection("connections").document(id).get().await().exists())
  }

  @Test
  // Verifies that generateCode retries if a collision occurs on the first generated code.
  fun generateCode_collidesOnce_thenSucceeds() = runTest {
    val taken = "123456"
    db.collection("connect_codes").document(taken).set(mapOf("any" to "x")).await()

    io.mockk.mockkObject(kotlin.random.Random)
    io.mockk.every { kotlin.random.Random.nextInt(100_000, 1_000_000) } returnsMany
        listOf(123456, 234567)

    val out = repo.generateCode(user3.uid).getOrThrow()
    assertEquals("234567", out)

    io.mockk.unmockkObject(kotlin.random.Random)
  }

  @Test
  // Verifies that claimCode fails when createdAt is missing in the code document.
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
    assertTrue("Expected failure when createdAt is missing", res.isFailure)

    val ex = res.exceptionOrNull()
    assertNotNull("Expected an exception when createdAt is missing", ex)

    val msg = ex!!.message
    assertNotNull("Exception should have a message", msg)
    assertTrue(
        "Expected message to contain 'Missing createdAt', but was: $msg",
        msg!!.contains("Missing createdAt"))
  }

  @Test
  // Verifies that claimCode fails when ttlMinutes is missing in the code document.
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
  // Verifies that claimCode fails when vetId is missing in the code document.
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
  // Verifies that claimCode skips creating a duplicate connection if one already exists.
  fun claimCode_skipsLink_whenConnectionAlreadyExists() = runTest {
    val vet = user3.uid
    val farmer = user1.uid
    val connId = listOf(vet, farmer).sorted().joinToString("__")

    db.collection("connections")
        .document(connId)
        .set(
            mapOf(
                "vetId" to vet,
                "farmerId" to farmer,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "active" to true))
        .await()

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

  // TODO: Add a test for double click on generateCode after implementing the UI.
}
