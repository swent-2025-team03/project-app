package com.android.agrihealth.data.model.location

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.agrihealth.data.model.device.location.LocationRepository
import com.android.agrihealth.data.model.device.location.LocationRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.testhelpers.templates.BaseUITest
import com.android.agrihealth.testhelpers.TestTimeout.LONG_TIMEOUT
import com.android.agrihealth.ui.common.LocationPicker
import com.android.agrihealth.ui.common.LocationPickerTestTags
import com.android.agrihealth.ui.map.MapScreenTestTags
import com.android.agrihealth.ui.map.MapViewModel
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocationPickerTest : BaseUITest() {

  private lateinit var locationRepository: LocationRepository

  @Before
  fun setUp() {
    locationRepository = mockk(relaxed = true) // TODO: Consider replacing with a fake repository
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

    assertNodeIsDisplayed(LocationPickerTestTags.MAP_SCREEN, LONG_TIMEOUT)

    clickOnNode(LocationPickerTestTags.SELECT_LOCATION_BUTTON)

    assertNodeIsDisplayed(LocationPickerTestTags.CONFIRMATION_PROMPT)
    assertNodeIsDisplayed(LocationPickerTestTags.PROMPT_CANCEL_BUTTON)
    
    clickOnNode(LocationPickerTestTags.PROMPT_CONFIRM_BUTTON)

    assertTrue(confirmClicked)
  }
}
