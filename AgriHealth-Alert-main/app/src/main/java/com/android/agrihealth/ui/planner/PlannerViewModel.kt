package com.android.agrihealth.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportRepository
import com.android.agrihealth.data.model.report.ReportRepositoryProvider
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.ui.loading.withLoadingState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.IsoFields
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlannerUIState(
    val user: User =
        Farmer(
            uid = "",
            firstname = "",
            lastname = "",
            email = "",
            address = null,
            linkedOffices = emptyList(),
            defaultOffice = "",
            isGoogleAccount = false,
            description = ""),
    val reports: Map<LocalDate?, List<Report>> = emptyMap(),
    val selectedDateReports: List<Report> = emptyList(),
    val originalDate: LocalDate = LocalDate.now(),

    // Selected date info
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedWeek: List<LocalDate> =
        (0..6).map { selectedDate.with(DayOfWeek.MONDAY).plusDays(it.toLong()) },
    val selectedWeekNumber: Int = selectedDate[IsoFields.WEEK_OF_WEEK_BASED_YEAR],

    // Report for which we are setting the date
    val setTime: LocalTime = LocalTime.now(),
    val setDuration: LocalTime = LocalTime.of(1, 0),
    val reportToSetTheDateFor: Report? = null,
    val isAllowedToSetDate: Boolean = false,

    // Unsaved Changes
    val isUnsavedAlertShowing: Boolean = false,
    val isLoading: Boolean = false
)

class PlannerViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(PlannerUIState())
  val uiState: StateFlow<PlannerUIState> = _uiState.asStateFlow()

  suspend fun loadReports() {
    _uiState.withLoadingState(applyLoading = { s, loading -> s.copy(isLoading = loading) }) {
      try {
        val user = _uiState.value.user
        val reports =
            reportRepository.getAllReports(user.uid).groupBy { it.startTime?.toLocalDate() }
        _uiState.value =
            _uiState.value.copy(
                reports = reports,
                selectedDateReports = reports[_uiState.value.selectedDate] ?: emptyList())
      } catch (_: Exception) {
        _uiState.value = _uiState.value.copy(reports = emptyMap())
      }
    }
  }

  fun editReportWithNewTime() {
    val selectedDate = _uiState.value.selectedDate
    val newDateTime = LocalDateTime.of(selectedDate, _uiState.value.setTime)
    viewModelScope.launch {
      _uiState.withLoadingState(applyLoading = { s, loading -> s.copy(isLoading = loading) }) {
        reportRepository.editReport(
            _uiState.value.reportToSetTheDateFor!!.id,
            _uiState.value.reportToSetTheDateFor!!.copy(
                startTime = newDateTime, duration = _uiState.value.setDuration))
        loadReports()
        setReportToSetTheDateFor(_uiState.value.reportToSetTheDateFor!!.id)
      }
    }
  }

  fun isReportDateSet(): Boolean {
    return _uiState.value.reportToSetTheDateFor == null ||
        _uiState.value.reportToSetTheDateFor?.startTime != null
  }

  fun setIsUnsavedAlertShowing(value: Boolean) {
    _uiState.value = _uiState.value.copy(isUnsavedAlertShowing = value)
  }

  fun setSelectedDate(date: LocalDate) {
    val week: List<LocalDate> = (0..6).map { date.with(DayOfWeek.MONDAY).plusDays(it.toLong()) }
    val weekNumber = date[IsoFields.WEEK_OF_WEEK_BASED_YEAR]

    _uiState.value =
        _uiState.value.copy(
            selectedDate = date,
            selectedWeek = week,
            selectedWeekNumber = weekNumber,
            selectedDateReports = _uiState.value.reports[date] ?: emptyList())
  }

  fun setOriginalDate(date: LocalDate) {
    _uiState.value = _uiState.value.copy(originalDate = date)
  }

  fun setReportTime(time: LocalTime) {
    _uiState.value = _uiState.value.copy(setTime = time)
  }

  fun setReportDuration(time: LocalTime) {
    _uiState.value = _uiState.value.copy(setDuration = time)
  }

  fun setUser(user: User) {
    _uiState.value = _uiState.value.copy(user = user)
  }

  fun setReportToSetTheDateFor(reportId: String?): Report? {
    if (reportId == null) {
      _uiState.value = _uiState.value.copy(reportToSetTheDateFor = null)
      return null
    }
    val report = _uiState.value.reports.values.flatten().find { it.id == reportId }
    if (report?.assignedVet == _uiState.value.user.uid)
        _uiState.value = _uiState.value.copy(isAllowedToSetDate = true)

    _uiState.value = _uiState.value.copy(reportToSetTheDateFor = report)
    return report
  }
}
