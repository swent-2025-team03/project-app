package com.android.agrihealth.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.data.repository.ReportRepositoryProvider
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class MapUIState(
    val reports: List<Report> = emptyList(),
    val locationPermission: Boolean = false
)

class MapViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val locationViewModel: LocationViewModel,
    val selectedReportId: String? = null,
    startingPosition: Location? = null,
    showReports: Boolean = true
) : ViewModel() {
  private val _uiState = MutableStateFlow(MapUIState())
  val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

  private val _startingLocation =
      MutableStateFlow(startingPosition ?: Location(46.9481, 7.4474, null)) // Bern
  val startingLocation = _startingLocation.asStateFlow()
  private val _zoom = MutableStateFlow(10f)
  val zoom = _zoom.asStateFlow()
  private val _selectedReport = MutableStateFlow<Report?>(null)
  val selectedReport: StateFlow<Report?> = _selectedReport.asStateFlow()

  init {
    if (selectedReportId != null) {
      viewModelScope.launch {
        _selectedReport.value = reportRepository.getReportById(selectedReportId)
      }
    }
    refreshMapPermission()
    setStartingLocation()

    if (showReports)
        refreshReports(
            Firebase.auth.currentUser?.uid
                ?: throw IllegalArgumentException(
                    "Map refreshed Reports while current user was null"))
  }

  fun refreshReports(uid: String) {
    fetchLocalizableReports(uid)
  }

  fun refreshMapPermission() {
    _uiState.value =
        _uiState.value.copy(locationPermission = locationViewModel.hasLocationPermissions())
  }

  private fun fetchLocalizableReports(uid: String) {
    viewModelScope.launch {
      try {
        val reports = reportRepository.getAllReports(uid).filter { it.location != null }
        _uiState.value = _uiState.value.copy(reports = reports)
      } catch (e: Exception) {
        Log.e("MapScreen", "Failed to load todos: ${e.message}")
      }
    }
  }

  fun setSelectedReport(report: Report?) {
    _selectedReport.value = report
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
        if (locationViewModel.hasLocationPermissions()) {
          if (useCurrentLocation) {
            locationViewModel.getCurrentLocation()
          } else {
            locationViewModel.getLastKnownLocation()
          }
          val gpsLocation =
              withTimeoutOrNull(3_000) {
                locationViewModel.locationState.firstOrNull { it != null }
              }

          _startingLocation.value = gpsLocation ?: getLocationFromUserAddress() ?: return@launch
        }
      }
    }
  }

  private suspend fun getLocationFromUserAddress(): Location? {
    val uid = Firebase.auth.currentUser?.uid ?: return null
    val user = userRepository.getUserFromId(uid)
    return user.getOrNull()?.address
  }

  fun refreshCameraPosition() {
    setStartingLocation(useCurrentLocation = true)
  }

  data class SpiderifiedReport(val report: Report, val position: LatLng, val center: LatLng)

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
  fun spiderifiedReports(): List<SpiderifiedReport> {
    val groups =
        uiState.value.reports
            .filter { it -> it.location != null }
            .groupBy { Pair(it.location!!.latitude, it.location.longitude) }
    val result = mutableListOf<SpiderifiedReport>()

    for ((latLong, group) in groups) {
      val baseLat = latLong.first
      val baseLng = latLong.second
      val center = LatLng(baseLat, baseLng)

      if (group.size == 1) {
        result.add(SpiderifiedReport(group.first(), center, center))
      } else {
        val radiusMeters = 20.0 + group.size * 5.0
        val angleStep = 2 * Math.PI / group.size
        group.forEachIndexed { index, report ->
          val angle = index * angleStep
          val offset = offsetLatLng(baseLat, baseLng, radiusMeters, angle)
          result.add(SpiderifiedReport(report, offset, center))
        }
      }
    }
    return result
  }

  /**
   * Offset [lat] and [lng] by [distanceMeters] in the direction of [angleRadians].
   *
   * @param lat the latitude of the point to offset
   * @param lng the longitude of the point to offset
   * @param distanceMeters the distance in meters to offset by which the point is offset
   * @param angleRadians angle to offset by. 0 offset to the right, PI offset to the left.
   */
  fun offsetLatLng(lat: Double, lng: Double, distanceMeters: Double, angleRadians: Double): LatLng {
    val earthRadius = 6371000.0 // meters
    val dLat = (distanceMeters / earthRadius) * sin(angleRadians)
    val dLng = (distanceMeters / (earthRadius * cos(Math.toRadians(lat)))) * cos(angleRadians)

    return LatLng(lat + Math.toDegrees(dLat), lng + Math.toDegrees(dLng))
  }
}
