package com.android.agrihealth.data.model.device.location

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.android.agrihealth.MainActivity
import com.android.agrihealth.data.model.location.Location
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test

class LocationServicesTest {
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var locationRepository: LocationRepository

  @get:Rule val activityRule = ActivityScenarioRule(MainActivity::class.java)

  val location1 = Location(7.27, 6.7)
  val location2 = Location(2.30, 54.71) // gegu adr + winrate

  @After
  fun tearDown() {
    clearMocks(locationRepository)
  }

  private fun setMockRepository(
      finePermission: Boolean,
      coarsePermission: Boolean,
      lastKnownLocation: Location = location1,
      currentLocation: Location = location2
  ) = runBlocking {
    locationRepository = mockk(relaxed = true)

    every { locationRepository.hasFineLocationPermission() } returns finePermission
    every { locationRepository.hasCoarseLocationPermission() } returns coarsePermission

    coEvery { locationRepository.getLastKnownLocation() } returns lastKnownLocation
    coEvery { locationRepository.getCurrentLocation() } returns currentLocation

    LocationRepositoryProvider.repository = locationRepository
    locationViewModel = LocationViewModel()
  }

  private fun locationsEqual(loc1: Location, loc2: Location): Boolean {
    return (loc1.latitude == loc2.latitude) && (loc1.longitude == loc2.longitude)
  }

  @Test
  fun canGetCurrentLocation() = runBlocking {
    val currentLocation = location1
    setMockRepository(
        finePermission = true, coarsePermission = true, currentLocation = currentLocation)

    val isAllowed = locationViewModel.hasLocationPermissions()
    assertTrue(isAllowed)

    val result = locationRepository.getCurrentLocation()
    assert(locationsEqual(result, currentLocation))
  }

  @Test
  fun canGetLastLocation() = runBlocking {
    val lastKnownLocation = location1
    setMockRepository(
        finePermission = true, coarsePermission = true, lastKnownLocation = lastKnownLocation)

    val isAllowed = locationViewModel.hasLocationPermissions()
    assertTrue(isAllowed)

    val result = locationRepository.getLastKnownLocation()
    assert(locationsEqual(result, lastKnownLocation))
  }

  @Test
  fun differentLocationIfUserMovedSinceLastKnown() = runBlocking {
    val lastKnownLocation = location1
    val currentLocation = location2
    setMockRepository(
        finePermission = true, coarsePermission = true, lastKnownLocation, currentLocation)

    val isAllowed = locationViewModel.hasLocationPermissions()
    assertTrue(isAllowed)

    locationViewModel.getLastKnownLocation()
    val lastResult =
        locationViewModel.locationState.first {
          it != null
        } // Wait for the value to exist before getting it
    assertNotNull(lastResult)

    locationViewModel.getCurrentLocation()
    val currentResult =
        locationViewModel.locationState.first {
          it != null && it != lastResult
        } // Same here, wait before get
    assertNotNull(currentResult)

    assert(locationsEqual(lastResult!!, lastKnownLocation))
    assert(locationsEqual(currentResult!!, currentLocation))
    assert(!locationsEqual(lastResult, currentResult))
  }

  @Test
  fun failToGetLocationWithoutFineLocation() = runBlocking {
    setMockRepository(finePermission = false, coarsePermission = true)

    val isAllowed = locationViewModel.hasLocationPermissions()
    assertFalse(isAllowed)

    val vmLocation = locationViewModel.locationState.value
    assertNull(vmLocation)

    try {
      locationViewModel.getCurrentLocation()
      fail("Location should not be attainable without permissions")
    } catch (_: IllegalStateException) {
      assertTrue(true)
    }
  }

  @Test
  fun failToGetLocationWithoutAnyPermission() = runBlocking {
    setMockRepository(finePermission = false, coarsePermission = false)

    val isAllowed = locationViewModel.hasLocationPermissions()
    assertFalse(isAllowed)

    val vmLocation = locationViewModel.locationState.value
    assertNull(vmLocation)

    try {
      locationViewModel.getCurrentLocation()
      fail("Location should not be attainable without permissions")
    } catch (_: IllegalStateException) {
      assertTrue(true)
    }
  }
}
