package com.android.agrihealth.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.core.design.theme.onStatusColor
import com.android.agrihealth.core.design.theme.statusColor
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.ui.navigation.BottomNavigationMenu
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.navigation.Tab
import kotlin.math.max

// Todo: Implement real week days
// Todo: Current date fetching
// Todo: scroll to first task of the day
// Todo: implement task overlapping
// Todo: implement task click to view Report
// Todo: implement switch day by clicking on week days
// Todo: implement setting due date if seen from ViewReport screen
val previewReport1 =
    Report(
        id = "1",
        title = "Checkup with Farmer John",
        description = "Regular health checkup for the cattle.",
        questionForms = emptyList(),
        photoURL = null,
        farmerId = "farmer1",
        officeId = "office1",
        status = ReportStatus.IN_PROGRESS,
        answer = null,
        location = Location(latitude = 12.34, longitude = 56.78, name = "Farmhouse A"),
        startTime = 7f,
        duration = 1.5f)

val previewReport2 = previewReport1.copy(startTime = 2f, duration = null)
val previewReport3 = previewReport1.copy(startTime = 5f, duration = 0.9f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(navigationActions: NavigationActions? = null) {

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  Screen.Planner().name,
                  style = MaterialTheme.typography.titleLarge,
                  modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions?.goBack() },
                  modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go Back")
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Planner,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { pd ->
        Box(modifier = Modifier.padding(pd).padding(horizontal = 8.dp)) {
          Column() {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Week 46 ", style = MaterialTheme.typography.titleMedium)
              Text("16 - 22 nov", style = MaterialTheme.typography.titleMedium)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
              listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLetter ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                  Text(dayLetter)
                }
              }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  listOf("16", "17", "18", "19", "20", "21", "22").forEach { dayNumber ->
                    DayCard(Modifier.weight(1f), dayNumber)
                  }
                }
            Row(modifier = Modifier.fillMaxWidth()) {
              Text("17 Mon", style = MaterialTheme.typography.titleLarge)
            }
            DailyScheduler(listOf(previewReport1, previewReport2, previewReport3)) { it ->
              navigationActions?.navigateTo(Screen.ViewReport(it))
            }
          }
        }
      })
}

@Composable
fun DayCard(modifier: Modifier = Modifier, day: String) {
  Card(modifier = modifier.height(64.dp)) {
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(day, modifier = Modifier, style = MaterialTheme.typography.titleLarge)
          DotGrid(
              dots =
                  listOf(
                      ReportStatus.RESOLVED,
                      ReportStatus.IN_PROGRESS,
                      ReportStatus.IN_PROGRESS,
                      ReportStatus.PENDING,
                      ReportStatus.PENDING,
                      ReportStatus.PENDING,
                      ReportStatus.PENDING,
                      ReportStatus.IN_PROGRESS,
                      ReportStatus.IN_PROGRESS,
                  ))
        }
  }
}

@Composable
fun DotGrid(
    dots: List<ReportStatus>,
    dotSize: Dp = 8.dp,
) {
  val density = LocalDensity.current
  var dayTagWidth by remember { mutableStateOf(64.dp) }

  Column(
      modifier =
          Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
            val newWidth = with(density) { coordinates.size.width.toDp() }
            if (newWidth != dayTagWidth) {
              dayTagWidth = newWidth
            }
          },
      verticalArrangement = Arrangement.SpaceEvenly) {
        dots.chunked((dayTagWidth / (dotSize + 2.dp)).toInt()).forEach { rowDots ->
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            rowDots.forEach { status ->
              Box(
                  modifier =
                      Modifier.size(dotSize).background(statusColor(status), shape = CircleShape))
            }
          }
        }
      }
}

@Composable
fun DailyScheduler(reports: List<Report>, navigateToReport: (String) -> Unit = {}) {
  val scrollState = rememberScrollState(0)

  val hourHeight = 60.dp
  Row(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
    HourScale(hourHeight = hourHeight)
    Box(modifier = Modifier.weight(1f)) {
      DailyTasks(reports = reports, hourHeight = hourHeight, navigateToReport = navigateToReport)
    }
  }
}

@Composable
fun HourScale(hourHeight: Dp = 60.dp) {
  Column {
    for (hour in 0..24) {
      Box(
          modifier = Modifier.height(hourHeight)
          // .fillMaxWidth()
          ,
          contentAlignment = Alignment.TopStart) {
            Text(text = "$hour:00", modifier = Modifier.padding(4.dp))
          }
    }
  }
}

@Composable
fun DailyTasks(
    reports: List<Report>,
    hourHeight: Dp = 60.dp,
    navigateToReport: (String) -> Unit = {}
) {
  Box(modifier = Modifier.fillMaxSize()) {
    reports
        .filter { it.startTime != null }
        .forEach { report ->
          val topOffset = report.startTime!!.times(hourHeight.value)
          val taskHeight = max(report.duration?.times(hourHeight.value) ?: 0f, 30f)

          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .height(taskHeight.dp)
                      .offset(y = topOffset.dp)
                      .padding(horizontal = 0.dp)
                      .background(
                          color = statusColor(report.status), shape = MaterialTheme.shapes.medium)
                      .clickable { navigateToReport(report.id) },
              verticalArrangement = Arrangement.Top) {
                Text(
                    report.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = onStatusColor(report.status),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (taskHeight.dp >= 90.dp) 2 else 1)
                if (taskHeight.dp >= 54.dp)
                    report.location?.name?.let {
                      Text(
                          it,
                          modifier = Modifier.weight(1f, fill = false),
                          style = MaterialTheme.typography.bodyMedium,
                          color = onStatusColor(report.status),
                          maxLines = 1)
                    }
              }
        }
  }
}

@Preview
@Composable
fun PlannerScreenPreview() {
  AgriHealthAppTheme { PlannerScreen() }
}
