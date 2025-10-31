package com.android.agrihealth.ui.map

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.data.repository.ReportRepositoryProvider
import com.android.agrihealth.ui.user.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUIState(
    val reports: List<Report> = emptyList(),
)

class MapViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository,
    private val userViewModel: UserViewModel = UserViewModel()
) : ViewModel() {
  private val _uiState = MutableStateFlow(MapUIState())
  val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

  init {
    fetchLocalizableReports()
  }

  private fun fetchLocalizableReports() {
    viewModelScope.launch {
      try {
        val userId = userViewModel.user.value.uid
        val reports = reportRepository.getAllReports(userId).filter { it.location != null }
        _uiState.value = MapUIState(reports = reports)
      } catch (e: Exception) {
        Log.w("MapScreen", "Failed to load todos: ${e.message}")
      }
    }
  }
}
