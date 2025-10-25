package com.android.agrihealth.data.connection

import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.tasks.await
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import com.android.agrihealth.data.model.connection.ConnectionRepository

class ConnectionRepositoryTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun beforeAll() {
            FirebaseApp.clearInstancesForTest()

            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val options = FirebaseOptions.Builder()
                .setProjectId("test-project")
                .setApplicationId("1:android:1")
                .setApiKey("fake-api-key")
                .build()
            FirebaseApp.initializeApp(context, options)

            // IMPORTANT: configure emulator on the instance before any Firestore usage
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8081)
        }

        @JvmStatic
        @AfterClass
        fun afterAll() {
            // Cleanup after all tests
            FirebaseApp.clearInstancesForTest()
        }
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var repo: ConnectionRepository

    @Before
    fun setup() = runBlocking {
        db = FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
        }
        repo = ConnectionRepository(db)
        clearCollections()
    }

    private suspend fun clearCollections() {
        listOf("connect_codes", "connections").forEach { col ->
            val snap = db.collection(col).get().await()
            snap.documents
                .map { it.reference.delete() }
                .let { tasks -> tasks.map { it.await() } }
        }
    }

    @Test
    fun generateCode_createsOpenDoc() = runTest {
        val vetId = "vetA"
        val code = repo.generateCode(vetId).getOrThrow()
        assertTrue(code.matches(Regex("\\d{6}")))

        val snap = db.collection("connect_codes").document(code).get().await()
        assertTrue(snap.exists())
        assertEquals("OPEN", snap.getString("status"))
        assertEquals(vetId, snap.getString("vetId"))
        assertNotNull(snap.getLong("expiresAtMs"))
        assertNotNull(snap.getTimestamp("createdAt"))
    }

    @Test
    fun claimCode_linksAndMarksUsed() = runTest {
        val vetId = "vetB"
        val farmerId = "farmer1"
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
        val vetId = "vetC"
        val farmerId = "farmer2"
        val code = repo.generateCode(vetId, ttlMinutes = 0).getOrThrow() // immediate expiry
        val res = repo.claimCode(code, farmerId)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull()!!.message!!.lowercase().contains("expired"))
    }

    @Test
    fun claimCode_failsWhenUsed() = runTest {
        val vetId = "vetD"
        val farmerId = "farmer3"
        val code = repo.generateCode(vetId).getOrThrow()
        db.collection("connect_codes").document(code).update("status", "USED").await()

        val res = repo.claimCode(code, farmerId)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull()!!.message!!.lowercase().contains("used"))
    }

    @Test
    fun claimCode_failsWhenUnknown() = runTest {
        val res = repo.claimCode("999999", "farmerX")
        assertTrue(res.isFailure)
    }

    @Test
    fun generateCode_many_areUnique() = runTest {
        val vet = "vetU"
        val codes = (1..200).map { repo.generateCode(vet).getOrThrow() }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun claimCode_raceTwoFarmers_oneSucceeds() = runTest {
        val vet = "vetZ"; val f1 = "farmer1"; val f2 = "farmer2"
        val code = repo.generateCode(vet).getOrThrow()
        val r1 = async { repo.claimCode(code, f1) }
        val r2 = async { repo.claimCode(code, f2) }
        val results = awaitAll(r1, r2)
        assertEquals(1, results.count { it.isSuccess })
        assertEquals(1, results.count { it.isFailure })
    }

    @Test
    fun claimCode_succeedsRightBeforeExpiry() = runTest {
        val vet = "vetT"; val farmer = "farmerT"
        val code = repo.generateCode(vet, ttlMinutes = 1).getOrThrow()
        // No need to delay: just check it succeeds before expiration
        assertTrue(repo.claimCode(code, farmer).isSuccess)
    }

    @Test
    fun connectionId_isSymmetric_singleDoc() = runTest {
        val vet="vetS"; val farmer="farmerS"
        val code = repo.generateCode(vet).getOrThrow()
        repo.claimCode(code, farmer).getOrThrow()
        val id = listOf(vet, farmer).sorted().joinToString("__")
        assertTrue(db.collection("connections").document(id).get().await().exists())
    }

    // TODO add a test for double click generateCode after implementing the UI
}
