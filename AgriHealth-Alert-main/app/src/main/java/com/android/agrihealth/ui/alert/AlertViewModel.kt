package com.android.agrihealth.ui.alert

import androidx.lifecycle.ViewModel
import com.android.agrihealth.data.model.alert.Alert
import com.android.agrihealth.ui.overview.AlertUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** UI State to show alert information */
data class AlertViewUIState(val alert: Alert? = null, val errorMessage: String? = null)

/**
 * ViewModel for alert navigation. Tracks current index and exposes the selected alert via
 * [uiState].
 */
class AlertViewModel(private val sortedAlerts: List<AlertUiState>, startAlertId: String) :
    ViewModel() {

  private val _currentAlertIndex =
      MutableStateFlow(sortedAlerts.indexOfFirst { it.alert.id == startAlertId }.coerceAtLeast(0))
  val currentAlertIndex: StateFlow<Int> = _currentAlertIndex.asStateFlow()

  private val _uiState =
      MutableStateFlow(
          AlertViewUIState(alert = sortedAlerts.getOrNull(_currentAlertIndex.value)?.alert))
  val uiState: StateFlow<AlertViewUIState> = _uiState.asStateFlow()

  fun loadPreviousAlert() {
    if (_currentAlertIndex.value > 0) {
      _currentAlertIndex.value -= 1
      _uiState.value = AlertViewUIState(alert = sortedAlerts[_currentAlertIndex.value].alert)
    }
  }

  fun loadNextAlert() {
    if (_currentAlertIndex.value < sortedAlerts.size - 1) {
      _currentAlertIndex.value += 1
      _uiState.value = AlertViewUIState(alert = sortedAlerts[_currentAlertIndex.value].alert)
    }
  }

  fun hasPrevious() = _currentAlertIndex.value > 0

  fun hasNext() = _currentAlertIndex.value < sortedAlerts.size - 1
}
