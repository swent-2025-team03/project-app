package com.android.agrihealth.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
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
import kotlinx.coroutines.launch

data class MapUIState(
    val reports: List<Report> = emptyList(),
)

class MapViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    val selectedReportId: String? = null
) : ViewModel() {
  private val _uiState = MutableStateFlow(MapUIState())
  val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

  private val _startingLocation = MutableStateFlow(Location(46.9481, 7.4474, null)) // Bern
  val startingLocation = _startingLocation.asStateFlow()
  private val _zoom = MutableStateFlow(10f)
  val zoom = _zoom.asStateFlow()
  var selectedReport: Report? = null

  init {
    if (selectedReportId != null) {
      viewModelScope.launch { selectedReport = reportRepository.getReportById(selectedReportId) }
    }
  }

  fun refreshReports(uid: String) {
    fetchLocalizableReports(uid)
  }

  private fun fetchLocalizableReports(uid: String) {
    viewModelScope.launch {
      try {
        val reports = reportRepository.getAllReports(uid).filter { it.location != null }
        _uiState.value = MapUIState(reports = reports)
        Log.d("MapScreen", "Loaded ${reports.count()} reports for UID $uid")
      } catch (e: Exception) {
        Log.w("MapScreen", "Failed to load todos: ${e.message}")
      }
    }
  }

  fun setStartingLocation(location: Location?, useCurrentLocation: Boolean = false) {
    // Specific starting point, takes priority because of report navigation for example
    if (location != null) {
      _startingLocation.value = location
      _zoom.value = 15f
    }
    // Default starting position, so either location or workplace or default
    else {
      // TODO: Something that fetches device location?
      viewModelScope.launch {
        val location = getLocationFromUserAddress() ?: return@launch
        _startingLocation.value = location
        _zoom.value = 12f
      }
    }
  }

  private suspend fun getLocationFromUserAddress(): Location? {
    val uid = Firebase.auth.currentUser?.uid ?: return null
    val user = userRepository.getUserFromId(uid)
    return user.getOrNull()?.address
  }

  fun refreshCameraPosition() {
    setStartingLocation(null, useCurrentLocation = true)
  }

  fun spiderifiedReports(): List<Pair<Report, LatLng>> {
    val groups =
        uiState.value.reports.groupBy { Pair(it.location!!.latitude, it.location.longitude) }
    val result = mutableListOf<Pair<Report, LatLng>>()

    for ((latLong, group) in groups) {
      val baseLat = latLong.first
      val baseLng = latLong.second
      val center = LatLng(baseLat, baseLng)

      if (group.size == 1) {
        result.add(group.first() to center)
      } else {
        val radiusMeters = 20.0 + group.size * 5.0
        val angleStep = 2 * Math.PI / group.size
        group.forEachIndexed { index, report ->
          val angle = index * angleStep
          val offset = offsetLatLng(baseLat, baseLng, radiusMeters, angle)
          result.add(report to offset)
        }
      }
    }
    return result
  }

  fun offsetLatLng(lat: Double, lng: Double, distanceMeters: Double, angleRadians: Double): LatLng {
    val earthRadius = 6371000.0 // meters
    val dLat = (distanceMeters / earthRadius) * sin(angleRadians)
    val dLng = (distanceMeters / (earthRadius * cos(Math.toRadians(lat)))) * cos(angleRadians)

    return LatLng(lat + Math.toDegrees(dLat), lng + Math.toDegrees(dLng))
  }
}
