package com.android.sample.ui.overview

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.android.sample.data.model.*

/**
 * ViewModel holding the state of reports displayed on the Overview screen.
 * Currently uses mock data only. Repository integration will be added later.
 */
class OverviewViewModel : ViewModel() {

    // ---- Mock reports list ----
    private val _reports = mutableStateOf(
        listOf(
            Report(
                id = "RPT001",
                title = "Cow coughing",
                description = "Coughing and nasal discharge observed in the barn.",
                photoUri = null,
                farmerId = "FARMER_001",
                vetId = null,
                status = ReportStatus.IN_PROGRESS,
                answer = null,
                location = Location(46.5191, 6.5668, "Lausanne Farm")
            ),
            Report(
                id = "RPT002",
                title = "Sheep lost appetite",
                description = "One sheep has not eaten for two days.",
                photoUri = null,
                farmerId = "FARMER_002",
                vetId = "VET_001",
                status = ReportStatus.PENDING,
                answer = null,
                location = Location(46.5210, 6.5650, "Vaud Farm")
            )
        )
    )

    // Expose immutable state
    val reports: State<List<Report>> = _reports

    /**
     * Return reports filtered by user role.
     * Farmers see only their own reports, Vets see all reports.
     */
    fun getReportsForUser(userRole: UserRole, userId: String): List<Report> {
        return when (userRole) {
            UserRole.FARMER -> _reports.value.filter { it.farmerId == userId }
            UserRole.VET -> _reports.value
            else -> emptyList()
        }
    }

}
