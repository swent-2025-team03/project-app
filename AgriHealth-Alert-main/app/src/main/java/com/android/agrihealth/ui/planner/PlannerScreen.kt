package com.android.agrihealth.ui.planner

import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
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
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.ui.navigation.BottomNavigationMenu
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.navigation.Tab
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

// Todo: show year if different from current year
// Todo: scroll to first task of the day
// Todo: implement task overlapping
// Todo: show a line for current hour
// Todo: Fix report title clipping onto background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    userId: String = "",
    reportId: String? = null,
    navigationActions: NavigationActions? = null,
    plannerVM: PlannerViewModel = viewModel(),
) {

  val uiState by plannerVM.uiState.collectAsState()

  LaunchedEffect(userId, reportId) {
    plannerVM.setUserId(userId)
    plannerVM.loadReports()
    val report = plannerVM.setReportToSetTheDateFor(reportId)
    val date = report?.startTime?.toLocalDate() ?: LocalDate.now()
    plannerVM.setSelectedDate(date)
    plannerVM.setOriginalDate(date)
  }

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
        if (reportId == null) {
          BottomNavigationMenu(
              selectedTab = Tab.Planner,
              onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
              modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
        }
      },
      content = { pd ->
        Box(modifier = Modifier.padding(pd)) {
          Column(modifier = Modifier.padding(horizontal = 8.dp)) {
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
            WeeklyPager(
                onDateSelected = { date -> plannerVM.setSelectedDate(date) },
                startingDate = uiState.originalDate,
                dayReportMap = uiState.reports)
            Row(modifier = Modifier.fillMaxWidth()) {
              Text(
                  uiState.selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM")),
                  style = MaterialTheme.typography.titleLarge)
            }
            DailyScheduler(uiState.selectedDateReports) { it ->
              navigationActions?.navigateTo(Screen.ViewReport(it))
            }
          }
          if (reportId != null) {

            SetReportDateBox(
                modifier = Modifier.align(Alignment.BottomEnd),
                report = uiState.reportToSetTheDateFor,
                uiState.selectedDate,
                onSetReportDateClick = { plannerVM.editReportWithNewTime() },
                onTimeSelected = plannerVM::setReportTime,
                onDurationSelected = plannerVM::setReportDuration)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeeklyPager(
    onDateSelected: (LocalDate) -> Unit,
    startingDate: LocalDate,
    dayReportMap: Map<LocalDate?, List<Report>> = emptyMap(),
) {
  val mondayOfStartingWeek: LocalDate = startingDate.with(DayOfWeek.MONDAY)

  val maxPages = 1000

  val initialPage = maxPages / 2
  val pagerState =
      rememberPagerState(
          initialPage = initialPage, initialPageOffsetFraction = 0f, pageCount = { maxPages })

  LaunchedEffect(pagerState.currentPage) {
    val date =
        if (pagerState.currentPage == initialPage) startingDate
        else {
          val weekOffset = pagerState.currentPage - initialPage
          mondayOfStartingWeek.plusWeeks(weekOffset.toLong())
        }
    onDateSelected(date)
  }

  HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxWidth(),
      pageSpacing = 16.dp,
  ) { page ->

    // Calculate week offset relative to the center
    val weekOffset = page - (initialPage)
    val startOfWeek = mondayOfStartingWeek.plusWeeks(weekOffset.toLong())
    val week: List<LocalDate> = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      week.forEach { day ->
        DayCard(
            Modifier.weight(1f),
            day,
            dayReportMap.getOrDefault(day, emptyList()).map { it.status },
            onClick = { onDateSelected(it) })
      }
    }
  }
}

@Composable
fun DayCard(
    modifier: Modifier = Modifier,
    day: LocalDate,
    reportStatuses: List<ReportStatus>,
    onClick: (LocalDate) -> Unit = {}
) {
  Card(modifier = modifier.height(64.dp)) {
    Column(
        modifier = Modifier.fillMaxSize().clickable(onClick = { onClick(day) }).padding(4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              day.dayOfMonth.toString(),
              modifier = Modifier,
              style = MaterialTheme.typography.titleLarge)
          DotGrid(dots = reportStatuses)
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
        val columnNumber = (dayTagWidth / (dotSize + 2.dp)).toInt().coerceAtLeast(1)
        dots.chunked(columnNumber).forEach { rowDots ->
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
          val topOffset = report.startTime!!.hour.times(hourHeight.value)
          val taskHeight =
              report.duration.let {
                if (it == null) 30f
                else {
                  max((it.hour + (it.minute.div(60))).times(hourHeight.value), 30f)
                }
              }

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

@Composable
fun SetReportDateBox(
    modifier: Modifier = Modifier,
    report: Report?,
    selectedDate: LocalDate = LocalDate.now(),
    onSetReportDateClick: () -> Unit = {},
    onTimeSelected: (LocalTime) -> Unit = {},
    onDurationSelected: (LocalTime) -> Unit = {}
) {
  if (report == null) return
  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .background(
                  MaterialTheme.colorScheme.surfaceVariant,
              )
              .padding(8.dp),
      contentAlignment = Alignment.CenterStart) {
        val date = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM"))
        Column {
          Text(
              report.title,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$date at ", style = MaterialTheme.typography.bodyLarge)
            TimePickerBox(onTimeSelected = onTimeSelected)
            Text("   Duration: ")
            TimePickerBox(initialTime = LocalTime.of(1, 0), onTimeSelected = onDurationSelected)
            Spacer(Modifier.weight(2f))
            IconButton(onClick = onSetReportDateClick, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.Add, contentDescription = "Set Report Date")
            }
          }
        }
      }
}

@Composable
fun TimePickerBox(
    initialTime: LocalTime = LocalTime.now(),
    onTimeSelected: (LocalTime) -> Unit = {},
) {
  val context = LocalContext.current
  var selectedTime by remember { mutableStateOf(initialTime) }

  val timePickerDialog =
      TimePickerDialog(
          context,
          { _, hour: Int, minute: Int ->
            selectedTime = LocalTime.of(hour, minute)
            onTimeSelected(selectedTime)
          },
          initialTime.hour,
          initialTime.minute,
          true)

  Box(
      modifier =
          Modifier.background(
                  MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small)
              .clickable { timePickerDialog.show() }
              .padding(8.dp)) {
        Text(
            text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = MaterialTheme.typography.bodyLarge)
      }
}

@Preview
@Composable
fun PlannerScreenPreview() {
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
          startTime = LocalDate.now().atTime(0, 0),
          duration = LocalTime.of(0, 0))

  val previewReport2 =
      previewReport1.copy(
          id = "2", startTime = LocalDateTime.now().plusHours(2), duration = LocalTime.of(1, 0))
  val previewReport3 =
      previewReport1.copy(
          id = "3",
          startTime = LocalDateTime.now().plusHours(4),
          duration = LocalTime.of(2, 0),
          status = ReportStatus.RESOLVED)
  val previewReport4 =
      previewReport1.copy(
          id = "4", startTime = LocalDateTime.now().plusDays(1), duration = LocalTime.of(2, 0))

  val reportRepo =
      object : ReportRepository {
        private val reports: MutableList<Report> =
            mutableListOf(previewReport1, previewReport2, previewReport3, previewReport4)

        private var nextId = 0

        override fun getNewReportId(): String {
          return (nextId++).toString()
        }

        override suspend fun getAllReports(userId: String): List<Report> {
          return reports.filter { it.farmerId == userId || it.vetId == userId }
        }

        override suspend fun getReportsByFarmer(farmerId: String): List<Report> {
          return reports.filter { it.farmerId == farmerId }
        }

        override suspend fun getReportsByVet(vetId: String): List<Report> {
          return reports.filter { it.vetId == vetId }
        }

        override suspend fun getReportById(reportId: String): Report? {
          return reports.find { it.id == reportId }
              ?: throw NoSuchElementException("ReportRepositoryLocal: Report not found")
        }

        override suspend fun addReport(report: Report) {
          try {
            editReport(report.id, report)
          } catch (e: Exception) {
            reports.add(report)
          }
        }

        override suspend fun editReport(reportId: String, newReport: Report) {
          val index = reports.indexOfFirst { it.id == reportId }
          if (index != -1) {
            reports[index] = newReport
          } else {
            throw NoSuchElementException("ReportRepositoryLocal: Report not found")
          }
        }

        override suspend fun deleteReport(reportId: String) {
          val index = reports.indexOfFirst { it.id == reportId }
          if (index != -1) {
            reports.removeAt(index)
          } else {
            throw NoSuchElementException("ReportRepositoryLocal: Report not found")
          }
        }
      }
  val fakePlannerVM = PlannerViewModel(reportRepo)
  AgriHealthAppTheme { PlannerScreen(userId = "vet1", reportId = null, plannerVM = fakePlannerVM) }
}
