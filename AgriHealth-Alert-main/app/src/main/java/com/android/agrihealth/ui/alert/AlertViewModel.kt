package com.android.agrihealth.ui.alert

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.alert.Alert
import com.android.agrihealth.data.model.alert.AlertRepository
import com.android.agrihealth.data.model.alert.AlertRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the UI state for viewing a single alert.
 *
 * @property alert The currently loaded `Alert`. Null if not loaded yet.
 */
data class AlertViewUIState(val alert: Alert? = null)

class AlertViewModel(private val repository: AlertRepository = AlertRepositoryProvider.repository) :
    ViewModel() {

  private val _uiState = MutableStateFlow(AlertViewUIState())
  val uiState: StateFlow<AlertViewUIState> = _uiState.asStateFlow()

  /** Loads an alert by its ID and updates the state. */
  fun loadAlert(alertId: String) {
    viewModelScope.launch {
      try {
        val fetchedAlert = repository.getAlertById(alertId)
        if (fetchedAlert != null) {
          _uiState.value = AlertViewUIState(alert = fetchedAlert)
        } else {
          Log.e("AlertViewModel", "Alert with ID $alertId not found.")
        }
      } catch (e: Exception) {
        Log.e("AlertViewModel", "Error loading Alert by ID: $alertId", e)
      }
    }
  }

  fun loadPreviousAlert(currentId: String) {
    val previous = repository.getPreviousAlert(currentId)
    previous?.let { loadAlert(it.id) }
  }

  fun loadNextAlert(currentId: String) {
    val next = repository.getNextAlert(currentId)
    next?.let { loadAlert(it.id) }
  }

  fun hasPrevious(currentId: String) = repository.getPreviousAlert(currentId) != null

  fun hasNext(currentId: String) = repository.getNextAlert(currentId) != null
}
