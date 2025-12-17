package com.android.agrihealth.previews.common

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.device.location.LocationRepository
import com.android.agrihealth.data.model.device.location.LocationRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.ui.common.LocationPicker
import com.android.agrihealth.ui.map.MapViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
@Preview
fun LocationPickerPreview() {
  val context = LocalContext.current

  LocationRepositoryProvider.repository = LocationRepository(context)
  val mapViewModel =
      MapViewModel(locationViewModel = LocationViewModel(), userId = "1234", showReports = false)

  AgriHealthAppTheme {
    LocationPicker(
        mapViewModel, null, onAddress = { address -> Log.d("LocationPicker", "Address: $address") })
  }
}
