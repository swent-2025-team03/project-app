package com.android.agrihealth.ui.map

import androidx.lifecycle.ViewModel
import com.android.agrihealth.data.model.Location
import com.android.agrihealth.data.model.Report
import com.android.agrihealth.data.model.ReportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapUIState(
    val reports: List<Report> = emptyList(),
)

class MapViewModel : ViewModel() {
  // Placeholder reports for testing
  val report1 =
      Report(
          "rep_id1",
          "Report title 1",
          "Description 1",
          null,
          "farmerId1",
          null,
          ReportStatus.PENDING,
          null,
          Location(46.5200948, 6.5651742, "Place name 1"))
  val report2 =
      Report(
          "rep_id2",
          "Report title 2",
          "Description aaaa 2",
          null,
          "farmerId2",
          "vetId2",
          ReportStatus.IN_PROGRESS,
          "Vet answer",
          Location(46.5183104, 6.5676777))
  val report3 =
      Report(
          "rep_id3",
          "Report title 3",
          "Description 3",
          null,
          "farmerId3",
          null,
          ReportStatus.RESOLVED,
          null,
          Location(46.5206231, 6.569927, "Place name 3"))
  val report4 =
      Report(
          "rep_id4",
          "Report title 4",
          "Description aaaa 4",
          null,
          "farmerId4",
          "vetId4",
          ReportStatus.ESCALATED,
          "Vet answer 4",
          Location(46.5232, 6.5681191))

  private val _uiState = MutableStateFlow(MapUIState(listOf(report1, report2, report3, report4)))
  val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()
}
