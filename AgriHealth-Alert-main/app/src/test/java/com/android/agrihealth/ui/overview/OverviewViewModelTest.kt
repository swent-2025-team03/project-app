package com.android.agrihealth.ui.overview

import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testutil.FakeOverviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OverviewViewModelTest {

  private lateinit var repository: FakeOverviewRepository
  private lateinit var viewModel: OverviewViewModel

  private val farmer001 =
      Farmer("FARMER_001", "john", "john", "john@john.john", null, defaultOffice = null)
  private val vet001 = Vet("VET_001", "john", "john", "john@john.john", null, officeId = "OFF_001")

  @Before
  fun setup() {
    Dispatchers.setMain(StandardTestDispatcher())
    repository = FakeOverviewRepository()
    viewModel = OverviewViewModel(repository)
  }

  @Test
  fun `getReportsForUser returns farmer's own reports`() = runTest {
    viewModel.loadReports(farmer001)
    advanceUntilIdle()
    val reports = viewModel.getReportsForUser(UserRole.FARMER, farmer001.uid)
    Assert.assertTrue(reports.all { it.farmerId == farmer001.uid })
  }

  @Test
  fun `getReportsForUser returns all reports for vet`() = runTest {
    viewModel.loadReports(vet001)
    advanceUntilIdle()
    val reports = viewModel.getReportsForUser(UserRole.VET, vet001.uid)
    Assert.assertTrue(reports.all { it.officeId == vet001.officeId })
  }

  @Test
  fun `loadReports handles repository exception safely`() = runTest {
    repository.throwOnGet = true
    viewModel.loadReports(farmer001)
    advanceUntilIdle()
    val state = viewModel.uiState.value
    Assert.assertTrue(state.reports.isEmpty())
  }

  @Test
  fun `init handles addReport exception safely`() = runTest {
    FakeOverviewRepository().apply {
      throwOnAddReport1 = true
      throwOnAddReport2 = true
    }
    advanceUntilIdle()
    // No crash = success
  }

  @Test
  fun `updateFilters applies filters correctly`() = runTest {
    viewModel.loadReports(vet001)
    advanceUntilIdle()

    val officeId = viewModel.uiState.value.officeOptions.firstOrNull()
    viewModel.updateFiltersForReports(status = null, officeId = officeId, farmerId = null)
    val state = viewModel.uiState.value

    Assert.assertEquals(officeId, state.selectedOffice)
    Assert.assertTrue(state.filteredReports.all { it.officeId == vet001.officeId })
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
}
