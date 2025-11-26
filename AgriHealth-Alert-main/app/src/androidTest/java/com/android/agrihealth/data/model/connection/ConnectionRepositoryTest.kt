package com.android.agrihealth.data.model.connection

import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
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
    repo = ConnectionRepository(db, connectionType = "farmerToOffice")
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
    val officeId = user3.officeId!!
    val code = repo.generateCode().getOrThrow()
    assertTrue(code.matches(Regex("\\d{6}")))

    val snap = db.collection("farmerToOfficeConnectCodes").document(code).get().await()
    assertTrue(snap.exists())
    assertEquals("OPEN", snap.getString("status"))
    assertEquals(officeId, snap.getString("officeId"))
    assertNotNull(snap.getLong("ttlMinutes"))
    assertNotNull(snap.getTimestamp("createdAt"))
  }

  @Test
  // Verifies that a farmer can claim a code, the repository returns vetId, code is marked USED, and
  // a connection document is created.
  fun claimCode_linksAndMarksUsed() = runTest {
    val officeId = user3.officeId!!
    val code = repo.generateCode().getOrThrow()

    authRepository.signOut()
    authRepository.signInWithEmailAndPassword(user1.email, password1)

    val returnedVet = repo.claimCode(code).getOrThrow()
    assertEquals(officeId, returnedVet)

    val codeDoc = db.collection("farmerToOfficeConnectCodes").document(code).get().await()
    assertEquals("USED", codeDoc.getString("status"))
  }

  @Test
  // Verifies that claimCode fails when the code is expired by simulating a past createdAt
  // timestamp.
  fun claimCode_failsForExpired() = runTest {
    val code = repo.generateCode(ttlMinutes = -1).getOrThrow() // immediate expiry

    authRepository.signOut()
    authRepository.signInWithEmailAndPassword(user2.email, password2)

    val res = repo.claimCode(code)
    assertTrue(res.isFailure)
    assertTrue(res.exceptionOrNull()!!.message!!.lowercase().contains("expired"))
  }

  @Test
  // Verifies that claimCode fails when the code has already been used.
  fun claimCode_failsWhenUsed() = runTest {
    val code = repo.generateCode().getOrThrow()

    authRepository.signOut()
    authRepository.signInWithEmailAndPassword(user1.email, password1)

    repo.claimCode(code)
    val res = repo.claimCode(code)
    assertTrue(res.isFailure)
    assertTrue(res.exceptionOrNull()!!.message!!.lowercase().contains("used"))
  }

  @Test
  // Verifies that claimCode fails when an unknown code is provided.
  fun claimCode_failsWhenUnknown() = runTest {
    val res = repo.claimCode("999999")
    assertTrue(res.isFailure)
  }

  @Test
  // Verifies that multiple generated codes are unique.
  fun generateCode_many_areUnique() = runTest {
    val codes = (1..200).map { repo.generateCode().getOrThrow() }
    assertEquals(codes.size, codes.toSet().size)
  }

  @Test
  // Verifies that only one farmer can successfully claim a code in a race condition scenario.
  fun claimCode_raceTwoFarmers_oneSucceeds() = runTest {
    val code = repo.generateCode().getOrThrow()

    val r1 = async {
      authRepository.signOut()
      authRepository.signInWithEmailAndPassword(user1.email, password1)
      repo.claimCode(code)
    }
    val r2 = async {
      authRepository.signOut()
      authRepository.signInWithEmailAndPassword(user2.email, password2)
      repo.claimCode(code)
    }
    val results = awaitAll(r1, r2)
    assertEquals(1, results.count { it.isSuccess })
  }

  @Test
  // Verifies that claimCode succeeds if claimed just before expiry when ttlMinutes is small.
  fun claimCode_succeedsRightBeforeExpiry() = runTest {
    val code = repo.generateCode(ttlMinutes = 1).getOrThrow()
    authRepository.signOut()
    authRepository.signInWithEmailAndPassword(user1.email, password1)
    val res = repo.claimCode(code)
    assertTrue(res.isSuccess)
  }

  @Test
  // Verifies that generateCode retries if a collision occurs on the first generated code.
  fun generateCode_collidesOnce_thenSucceeds() = runTest {
    val taken = "123456"
    db.collection("farmerToOfficeConnectCodes").document(taken).set(mapOf("any" to "x")).await()

    io.mockk.mockkObject(kotlin.random.Random)
    io.mockk.every { kotlin.random.Random.nextInt(100_000, 1_000_000) } returnsMany
        listOf(123456, 234567)

    val out = repo.generateCode().getOrThrow()
    assertEquals("234567", out)

    io.mockk.unmockkObject(kotlin.random.Random)
  }

  @Test
  // Verifies that claimCode fails when createdAt is missing in the code document.
  fun claimCode_fails_whenMissingCreatedAt() = runTest {
    val code = "111222"
    db.collection("farmerToOfficeConnectCodes")
        .document(code)
        .set(
            mapOf(
                "code" to code, "officeId" to user3.uid, "status" to "OPEN", "ttlMinutes" to 10L
                // createdAt ABSENT
                ))
        .await()

    val res = repo.claimCode(code)
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
    db.collection("farmerToOfficeConnectCodes")
        .document(code)
        .set(
            mapOf(
                "code" to code,
                "officeId" to user3.uid,
                "status" to "OPEN",
                "createdAt" to Timestamp.now()
                // ttlMinutes ABSENT
                ))
        .await()

    val res = repo.claimCode(code)
    assertTrue(res.isFailure)
    assertTrue(res.exceptionOrNull()!!.message!!.contains("Missing TTL"))
  }

  @Test
  // Verifies that claimCode fails when vetId is missing in the code document.
  fun claimCode_fails_whenMissingVetId() = runTest {
    val code = "333444"
    db.collection("farmerToOfficeConnectCodes")
        .document(code)
        .set(
            mapOf(
                "code" to code,
                "status" to "OPEN",
                "createdAt" to Timestamp.now(),
                "ttlMinutes" to 60L
                // vetId ABSENT
                ))
        .await()

    val res = repo.claimCode(code)
    assertTrue(res.isFailure)
    assertTrue(res.exceptionOrNull()!!.message!!.contains("Invalid office ID"))
  }
}
