package com.android.agrihealth.zztemp.ui.office

import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.ui.office.ViewOfficeUiState
import com.android.agrihealth.ui.office.ViewOfficeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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

  override suspend fun getVetsInOffice(officeId: String): List<String> {
    return if (office == null || office.id != officeId) {
      emptyList()
    } else {
      office.vets
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
            targetOfficeId = "o1",
            officeRepository = FakeOfficeRepoForVM(office),
            dispatcher = testDispatcher
          )

        vm.load()
        advanceUntilIdle()

        val ui = vm.uiState.value
        assertTrue(ui is ViewOfficeUiState.Success)
        assertEquals("Test Office", (ui as ViewOfficeUiState.Success).office.name)
      }

  @Test
  fun load_missingOffice_updatesUiStateToError() =
      testScope.runTest {
        val vm =
            ViewOfficeViewModel(
                targetOfficeId = "missing_id",
                officeRepository = FakeOfficeRepoForVM(null),
                dispatcher = testDispatcher)

        vm.load()
        advanceUntilIdle()

        val ui = vm.uiState.value
        assertTrue(ui is ViewOfficeUiState.Error)
        assertEquals("Office does not exist.", (ui as ViewOfficeUiState.Error).message)
      }
}
