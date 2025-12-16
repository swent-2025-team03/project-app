package com.android.agrihealth.ui.map

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.alert.Alert
import com.android.agrihealth.data.model.alert.AlertRepository
import com.android.agrihealth.data.model.alert.AlertRepositoryProvider
import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.location.offsetLatLng
import com.android.agrihealth.data.model.location.toLatLng
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.data.repository.ReportRepositoryProvider
import com.android.agrihealth.ui.loading.withLoadingState
import com.google.android.gms.maps.model.LatLng
import java.util.Locale
import kotlin.collections.firstOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class MapUIState(
    val reports: List<SpiderifiedReport> = emptyList(),
    val alerts: List<Alert> = emptyList(),
    val geocodedAddress: String? = null,
    val isLoadingLocation: Boolean = false,
)

data class SpiderifiedReport(val report: Report, val position: LatLng, val center: LatLng)

class MapViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository,
    private val alertRepository: AlertRepository = AlertRepositoryProvider.get(),
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val locationViewModel: LocationViewModel,
    private val userId: String,
    val selectedReportId: String? = null,
    val selectedAlertId: String? = null,
    startingPosition: Location? = null,
    showReports: Boolean = true,
    showAlerts: Boolean = true
) : ViewModel() {
  private val _uiState = MutableStateFlow(MapUIState())
  val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

  private fun updateState(reducer: MapUIState.() -> MapUIState) {
    _uiState.update { it.reducer() }
  }

  private val _selectedReport = MutableStateFlow<Report?>(null)
  val selectedReport = _selectedReport.asStateFlow()

  private val _selectedAlert = MutableStateFlow<Alert?>(null)
  val selectedAlert = _selectedAlert.asStateFlow()

  private fun Report?.select() {
    _selectedReport.value = this
  }

  private fun Alert?.select() {
    _selectedAlert.value = this
  }

  private val _startingLocation =
      MutableStateFlow(startingPosition ?: Location(46.9481, 7.4474)) // Bern
  val startingLocation = _startingLocation.asStateFlow()
  private val _zoom = MutableStateFlow(10f)
  val zoom = _zoom.asStateFlow()

  init {
    if (showReports) {
      refreshReports()
    }

    if (showAlerts) {
      refreshAlerts()
    }

    if (selectedReportId != null) {
      viewModelScope.launch { reportRepository.getReportById(selectedReportId).select() }
    } else if (selectedAlertId != null) {
      viewModelScope.launch { alertRepository.getAlertById(selectedAlertId).select() }
    }

    setStartingLocation()
  }

  /** Fetches every report linked to the current user and exposes them in MapUIState */
  fun refreshReports() {
    viewModelScope.launch {
      try {
        val newReports = fetchSpiderifiedReports()
        updateState { copy(reports = newReports) }
      } catch (e: Exception) {
        Log.e("MapScreen", "Failed to load reports: ${e.message}")
      }
    }
  }

  /** Fetches every current alert and exposes them in MapUIState */
  fun refreshAlerts() {
    viewModelScope.launch {
      try {
        val newAlerts = alertRepository.getAlerts()
        updateState { copy(alerts = newAlerts) }
      } catch (e: Exception) {
        Log.e("MapScreen", "Failed to load alerts: ${e.message}")
      }
    }
  }

  /** Sets the currently selected report. Intended to be highlighted in the map */
  fun setSelectedReport(report: Report?) {
    report.select()
  }

  /**
   * Resets the camera to its original position, or moves to the user's current location if allowed
   */
  fun refreshCameraPosition() {
    setStartingLocation(useCurrentLocation = true)
  }

  /**
   * Sets the starting location for the map
   *
   * This function sets starting location based on the following priority:
   * 1. [location] if provided
   * 2. default location if the app does not have location permissions
   * 3. current location if [useCurrentLocation]
   * 4. else Last known location
   *
   * @param location the map screen will start at this location if not null.
   * @param useCurrentLocation will fetch new location instead of using last known location if true.
   */
  fun setStartingLocation(location: Location? = null, useCurrentLocation: Boolean = false) {
    // Specific starting point, takes priority because of report navigation for example
    if (location != null) {
      _startingLocation.value = location
      _zoom.value = 15f
    }
    // Default starting position, so either location or workplace or default
    else {
      viewModelScope.launch {
        _zoom.value = 12f

        // Prevent overlapping location fetches
        if (_uiState.value.isLoadingLocation) return@launch

        // If no permissions, fallback to user address without toggling loading
        if (!locationViewModel.hasLocationPermissions()) {
          val fallback = getLocationFromUserAddress()
          if (fallback != null) _startingLocation.value = fallback
          return@launch
        }

        // Permissions granted: toggle isLoadingLocation while fetching GPS
        _uiState.withLoadingState(
            applyLoading = { s: MapUIState, loading: Boolean ->
              s.copy(isLoadingLocation = loading)
            }) {
              if (useCurrentLocation) {
                locationViewModel.getCurrentLocation()
              } else {
                locationViewModel.getLastKnownLocation()
              }
              val gpsLocation =
                  withTimeoutOrNull(3_000) {
                    locationViewModel.locationState.firstOrNull { it != null }
                  }

              _startingLocation.value =
                  gpsLocation ?: getLocationFromUserAddress() ?: return@withLoadingState
            }
      }
    }
  }

  private suspend fun getLocationFromUserAddress(): Location? {
    val user = userRepository.getUserFromId(userId)
    return user.getOrNull()?.address
  }

  /**
   * Generate a List of [SpiderifiedReport] with new positions centered around common report
   * locations.
   *
   * This function spreads report with equal [Report.location] in a circle around the common
   * location.
   *
   * If the [Report] is the only one in the location it keeps it does not impact it.
   *
   * @return A list of [SpiderifiedReport] objects containing their adjusted map positions.
   * @see offsetLatLng for how the offset positions are calculated
   */
  private suspend fun fetchSpiderifiedReports(): List<SpiderifiedReport> {
    val currentReports = reportRepository.getAllReports(userId)

    val groups =
        currentReports
            .filter { it.location != null }
            .groupBy { Pair(it.location!!.latitude, it.location.longitude) }

    val newReports = mutableListOf<SpiderifiedReport>()

    for ((latLong, group) in groups) {
      val position = Location(latLong.first, latLong.second)
      val center = position.toLatLng()

      if (group.size == 1) {
        newReports.add(SpiderifiedReport(group.first(), center, center))
      } else {
        val radiusMeters = 20.0 + group.size * 5.0
        val angleStep = 2 * Math.PI / group.size
        group.forEachIndexed { index, report ->
          val angle = index * angleStep
          val offset = offsetLatLng(position, radiusMeters, angle)

          newReports.add(SpiderifiedReport(report, offset.toLatLng(), center))
        }
      }
    }

    return newReports
  }

  /**
   * Converts geographical coordinates into a text address
   *
   * @param context Current composable context
   * @param lat Latitude to convert
   * @param lng Longitude to convert
   */
  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  fun getAddressFromLatLng(context: Context, lat: Double, lng: Double) {
    val geocoder = Geocoder(context, Locale.getDefault())

    try {
      // Deprecated but I can't use the new function for some reason
      val addresses = geocoder.getFromLocation(lat, lng, 1)
      val result = addresses?.firstOrNull()?.getAddressLine(0)
      updateState { copy(geocodedAddress = result) }
    } catch (_: Exception) {
      updateState { copy(geocodedAddress = null) }
    }
  }
}
