package com.android.agrihealth.data.model.connection

import com.android.agrihealth.core.constants.FirestoreSchema.Collections.CONNECT_CODES
import com.android.agrihealth.core.constants.FirestoreSchema.Collections.FARMER_TO_OFFICE
import com.android.agrihealth.core.constants.FirestoreSchema.Collections.VET_TO_OFFICE
import com.android.agrihealth.data.model.authentification.AuthRepositoryFirebase
import com.android.agrihealth.data.model.office.OfficeRepositoryProvider
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testhelpers.TestPassword.password1
import com.android.agrihealth.testhelpers.TestPassword.password2
import com.android.agrihealth.testhelpers.TestPassword.password3
import com.android.agrihealth.testhelpers.TestUser.farmer1
import com.android.agrihealth.testhelpers.TestUser.farmer2
import com.android.agrihealth.testhelpers.TestUser.office1
import com.android.agrihealth.testhelpers.TestUser.vet1
import com.android.agrihealth.testhelpers.templates.FirebaseTest
import com.android.agrihealth.ui.profile.CodeType
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
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
class ConnectionRepositoryTest : FirebaseTest() {

  private lateinit var db: FirebaseFirestore
  private lateinit var repo: ConnectionRepository

  val authRepository = AuthRepositoryFirebase()

  /**
   * Test setup for ConnectionRepository integration tests.
   * - Calls super.setUp() to initialize Firebase emulator and base test logic.
   * - Initializes Firestore and the repository instance.
   * - Creates three test users (farmer1, farmer2, vet1) in Firebase for use as vet and farmers in
   *   all tests. This ensures all tests run with a clean, known state and valid user accounts.
   */
  @Before
  fun setUp() = runBlocking {
    // Initialize Firestore and repository, and create test users for vet and farmers.
    db = FirebaseFirestore.getInstance()
    repo = ConnectionRepository(db, connectionType = FARMER_TO_OFFICE)
    runTest {
      authRepository.signUpWithEmailAndPassword(farmer1.email, password1, farmer1)
      authRepository.signOut()
      authRepository.signUpWithEmailAndPassword(farmer2.email, password2, farmer2)
      authRepository.signOut()
      authRepository.signUpWithEmailAndPassword(vet1.email, password3, vet1)
      OfficeRepositoryProvider.get().addOffice(office1.copy(ownerId = Firebase.auth.uid!!))
    }
  }

  @Test
  // Verifies that generateCode creates a 6-digit code and writes an OPEN document with expected
  // fields.
  fun generateCode_createsOpenDoc() = runTest {
    val officeId = vet1.officeId!!
    val code = repo.generateCode().getOrThrow()
    assertTrue(code.matches(Regex("\\d{6}")))

    val snap = db.collection(FARMER_TO_OFFICE + CONNECT_CODES).document(code).get().await()
    assertTrue(snap.exists())
    assertEquals("OPEN", snap.getString("status"))
    assertEquals(officeId, snap.getString("officeId"))
    assertNotNull(snap.getTimestamp("createdAt"))
  }

  @Test
  fun generateCodeForOfficeWorks() = runTest {
    repo = ConnectionRepository(db, connectionType = VET_TO_OFFICE)
    val officeId = vet1.officeId!!
    val code = repo.generateCode().getOrThrow()
    assertTrue(code.matches(Regex("\\d{6}")))

    val snap = db.collection(VET_TO_OFFICE + CONNECT_CODES).document(code).get().await()
    assertTrue(snap.exists())
    assertEquals("OPEN", snap.getString("status"))
    assertEquals(officeId, snap.getString("officeId"))
    assertNotNull(snap.getTimestamp("createdAt"))
  }

  @Test
  // Verifies that a farmer can claim a code, the repository returns officeId and code is marked
  // USED.
  fun claimCodeAndMarksUsed() = runTest {
    val officeId = vet1.officeId!!
    val code = repo.generateCode().getOrThrow()

    authRepository.signOut()
    authRepository.signInWithEmailAndPassword(farmer1.email, password1)

    val returnedVet = repo.claimCode(code).getOrThrow()
    assertEquals(officeId, returnedVet)

    val codeDoc = db.collection(FARMER_TO_OFFICE + CONNECT_CODES).document(code).get().await()
    assertEquals("USED", codeDoc.getString("status"))
  }

  @Test
  // Verifies that claimCode fails when the code has already been used.
  fun claimCode_failsWhenUsed() = runTest {
    val code = repo.generateCode().getOrThrow()

    authRepository.signOut()
    authRepository.signInWithEmailAndPassword(farmer1.email, password1)

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
      authRepository.signInWithEmailAndPassword(farmer1.email, password1)
      repo.claimCode(code)
    }
    val r2 = async {
      authRepository.signOut()
      authRepository.signInWithEmailAndPassword(farmer2.email, password2)
      repo.claimCode(code)
    }
    val results = awaitAll(r1, r2)
    assertEquals(1, results.count { it.isSuccess })
  }

  @Test
  // Verifies that generateCode retries if a collision occurs on the first generated code.
  fun generateCode_collidesOnce_thenSucceeds() = runTest {
    val taken = "123456"
    db.collection(FARMER_TO_OFFICE + CONNECT_CODES).document(taken).set(mapOf("any" to "x")).await()

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
    db.collection(FARMER_TO_OFFICE + CONNECT_CODES)
        .document(code)
        .set(
            mapOf(
                "code" to code, "officeId" to vet1.uid, "status" to "OPEN"
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
  // Verifies that claimCode fails when officeId is missing in the code document.
  fun claimCode_fails_whenMissingOfficeId() = runTest {
    val code = "333444"
    db.collection(FARMER_TO_OFFICE + CONNECT_CODES)
        .document(code)
        .set(
            mapOf(
                "code" to code, "status" to "OPEN", "createdAt" to Timestamp.now()
                // officeId MISSING
                ))
        .await()

    val res = repo.claimCode(code)
    assertTrue(res.isFailure)
    assertTrue(res.exceptionOrNull()!!.message!!.contains("Invalid office ID"))
  }

  @Test
  // Verifies that getValidCodes returns only the user's unused (OPEN) active codes.
  fun getValidCodes_returnsOnlyActiveCodes() = runTest {
    val vet =
        Vet(
            uid = "ato",
            firstname = "nishuukann",
            lastname = "samishiina",
            email = "koukannryuugaku@de.com",
            address = null,
            farmerConnectCodes = listOf("111111", "222222", "333333"),
            vetConnectCodes = listOf("444444"),
            officeId = "epflwoerannde",
            isGoogleAccount = false,
            description = null,
            collected = false,
            deviceTokensFCM = setOf("yokattana"))

    // Prepare Firestore docs: 111111 (OPEN), 222222 (USED), 333333 (OPEN)
    val coll = FARMER_TO_OFFICE + CONNECT_CODES

    db.collection(coll)
        .document("111111")
        .set(
            mapOf(
                "code" to "111111",
                "officeId" to vet1.officeId!!,
                "status" to "OPEN",
                "createdAt" to Timestamp.now()))
        .await()

    db.collection(coll)
        .document("222222")
        .set(
            mapOf(
                "code" to "222222",
                "officeId" to vet1.officeId!!,
                "status" to "USED",
                "createdAt" to Timestamp.now()))
        .await()

    db.collection(coll)
        .document("333333")
        .set(
            mapOf(
                "code" to "333333",
                "officeId" to vet1.officeId!!,
                "status" to "OPEN",
                "createdAt" to Timestamp.now()))
        .await()

    val result = repo.getValidCodes(vet, CodeType.FARMER)

    assertEquals(listOf("111111", "333333"), result.sorted())
  }
}
