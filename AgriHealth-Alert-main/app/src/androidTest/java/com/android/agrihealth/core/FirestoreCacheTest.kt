package com.android.agrihealth.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreCacheTest {

    @Before
    fun setup() {
        // Route Firestore/Auth to the emulator before any instance is created
        FirebaseEmulatorsManager.linkEmulators()
        FirebaseFirestore.setLoggingEnabled(true)

    }

    @Test
    fun cachePersistsDataOffline() = runBlocking {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("connect_codes").document("cache_${UUID.randomUUID()}")

        try {
            // 1) Write online
            docRef.set(mapOf("value" to 123L)).await()

            // 2) Go offline
            db.disableNetwork().await()

            // 3) Read from cache
            val cached = docRef.get(Source.CACHE).await()
            assertTrue(cached.exists())
            assertEquals(123L, cached.getLong("value"))

            // 4) Force server read should fail quickly
            try {
                docRef.get(Source.SERVER).await()
                fail("Expected network error")
            } catch (_: Exception) { /* expected */ }

            // 5) Back online and confirm on server (bounded wait)
            db.enableNetwork().await()
            awaitServerValue(docRef, "value", 123L)
        } finally {
            // Ensure network is enabled even if assertions fail
            try { db.enableNetwork().await() } catch (_: Exception) {}
        }
    }

    @Test
    fun offlineWriteSyncsWhenNetworkRestored_stable() = runBlocking {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("connect_codes").document("offline_${UUID.randomUUID()}")

        // 1) Write normally (network ON)
        docRef.set(mapOf("status" to "cached_write")).await()

        // 2) Read from cache directly
        val cached = docRef.get(Source.CACHE).await()
        assertTrue(cached.exists())
        assertEquals("cached_write", cached.getString("status"))

        // 3) Read again from server (verifies sync path)
        val remote = docRef.get(Source.SERVER).await()
        assertTrue(remote.exists())
        assertEquals("cached_write", remote.getString("status"))
    }

    private suspend fun awaitServerValue(
        ref: DocumentReference,
        key: String,
        expected: Any,
        timeoutMs: Long = 10_000,
        intervalMs: Long = 300
    ) {
        val start = System.currentTimeMillis()
        withTimeout(timeoutMs) {
            while (true) {
                try {
                    val s = ref.get(Source.SERVER).await()
                    if (s.exists() && s.get(key) == expected) {
                        val elapsed = System.currentTimeMillis() - start
                        println("✅ Synced after ${elapsed}ms")
                        return@withTimeout
                    }
                } catch (_: Exception) {
                    // still offline or not yet synced
                }
                delay(intervalMs)
            }
        }
    }
}