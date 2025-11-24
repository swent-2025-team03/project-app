package com.android.agrihealth.ui.planner

import androidx.lifecycle.ViewModel
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.data.repository.ReportRepositoryProvider
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.IsoFields
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlannerUIState(
    val reports: List<Report> = emptyList(),

    // Today info
    val now: LocalDateTime = LocalDateTime.now(),
    val today: LocalDate = LocalDate.now(),
    val currentTime: LocalTime = LocalTime.now(),

    // Selected date info
    val selectedDate: LocalDate = today,
    val selectedWeek: List<LocalDate> =
        (0..6).map { selectedDate.with(DayOfWeek.MONDAY).plusDays(it.toLong()) },
    val selectedWeekNumber: Int = selectedDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
)

class PlannerViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(PlannerUIState())
  val uiState: StateFlow<PlannerUIState> = _uiState.asStateFlow()

  fun setSelectedDate(date: LocalDate) {
    val week: List<LocalDate> = (0..6).map { date.with(DayOfWeek.MONDAY).plusDays(it.toLong()) }
    val weekNumber = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

    _uiState.value =
        _uiState.value.copy(
            selectedDate = date, selectedWeek = week, selectedWeekNumber = weekNumber)
  }
}
