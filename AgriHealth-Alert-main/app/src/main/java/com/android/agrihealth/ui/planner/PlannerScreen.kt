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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.core.design.theme.onStatusColor
import com.android.agrihealth.core.design.theme.statusColor
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.repository.ReportRepositoryLocal
import com.android.agrihealth.ui.navigation.BottomNavigationMenu
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.navigation.Tab
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

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
        photoUri = null,
        farmerId = "farmer1",
        vetId = "vet1",
        status = ReportStatus.IN_PROGRESS,
        answer = null,
        location = Location(latitude = 12.34, longitude = 56.78, name = "Farmhouse A"),
        startTime = 7f,
        duration = 1.5f)

val previewReport2 = previewReport1.copy(startTime = 2f, duration = null)
val previewReport3 = previewReport1.copy(startTime = 5f, duration = 0.9f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    navigationActions: NavigationActions? = null,
    plannerVM: PlannerViewModel = viewModel()
) {

  val uiState by plannerVM.uiState.collectAsState()

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
          Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                  "Week ${uiState.selectedWeekNumber}",
                  style = MaterialTheme.typography.titleMedium)
              WeekHeader(uiState.selectedWeek[0], uiState.selectedWeek[6])
            }
            Row(modifier = Modifier.fillMaxWidth()) {
              listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLetter ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                  Text(dayLetter)
                }
              }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  uiState.selectedWeek.forEach { dayNumber ->
                    DayCard(Modifier.weight(1f), dayNumber)
                  }
                }
            Row(modifier = Modifier.fillMaxWidth()) {
              Text(
                  uiState.selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM")),
                  style = MaterialTheme.typography.titleLarge)
            }
            DailyScheduler(listOf(previewReport1, previewReport2, previewReport3)) { it ->
              navigationActions?.navigateTo(Screen.ViewReport(it))
            }
          }
        }
      })
}

@Composable
fun WeekHeader(start: LocalDate, end: LocalDate) {
  val dayFormatter = remember { DateTimeFormatter.ofPattern("d") }
  val monthFormatter = remember { DateTimeFormatter.ofPattern("MMM") }

  val sameMonth = start.month == end.month

  val text =
      if (sameMonth) {
        "${start.format(dayFormatter)} - ${end.format(dayFormatter)} ${start.format(monthFormatter)}"
      } else {
        "${start.format(dayFormatter)} ${start.format(monthFormatter)} - " +
            "${end.format(dayFormatter)} ${end.format(monthFormatter)}"
      }

  Text(text)
}

@Composable
fun DayCard(modifier: Modifier = Modifier, day: LocalDate) {
  Card(modifier = modifier.height(64.dp)) {
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              day.dayOfMonth.toString(),
              modifier = Modifier,
              style = MaterialTheme.typography.titleLarge)
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
  val fakeVM = PlannerViewModel(ReportRepositoryLocal())
  AgriHealthAppTheme { PlannerScreen(plannerVM = fakeVM) }
}
