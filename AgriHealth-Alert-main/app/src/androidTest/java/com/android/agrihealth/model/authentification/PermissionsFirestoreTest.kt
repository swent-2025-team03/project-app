package com.android.agrihealth.model.authentification;

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.test.runTest
import org.junit.Before;
import org.junit.Test;

class PermissionsFirestoreTest {
    @Before
    fun setUp() {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        Firebase.auth.useEmulator("10.0.2.2", 9099)
    }

    @Test
    fun canAccessOwnData() {

    }

    @Test
    fun failToAccessOtherUsersData() {

    }

    @Test
    fun failToAccessDataWhileLoggedOut() {

    }

    @Test
    fun failToUpdateOwnRole() {

    }

    @Test
    fun failToDeleteWhileLoggedOut() = runTest {

    }
}
