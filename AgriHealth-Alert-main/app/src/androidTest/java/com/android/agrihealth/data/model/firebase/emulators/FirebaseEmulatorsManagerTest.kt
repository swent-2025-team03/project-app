package com.android.agrihealth.data.model.firebase.emulators

import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsManager.isLocalRunning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FirebaseEmulatorsManagerTest {
    @Test
    fun hostMatchesLocalIfRunning() {
        FirebaseEmulatorsManager.linkEmulators()

        val environment = FirebaseEmulatorsManager.environment
        val localhost = "10.0.2.2"

        if (isLocalRunning(environment.firestorePort)) assertEquals(environment.host, localhost)
        else assertNotEquals(environment.host, localhost)
    }
}