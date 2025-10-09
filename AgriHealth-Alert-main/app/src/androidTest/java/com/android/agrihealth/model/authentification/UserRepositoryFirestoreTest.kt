package com.android.agrihealth.model.authentification

import com.android.agrihealth.data.model.authentification.USERS_COLLECTION_PATH
import com.android.agrihealth.data.model.authentification.User
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.authentification.UserRole
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class UserRepositoryFirestoreTest {

    val userRepository = UserRepositoryProvider.repository

    val user1 = User(
        "abc123",
        "Rushia",
        "Uruha",
        UserRole.FARMER,
        "email1@thing.com"
    )
    val user2 = User(
        "def456",
        "mike",
        "neko",
        UserRole.FARMER,
        "email2@aaaaa.balls"
    )
    val user3 = User(
        "ghj789",
        "Nazuna",
        "Amemiya",
        UserRole.VETERINARIAN,
        "email3@kms.josh"
    )

    private suspend fun clearUsers() {
        val usersCollection = Firebase.firestore
            .collection(USERS_COLLECTION_PATH)
            .get()
            .await()

        if (usersCollection.count() > 0) {
            val batch = Firebase.firestore.batch()
            usersCollection.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    @Before
    fun setUp() {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        runTest {
            clearUsers()
        }
    }

    @Test
    fun canGetCorrectUserData() = runTest {
        userRepository.addUser(user1)
        val userData = userRepository.getUserFromId(user1.uid).getOrThrow()
        assertEquals(user1.uid, userData.uid)
        assertEquals(user1.name, userData.name)
        assertEquals(user1.surname, userData.surname)
        assertEquals(user1.role, userData.role)
        assertEquals(user1.email, userData.email)
    }

    @Test
    fun canUpdateSingleField() = runTest {
        userRepository.addUser(user1)
        val newEmail = "newemail@thing.com"
        userRepository.updateUser(user1.copy(email = newEmail))
        val userData = userRepository.getUserFromId(user1.uid).getOrThrow()
        assertEquals(userData.email, newEmail)
    }

    @Test
    fun canUpdateMultipleFields() = runTest {
        userRepository.addUser(user1)
        val newEmail = "newemail@thing.com"
        val newName = "newName"
        userRepository.updateUser(user1.copy(email = newEmail, name = newName))
        val userData = userRepository.getUserFromId(user1.uid).getOrThrow()
        assertEquals(userData.email, newEmail)
        assertEquals(userData.name, newName)
    }

    @Test
    fun canDeleteAccountData() = runTest {
        userRepository.addUser(user1)
        val userData = userRepository.getUserFromId(user1.uid).getOrThrow()
        assertNotNull(userData)
        userRepository.deleteUser(user1.uid)
        val result = userRepository.getUserFromId(user1.uid).exceptionOrNull()
        assertNotNull(result) // not a success
        assertTrue(result is NullPointerException)
        assertEquals(result?.message, "No such user found")
    }

    @Test
    fun failToUpdateNonExistingAccount() = runTest {
        try {
            userRepository.updateUser(user1)
            fail("Expected FirebaseFirestoreException, but code ran fine")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(e.code, FirebaseFirestoreException.Code.NOT_FOUND)
        }
    }


}