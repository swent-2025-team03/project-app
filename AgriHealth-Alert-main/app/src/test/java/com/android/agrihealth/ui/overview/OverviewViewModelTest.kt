package com.android.agrihealth.ui.overview

/*import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testutil.FakeAlertRepository
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
    val alertRepository = FakeAlertRepository()
    viewModel = OverviewViewModel(repository, alertRepository)
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

    viewModel.updateFiltersForReports(officeId = FilterArg.Value(officeId))
    advanceUntilIdle()

    val state = viewModel.uiState.value

    Assert.assertEquals(officeId, state.selectedOffice)
    Assert.assertTrue(state.filteredReports.all { it.officeId == officeId })
  }

  @Test
  fun `updateFiltersForReports applies assignment filter correctly`() = runTest {
    viewModel.loadReports(vet001)
    advanceUntilIdle()

    val currentVetId = vet001.uid

    // --- ASSIGNED_TO_CURRENT_VET ---
    viewModel.updateFiltersForReports(
        assignment = FilterArg.Value(AssignmentFilter.ASSIGNED_TO_CURRENT_VET))
    advanceUntilIdle()
    val assignedToMeReports = viewModel.uiState.value.filteredReports
    assert(assignedToMeReports.all { it.assignedVet == currentVetId })

    // --- UNASSIGNED ---
    viewModel.updateFiltersForReports(assignment = FilterArg.Value(AssignmentFilter.UNASSIGNED))
    advanceUntilIdle()
    val unassignedReports = viewModel.uiState.value.filteredReports
    assert(unassignedReports.all { it.assignedVet == null })

    // --- ASSIGNED_TO_OTHERS ---
    viewModel.updateFiltersForReports(
        assignment = FilterArg.Value(AssignmentFilter.ASSIGNED_TO_OTHERS))
    advanceUntilIdle()
    val assignedToOthersReports = viewModel.uiState.value.filteredReports
    assert(assignedToOthersReports.all { it.assignedVet != null && it.assignedVet != currentVetId })
  }

  @Test
  fun `updateFiltersForReports resets filters with FilterArg Reset`() = runTest {
    viewModel.loadReports(vet001)
    advanceUntilIdle()

    // Apply a filter first
    viewModel.updateFiltersForReports(
        status = FilterArg.Value(ReportStatus.SPAM), assignment = FilterArg.Unset)
    advanceUntilIdle()
    Assert.assertNotNull(viewModel.uiState.value.selectedStatus)

    // Reset filters
    viewModel.updateFiltersForReports(
        status = FilterArg.Reset,
        officeId = FilterArg.Reset,
        farmerId = FilterArg.Reset,
        assignment = FilterArg.Reset)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertNull(state.selectedStatus)
    Assert.assertNull(state.selectedOffice)
    Assert.assertNull(state.selectedFarmer)
    Assert.assertNull(state.selectedAssignmentFilter)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
}*/
