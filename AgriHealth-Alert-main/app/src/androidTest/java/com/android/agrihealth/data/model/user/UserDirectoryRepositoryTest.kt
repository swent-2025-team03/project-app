package com.android.agrihealth.data.model.user

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDirectoryRepositoryTest {

  private lateinit var db: FirebaseFirestore
  private lateinit var users: CollectionReference
  private lateinit var docRef: DocumentReference
  private lateinit var repository: UserDirectoryRepository

  @Before
  fun setUp() {
    db = mockk(relaxed = true)
    users = mockk(relaxed = true)
    docRef = mockk(relaxed = true)

    every { db.collection("users") } returns users
    every { users.document(any()) } returns docRef

    repository = UserDirectoryRepository(db)
  }

  @Test
  fun getUserSummary_returnsData_whenUserExists() = runBlocking {
    val snap = mockk<DocumentSnapshot>()
    every { snap.exists() } returns true
    every { snap.getString("firstname") } returns "Jean"
    every { snap.getString("lastname") } returns "Dupont"
    every { snap.getString("role") } returns "FARMER"

    every { docRef.get() } returns Tasks.forResult(snap)

    val res = repository.getUserSummary("uid_1")

    assertNotNull(res)
    assertEquals("Jean", res!!.firstname)
    assertEquals("Dupont", res.lastname)
    assertEquals(UserRole.FARMER, res.role)

    verify(exactly = 1) { docRef.get() }

    val res2 = repository.getUserSummary("uid_1")
    assertNotNull(res2)
    verify(exactly = 1) { docRef.get() }
  }

  @Test
  fun getUserSummary_returnsNull_andCaches_whenUserDoesNotExist() = runBlocking {
    val snap = mockk<DocumentSnapshot>()
    every { snap.exists() } returns false

    every { docRef.get() } returns Tasks.forResult(snap)

    val res = repository.getUserSummary("uid_missing")
    assertNull(res)

    val res2 = repository.getUserSummary("uid_missing")
    assertNull(res2)
    verify(exactly = 2) { docRef.get() }
  }

  @Test
  fun getUserSummary_parsesUnknownRole_asNull() = runBlocking {
    val snap = mockk<DocumentSnapshot>()
    every { snap.exists() } returns true
    every { snap.getString("firstname") } returns "Alice"
    every { snap.getString("lastname") } returns "Martin"
    every { snap.getString("role") } returns "SOMETHING_ELSE"

    every { docRef.get() } returns Tasks.forResult(snap)

    val res = repository.getUserSummary("uid_2")

    assertNotNull(res)
    assertEquals("Alice", res!!.firstname)
    assertEquals("Martin", res.lastname)
    assertNull(res.role)
  }
}
