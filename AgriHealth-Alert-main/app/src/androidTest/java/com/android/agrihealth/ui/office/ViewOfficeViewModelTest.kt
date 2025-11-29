package com.android.agrihealth.ui.office

import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Fake repository for testing the ViewModel. */
class FakeOfficeRepoForVM(private val office: Office? = null) : OfficeRepository {

  override fun getNewUid(): String = "new"

  override suspend fun addOffice(office: Office) {}

  override suspend fun updateOffice(office: Office) {}

  override suspend fun deleteOffice(id: String) {}

  override suspend fun getOffice(id: String): Result<Office> {
    return if (office == null || office.id != id) {
      Result.failure(NullPointerException("Office not found"))
    } else {
      Result.success(office)
    }
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ViewOfficeViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @Test
  fun load_success_updatesUiStateToSuccess() =
      testScope.runTest {
        val office =
            Office(
                id = "o1",
                name = "Test Office",
                address = null,
                description = "A test description",
                vets = listOf("vet1"),
                ownerId = "owner")
        val vm =
            ViewOfficeViewModel(
                targetOfficeId = "o1", officeRepository = FakeOfficeRepoForVM(office))

        vm.load() // Launches coroutine
        testScheduler.advanceUntilIdle() // Wait for coroutine to finish

        val ui = vm.uiState.value
        assertTrue(ui is ViewOfficeUiState.Success)
        assertEquals("Test Office", (ui as ViewOfficeUiState.Success).office.name)
      }
}
