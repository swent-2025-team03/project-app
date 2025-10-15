package com.android.agrihealth.ui.overview

import com.android.agrihealth.data.model.UserRole
import com.android.agrihealth.data.repository.FakeOverviewRepository
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

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(StandardTestDispatcher())
    repository = FakeOverviewRepository()
    viewModel = OverviewViewModel(repository)
  }

  @Test
  fun `uiState contains mock reports after init`() = runTest {
    val state = viewModel.uiState.value
    Assert.assertEquals(2, state.reports.size)
    Assert.assertEquals("RPT001", state.reports[0].id)
    Assert.assertEquals("RPT002", state.reports[1].id)
  }

  @Test
  fun `getReportsForUser returns farmer's own reports`() = runTest {
    val reports = viewModel.getReportsForUser(UserRole.FARMER, "FARMER_001")
    Assert.assertEquals(2, reports.size)
    Assert.assertTrue(reports.all { it.farmerId == "FARMER_001" })
  }

  @Test
  fun `getReportsForUser returns all reports for vet`() = runTest {
    val reports = viewModel.getReportsForUser(UserRole.VET, "VET_001")
    Assert.assertEquals(2, reports.size)
  }

  @Test
  fun `getReportsForUser returns empty for unknown role`() = runTest {
    val reports = viewModel.getReportsForUser(UserRole.AUTHORITY, "userX")
    Assert.assertTrue(reports.isEmpty())
  }

  @Test
  fun `getAllReports handles repository exception safely`() = runTest {
    val safeRepo = FakeOverviewRepository()
    val viewModel = OverviewViewModel(safeRepo)
    advanceUntilIdle()

    safeRepo.throwOnGet = true

    viewModel.javaClass.getDeclaredMethod("getAllReports").apply {
      isAccessible = true
      invoke(viewModel)
    }

    advanceUntilIdle()
    val state = viewModel.uiState.value
    Assert.assertTrue(state.reports.isEmpty())
  }

  @Test
  fun `init handles addReport exception safely`() = runTest {
    val repo =
        FakeOverviewRepository().apply {
          throwOnAddReport1 = true
          throwOnAddReport2 = true
        }
    val viewModel = OverviewViewModel(repo)
    advanceUntilIdle()
    // No crash = success
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
}
