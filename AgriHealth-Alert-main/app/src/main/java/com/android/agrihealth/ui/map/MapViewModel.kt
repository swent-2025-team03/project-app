package com.android.agrihealth.ui.map

import androidx.lifecycle.ViewModel
import com.android.agrihealth.data.model.Report
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapUIState(
    val reports: List<Report> = emptyList(),
)

class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MapUIState())
    val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()
}