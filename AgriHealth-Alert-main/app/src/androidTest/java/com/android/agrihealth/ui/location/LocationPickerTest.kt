package com.android.agrihealth.ui.location

import com.android.agrihealth.data.model.device.location.LocationRepository
import com.android.agrihealth.data.model.device.location.LocationRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.testhelpers.TestTimeout.LONG_TIMEOUT
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.common.LocationPicker
import com.android.agrihealth.ui.common.LocationPickerTestTags
import com.android.agrihealth.ui.map.MapViewModel
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
            assertEquals(position, selectedPosition)
          },
          onAddress = { address ->
            confirmClicked = true
            assertTrue(address?.contains(cityName) == true)
          })
    }

    nodeIsDisplayed(LocationPickerTestTags.MAP_SCREEN, LONG_TIMEOUT)

    clickOn(LocationPickerTestTags.SELECT_LOCATION_BUTTON)

    nodeIsDisplayed(LocationPickerTestTags.CONFIRMATION_PROMPT)
    nodeIsDisplayed(LocationPickerTestTags.PROMPT_CANCEL_BUTTON)

    clickOn(LocationPickerTestTags.PROMPT_CONFIRM_BUTTON)

    assertTrue(confirmClicked)
  }

  override fun displayAllComponents() {}
}
