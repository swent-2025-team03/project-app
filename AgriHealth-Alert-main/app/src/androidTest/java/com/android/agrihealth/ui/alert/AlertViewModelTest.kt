package com.android.agrihealth.ui.alert

import com.android.agrihealth.data.model.alert.FakeAlertRepository
import com.android.agrihealth.ui.overview.AlertUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
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
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initial_state_isFirstAlert_ifExists() {
    val sortedAlerts = repository.allAlerts.map { AlertUiState(it) }
    viewModel = AlertViewModel(sortedAlerts, repository.allAlerts.first().id)

    val state = viewModel.uiState.value
    assertNotNull(state.alert)
    assertEquals(repository.allAlerts.first().id, state.alert!!.id)
    assertEquals(0, viewModel.currentAlertIndex.value)
  }

  @Test
  fun startAtMiddleAlert_initializesCorrectly() {
    val sortedAlerts = repository.allAlerts.map { AlertUiState(it) }
    val middleId = repository.allAlerts[1].id
    viewModel = AlertViewModel(sortedAlerts, middleId)

    val state = viewModel.uiState.value
    assertNotNull(state.alert)
    assertEquals(middleId, state.alert!!.id)
    assertEquals(1, viewModel.currentAlertIndex.value)
  }

  @Test
  fun loadNextAlert_movesToNext() {
    val sortedAlerts = repository.allAlerts.map { AlertUiState(it) }
    val firstId = repository.allAlerts[0].id
    viewModel = AlertViewModel(sortedAlerts, firstId)

    viewModel.loadNextAlert()

    val state = viewModel.uiState.value
    assertEquals(repository.allAlerts[1].id, state.alert!!.id)
    assertEquals(1, viewModel.currentAlertIndex.value)
  }

  @Test
  fun loadPreviousAlert_movesToPrevious() {
    val sortedAlerts = repository.allAlerts.map { AlertUiState(it) }
    val secondId = repository.allAlerts[1].id
    viewModel = AlertViewModel(sortedAlerts, secondId)

    viewModel.loadPreviousAlert()

    val state = viewModel.uiState.value
    assertEquals(repository.allAlerts[0].id, state.alert!!.id)
    assertEquals(0, viewModel.currentAlertIndex.value)
  }

  @Test
  fun hasNext_and_hasPrevious_areCorrect() {
    val sortedAlerts = repository.allAlerts.map { AlertUiState(it) }
    val first = repository.allAlerts.first().id
    val last = repository.allAlerts.last().id

    viewModel = AlertViewModel(sortedAlerts, first)
    assertFalse(viewModel.hasPrevious())
    assertTrue(viewModel.hasNext())

    viewModel = AlertViewModel(sortedAlerts, last)
    assertTrue(viewModel.hasPrevious())
    assertFalse(viewModel.hasNext())
  }

  @Test
  fun nextPrevious_doNothing_atBoundaries() {
    val sortedAlerts = repository.allAlerts.map { AlertUiState(it) }
    val firstId = repository.allAlerts[0].id
    viewModel = AlertViewModel(sortedAlerts, firstId)

    viewModel.loadPreviousAlert()
    assertEquals(firstId, viewModel.uiState.value.alert!!.id)

    val lastId = repository.allAlerts.last().id
    viewModel = AlertViewModel(sortedAlerts, lastId)
    viewModel.loadNextAlert()
    assertEquals(lastId, viewModel.uiState.value.alert!!.id)
  }
}
