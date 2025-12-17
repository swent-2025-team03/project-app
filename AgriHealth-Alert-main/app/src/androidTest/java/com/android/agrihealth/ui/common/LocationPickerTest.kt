package com.android.agrihealth.ui.common

import com.android.agrihealth.data.model.device.location.LocationRepository
import com.android.agrihealth.data.model.device.location.LocationRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.testhelpers.TestTimeout
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.map.MapViewModel
import io.mockk.mockk
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LocationPickerTest : UITest() {

  private lateinit var locationRepository: LocationRepository

  @Before
  fun setUp() {
    locationRepository = mockk(relaxed = true)
    LocationRepositoryProvider.repository = locationRepository
  }

  @Test
  fun locationPicker_getsRightCoordinatesAndCity() {
    val position = Location(46.7815062, 6.6463836) // Station d'épuration d'Yverdon-les-Bains
    val cityName = "Yverdon"

    val mapViewModel =
        MapViewModel(
            locationViewModel = LocationViewModel(),
            userId = "test-user",
            startingPosition = position,
            showReports = false,
            showAlerts = false)

    var confirmClicked = false

    setContent {
      LocationPicker(
          mapViewModel = mapViewModel,
          onLatLng = { lat, lng ->
            val selectedPosition = Location(lat, lng)
            assertLocationsEqual(position, selectedPosition)
          },
          onAddress = { address ->
            confirmClicked = true
            Assert.assertTrue(address?.contains(cityName) == true)
          })
    }

    nodeIsDisplayed(LocationPickerTestTags.MAP_SCREEN, TestTimeout.LONG_TIMEOUT)

    composeTestRule.waitForIdle()
    clickOn(LocationPickerTestTags.SELECT_LOCATION_BUTTON, TestTimeout.SUPER_LONG_TIMEOUT)

    composeTestRule.waitForIdle()
    nodeIsDisplayed(LocationPickerTestTags.CONFIRMATION_PROMPT, TestTimeout.SUPER_LONG_TIMEOUT)
    nodeIsDisplayed(LocationPickerTestTags.PROMPT_CANCEL_BUTTON)

    clickOn(LocationPickerTestTags.PROMPT_CONFIRM_BUTTON)

    Assert.assertTrue(confirmClicked)
  }

  private fun assertLocationsEqual(expected: Location, actual: Location) {
    val delta = 1e-6
    assertEquals(expected.latitude, actual.latitude, delta)
    assertEquals(expected.longitude, actual.longitude, delta)
  }

  override fun displayAllComponents() {}
}
