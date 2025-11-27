package com.android.agrihealth.ui.alert

import com.android.agrihealth.testutil.FakeAlertRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertViewModelTest {

  private lateinit var repository: FakeAlertRepository
  private lateinit var viewModel: AlertViewModel
  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(dispatcher)
    repository = FakeAlertRepository()
    viewModel = AlertViewModel(repository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initial_state_isEmpty() {
    val state = viewModel.uiState.value
    assertNull(state.alert)
    assertEquals(0, viewModel.currentAlertIndex.value)
  }

  @Test
  fun loadAlert_updatesUiState_andIndex() =
      runTest(dispatcher) {
        val targetId = repository.allAlerts[1].id

        viewModel.loadAlert(targetId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.alert)
        assertEquals(targetId, state.alert!!.id)
        assertEquals(1, viewModel.currentAlertIndex.value)
      }

  @Test
  fun loadAlert_withInvalidId_keepsStateUnchanged() =
      runTest(dispatcher) {
        viewModel.loadAlert("invalid-id")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.alert)
        assertEquals(0, viewModel.currentAlertIndex.value)
      }

  @Test
  fun loadNextAlert_movesToNext() =
      runTest(dispatcher) {
        val firstId = repository.allAlerts[0].id
        viewModel.loadAlert(firstId)
        advanceUntilIdle()

        viewModel.loadNextAlert(firstId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(repository.allAlerts[1].id, state.alert!!.id)
        assertEquals(1, viewModel.currentAlertIndex.value)
      }

  @Test
  fun loadPreviousAlert_movesToPrevious() =
      runTest(dispatcher) {
        val secondId = repository.allAlerts[1].id

        viewModel.loadAlert(secondId)
        advanceUntilIdle()

        viewModel.loadPreviousAlert(secondId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(repository.allAlerts[0].id, state.alert!!.id)
        assertEquals(0, viewModel.currentAlertIndex.value)
      }

  @Test
  fun hasNext_and_hasPrevious_areCorrect() {
    val first = repository.allAlerts.first().id
    val last = repository.allAlerts.last().id

    assertFalse(viewModel.hasPrevious(first))
    assertTrue(viewModel.hasNext(first))

    assertTrue(viewModel.hasPrevious(last))
    assertFalse(viewModel.hasNext(last))
  }

  @Test
  fun nextPrevious_doNothing_atBoundaries() =
      runTest(dispatcher) {
        // First alert
        val firstId = repository.allAlerts[0].id
        viewModel.loadAlert(firstId)
        advanceUntilIdle()

        viewModel.loadPreviousAlert(firstId)
        advanceUntilIdle()

        assertEquals(firstId, viewModel.uiState.value.alert!!.id)

        // Last alert
        val lastId = repository.allAlerts.last().id
        viewModel.loadAlert(lastId)
        advanceUntilIdle()

        viewModel.loadNextAlert(lastId)
        advanceUntilIdle()

        assertEquals(lastId, viewModel.uiState.value.alert!!.id)
      }
}
