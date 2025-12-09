package com.android.agrihealth.ui.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.android.agrihealth.R
import com.android.agrihealth.core.design.theme.statusColor
import com.android.agrihealth.data.model.alert.Alert
import com.android.agrihealth.data.model.alert.distanceMeters
import com.android.agrihealth.data.model.location.toLatLng
import com.android.agrihealth.data.model.location.toLocation
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline

// === Map setup ===

/** Returns the app top bar for a map-like screen, using the provided title */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopBar(onBack: () -> Unit, title: String = "Map") {
  TopAppBar(
      title = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = title,
                  style = MaterialTheme.typography.titleLarge,
                  modifier = Modifier.weight(1f).testTag(MapScreenTestTags.TOP_BAR_MAP_TITLE))
            }
      },
      navigationIcon = {
        IconButton(
            onClick = onBack, modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
              Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
      })
}

/** Gets the Google map UI settings and theme used in our app */
@Composable
fun getUISettingsAndTheme(): Pair<MapUiSettings, MapProperties> {
  val googleMapUiSettings = remember {
    MapUiSettings(
        zoomControlsEnabled = false,
    )
  }

  val context = LocalContext.current
  val darkTheme = isSystemInDarkTheme()
  val styleRes = if (darkTheme) R.raw.map_style_dark else R.raw.map_style_light
  val style = MapStyleOptions.loadRawResourceStyle(context, styleRes)

  val googleMapMapProperties = remember(style) { MapProperties(mapStyleOptions = style) }

  return Pair(googleMapUiSettings, googleMapMapProperties)
}

// === Map components ===
// === Reports ===

/**
 * Displays the given spiderified reports
 *
 * @param reports Reports to show
 * @param isSelected Predicate on a single report to highlight it or not
 * @param onClick Action to take when a single report is clicked
 */
@Composable
fun ReportMarkers(
    reports: List<SpiderifiedReport>,
    isSelected: (report: Report) -> Boolean = { false },
    onClick: (report: Report) -> Unit = {}
) {
  reports.forEach {
    val report = it.report

    val markerSize = if (isSelected(report)) 60f else 40f
    val markerIcon = createCircleMarker(statusColor(report.status).toArgb(), markerSize)

    Marker(
        state = MarkerState(position = it.position),
        anchor = Offset(0.5f, 0.5f),
        title = report.title,
        snippet = report.description,
        icon = markerIcon,
        onClick = {
          onClick(report)
          true
        })

    // Spider effect if multiple markers on same location
    Polyline(
        points = listOf(it.position, it.center),
        width = 5f,
        color = MaterialTheme.colorScheme.onBackground)
  }
}

/**
 * Bottom menu to show info on the selected [report].
 *
 * Contain a 2 line Report title, and the entire description. Also has an IconButton that executes
 * [onReportClick]
 *
 * @param modifier Modifier on the visible Column.
 * @param report The report whose information will be displayed.
 * @param onReportClick run when IconButton is clicked.
 */
@Composable
fun ShowReportInfo(
    modifier: Modifier = Modifier,
    report: Report?,
    onReportClick: (String) -> Unit = {}
) {
  if (report == null) return

  Box(modifier = Modifier.fillMaxSize().testTag(MapScreenTestTags.REPORT_INFO_BOX)) {
    InfoElement(
        Modifier.align(Alignment.BottomCenter),
        report.title,
        report.description,
        "View report",
        { onReportClick(report.id) },
        MapScreenTestTags.getTestTagForReportTitle(report.id),
        MapScreenTestTags.getTestTagForReportDesc(report.id))
  }
}

// === Alerts ===

/** Displays relevant areas for every current alert */
@Composable
fun AlertAreas(alerts: List<Alert>) {
  alerts.forEach { alert ->
    alert.zones?.forEach { zone ->
      Circle(
          center = zone.center.toLatLng(),
          radius = zone.radiusMeters,
          fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3F),
          strokeColor = MaterialTheme.colorScheme.primary,
          strokeWidth = 4f)
    }
  }
}

/** Menu to show info about every alert provided */
@Composable
fun ShowAlertInfo(alerts: List<Alert>, onClick: (alert: Alert) -> Unit) {
  if (alerts.isEmpty()) return

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier =
            Modifier.background(color = MaterialTheme.colorScheme.surface)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()) {
          items(alerts) { alert ->
            InfoElement(
                title = alert.title,
                description = alert.description,
                buttonDesc = "View alert",
                onButtonClick = { onClick(alert) },
                titleTestTag = "",
                descTestTag = "")

            if (alerts.last() != alert)
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3F))
          }
        }
  }
}

fun findAlertZonesUnderTap(alerts: List<Alert>, tap: LatLng): List<Alert> {
  return alerts.filter { alert ->
    alert.zones?.any { zone ->
      val center = zone.center
      val radius = zone.radiusMeters
      val distance = distanceMeters(center, tap.toLocation())
      distance <= radius
    } == true
  }
}

// === UI ===

@Composable
fun InfoElement(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    buttonDesc: String,
    onButtonClick: () -> Unit,
    titleTestTag: String,
    descTestTag: String
) {
  Column(
      modifier =
          Modifier.background(color = MaterialTheme.colorScheme.surface)
              .fillMaxWidth()
              .padding(16.dp)
              .then(modifier)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text(
              text = title,
              style = MaterialTheme.typography.titleLarge,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f).padding(end = 8.dp).testTag(titleTestTag))

          IconButton(
              onClick = onButtonClick,
              modifier =
                  Modifier.align(Alignment.CenterVertically)
                      .size(32.dp)
                      .testTag(MapScreenTestTags.REPORT_NAVIGATION_BUTTON),
              colors =
                  IconButtonColors(
                      MaterialTheme.colorScheme.primaryContainer,
                      MaterialTheme.colorScheme.onPrimaryContainer,
                      disabledContainerColor = MaterialTheme.colorScheme.surface,
                      disabledContentColor = MaterialTheme.colorScheme.onSurface)) {
                Icon(
                    imageVector = Icons.Default.Preview,
                    contentDescription = buttonDesc,
                    modifier = Modifier.size(24.dp))
              }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(text = description, modifier = Modifier.testTag(descTestTag))
        Spacer(modifier = Modifier.height(8.dp))
      }
}

/** Displays a switch to show/hide reports on the map */
@Composable
fun ReportVisibilitySwitch(shouldDisplay: Boolean, onChange: (Boolean) -> Unit) {
  VisibilitySwitch("Show reports", shouldDisplay, onChange)
}

/** Displays a switch to show/hide alerts on the map */
@Composable
fun AlertVisibilitySwitch(shouldDisplay: Boolean, onChange: (Boolean) -> Unit) {
  VisibilitySwitch("Show alerts", shouldDisplay, onChange)
}

@Composable
private fun VisibilitySwitch(text: String, shouldDisplay: Boolean, onChange: (Boolean) -> Unit) {
  Row(modifier = Modifier.padding(horizontal = 8.dp)) {
    Text(
        text,
        Modifier.align(Alignment.CenterVertically),
        color = MaterialTheme.colorScheme.onSurface)
    Spacer(Modifier.size(8.dp))
    Switch(checked = shouldDisplay, onCheckedChange = { enabled -> onChange(enabled) })
  }
}

/** Displays a dropdown menu to filter reports based on their status */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFilterMenu(selectedOption: String?, onOptionSelected: (String?) -> Unit) {
  val options = listOf(null) + ReportStatus.entries.map { it.displayString() }

  var expanded by remember { mutableStateOf(false) }
  val textFieldBackgroundColor = MaterialTheme.colorScheme.surface

  val textMeasurer = rememberTextMeasurer()
  val maxTextWidth =
      remember(options) {
        options.maxOf {
          textMeasurer.measure(text = AnnotatedString(it ?: AllFilterText)).size.width
        }
      }
  val dropdownWidth = maxTextWidth - 32.dp.value

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = !expanded },
      modifier = Modifier.padding(8.dp).width(dropdownWidth.dp)) {
        OutlinedTextField(
            value = selectedOption ?: AllFilterText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().testTag(MapScreenTestTags.REPORT_FILTER_MENU),
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = textFieldBackgroundColor,
                    focusedContainerColor = textFieldBackgroundColor))

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
              options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                      onOptionSelected(option)
                      expanded = false
                    },
                    text = { Text(option ?: AllFilterText) },
                    modifier = Modifier.testTag(MapScreenTestTags.getTestTagForFilter(option)))

                if (option == AllFilterText)
                    HorizontalDivider(
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        thickness = 1.dp)
              }
            }
      }
}

@Composable
fun RefreshLocationButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
  FloatingActionButton(
      modifier = Modifier.padding(16.dp).testTag(MapScreenTestTags.REFRESH_BUTTON).then(modifier),
      onClick = onClick) {
        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Location")
      }
}

private fun createCircleMarker(
    color: Int,
    radius: Float = 40f,
    strokeWidth: Float = 8f
): BitmapDescriptor {
  val size = (radius * 2 + strokeWidth).toInt()
  val bitmap = createBitmap(size, size)
  val canvas = Canvas(bitmap)

  // Draw filled circle
  val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  paint.color = color
  paint.style = Paint.Style.FILL
  canvas.drawCircle(size / 2f, size / 2f, radius, paint)

  // Draw white outline
  val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
  strokePaint.color = Color.WHITE
  strokePaint.style = Paint.Style.STROKE
  strokePaint.strokeWidth = strokeWidth
  canvas.drawCircle(size / 2f, size / 2f, radius, strokePaint)

  // Draw white center dot
  val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  val dotRadius = radius / 4f
  dotPaint.color = Color.WHITE
  dotPaint.style = Paint.Style.FILL
  canvas.drawCircle(size / 2f, size / 2f, dotRadius, dotPaint)

  return BitmapDescriptorFactory.fromBitmap(bitmap)
}

// === Test stuff ===

/**
 * Displays debug boxes to act as markers during instrumented tests. This is because Google maps
 * markers cannot be accessed during testing. onClick should have the same behavior as the real
 * markers
 */
@Composable
fun MapTestMarkers(reports: List<SpiderifiedReport>, onClick: (Report) -> Unit) {
  reports.forEach {
    val report = it.report

    Box(
        modifier =
            Modifier.testTag(MapScreenTestTags.getTestTagForReportMarker(report.id))
                .clickable { onClick(report) }
                .alpha(0f)
                .size(1.dp)) {
          Text(":)") // Box doesn't exist if it's not visible
    }
  }
}
