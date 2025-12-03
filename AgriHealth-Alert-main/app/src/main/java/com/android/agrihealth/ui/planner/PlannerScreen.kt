package com.android.agrihealth.ui.planner

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.ui.navigation.BottomNavigationMenu
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.navigation.Tab
import java.lang.IllegalArgumentException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

object PlannerScreenTestTags {
  const val SCREEN = "plannerScreen"
  const val WEEK_NUMBER = "weekNumber"
  const val WEEK_HEADER = "weekHeader"
  const val SELECTED_DATE = "selectedDate"
  const val WEEKLY_PAGER = "weeklyPager"
  const val DAILY_SCHEDULER = "dailyScheduler"
  const val SET_REPORT_DATE_BOX = "setReportDateBox"
  const val SET_REPORT_DATE_BUTTON = "setReportDateButton"
  const val UNSAVED_ALERT_BOX = "unsavedAlertBoxPlanner"
  const val UNSAVED_ALERT_BOX_CANCEL = "unsavedAlertBoxPlannerCancelButton"
  const val UNSAVED_ALERT_BOX_GO_BACK = "unsavedAlertBoxPlannerGoBackButton"

  fun dayCardTag(day: LocalDate): String = "dayCard_${day}"

  fun reportCardTag(reportId: String): String = "reportCard_$reportId"
}

// Todo: implement task overlapping

/**
 * Planner Screen lets the user see their Reports ordered by dates.
 *
 * @param user the user currently using the app, used to fetch the reports
 * @param reportId the id of the report that the user is currently setting a date for
 * @param goBack function called when top left back arrow is clicked
 * @param tabClicked called with tab.destination when a tab of the bottom bar is clicked
 * @param reportClicked called with reportId when a report card is clicked
 * @param plannerVM the PlannerViewModel instance used in the Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    user: User? = null,
    reportId: String? = null,
    goBack: () -> Unit = {},
    tabClicked: (Screen) -> Unit = {},
    reportClicked: (String) -> Unit = {},
    plannerVM: PlannerViewModel = viewModel(),
) {

  val uiState by plannerVM.uiState.collectAsState()
  val user = user ?: throw IllegalArgumentException("User cannot be null for PlannerScreen")

  LaunchedEffect(user, reportId) {
    plannerVM.setUser(user)
    plannerVM.loadReports()
    val report = plannerVM.setReportToSetTheDateFor(reportId)
    val date: LocalDateTime = report?.startTime ?: LocalDateTime.now()
    plannerVM.setSelectedDate(date.toLocalDate())
    plannerVM.setOriginalDate(date.toLocalDate())
    plannerVM.setReportTime(date.toLocalTime())
    plannerVM.setReportDuration(report?.duration ?: LocalTime.of(1, 0))
  }

  BackHandler {
    if (plannerVM.isReportDateSet()) {
      goBack()
    } else {
      if (uiState.isUnsavedAlertShowing) {
        goBack()
      } else {
        plannerVM.setIsUnsavedAlertShowing(true)
      }
    }
  }

  if (uiState.isUnsavedAlertShowing) {
    UnsavedChangesAlert(
        onGoBack = {
          plannerVM.setIsUnsavedAlertShowing(false)
          goBack()
        },
        onStay = { plannerVM.setIsUnsavedAlertShowing(false) })
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
                  onClick = {
                    if (plannerVM.isReportDateSet()) {
                      goBack()
                    } else {
                      plannerVM.setIsUnsavedAlertShowing(true)
                    }
                  },
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
              onTabSelected = { tab -> tabClicked(tab.destination) },
              modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
        }
      },
      content = { pd ->
        Box(modifier = Modifier.padding(pd).testTag(PlannerScreenTestTags.SCREEN)) {
          Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                  "Week ${uiState.selectedWeekNumber}",
                  style = MaterialTheme.typography.titleMedium,
                  modifier = Modifier.testTag(PlannerScreenTestTags.WEEK_NUMBER))
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
                  if (uiState.selectedDate.year != LocalDate.now().year) {
                    uiState.selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                  } else {
                    uiState.selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM"))
                  },
                  style = MaterialTheme.typography.titleLarge,
                  modifier = Modifier.testTag(PlannerScreenTestTags.SELECTED_DATE))
            }
            DailyScheduler(
                uiState.selectedDateReports, reportId, uiState.selectedDate == LocalDate.now()) { it
                  ->
                  reportClicked(it)
                }
          }
          if (reportId != null) {

            SetReportDateBox(
                modifier = Modifier.align(Alignment.BottomEnd),
                report = uiState.reportToSetTheDateFor,
                selectedDate = uiState.selectedDate,
                initialTime = uiState.setTime,
                initialDuration = uiState.setDuration,
                onSetReportDateClick = { plannerVM.editReportWithNewTime() },
                onTimeSelected = plannerVM::setReportTime,
                onDurationSelected = plannerVM::setReportDuration)
          }
        }
      })
}

/** AlertDialog shown if the user exits the screen without setting a date. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnsavedChangesAlert(onGoBack: () -> Unit, onStay: () -> Unit) {
  AlertDialog(
      modifier = Modifier.testTag(PlannerScreenTestTags.UNSAVED_ALERT_BOX),
      onDismissRequest = onStay,
      title = { Text("Date Not Assigned") },
      text = { Text("You did not assign a date.\nGo back anyway?") },
      confirmButton = {
        TextButton(
            modifier = Modifier.testTag(PlannerScreenTestTags.UNSAVED_ALERT_BOX_GO_BACK),
            onClick = onGoBack) {
              Text("Go Back")
            }
      },
      dismissButton = {
        TextButton(
            modifier = Modifier.testTag(PlannerScreenTestTags.UNSAVED_ALERT_BOX_CANCEL),
            onClick = onStay) {
              Text("Set the Date")
            }
      })
}

/**
 * Text showing "24 - 30 Nov" or "29 Nov - 4 Dec" shows both month only if they are different
 *
 * @param start the date of the first part of the Text before " - "
 * @param end the date of the second part of the Text after " - "
 */
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

  Text(text, modifier = Modifier.testTag(PlannerScreenTestTags.WEEK_HEADER))
}

/**
 * Scrollable Pager showing the days of one week, scroll right for next week, left for past week.
 * Can be clicked to select a different week day.
 *
 * @param onDateSelected called when a DayCard is clicked with LocalDate the date of the DayCard
 * @param startingDate the date in the middle of the Pager shown first.
 * @param dayReportMap used to show DotGrid on the DayCards
 */
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
      modifier = Modifier.fillMaxWidth().testTag(PlannerScreenTestTags.WEEKLY_PAGER),
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

/**
 * Represent a day in the week and shows a list of point representing the tasks
 *
 * @param modifier the outside composable modifier
 * @param day the LocalDate this Card represents
 * @param reportStatuses list of ReportStatus to show on the card as dots
 * @param onClick called when a card is clicked, meant to be used to change the date to LocalDate
 */
@Composable
fun DayCard(
    modifier: Modifier = Modifier,
    day: LocalDate,
    reportStatuses: List<ReportStatus>,
    onClick: (LocalDate) -> Unit = {}
) {
  Card(
      modifier = modifier.height(64.dp).testTag(PlannerScreenTestTags.dayCardTag(day)),
      onClick = { onClick(day) }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
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

/**
 * The Grid of dots shown in the DayCard
 *
 * @param dots list of ReportStatus to show on the card as dots
 * @param dotSize the size of each dot
 */
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

/**
 * The Scheduler containing the hour Scale and the Daily task representing the Reports
 *
 * @param reports the list of reports to show on the Scheduler
 * @param reportId the id of the report to prioritize auto scroll to.
 * @param navigateToReport called when a report is clicked with the report.id
 */
@Composable
fun DailyScheduler(
    reports: List<Report>,
    reportId: String? = null,
    showTimeLine: Boolean = false,
    navigateToReport: (String) -> Unit = {}
) {
  val scrollState = rememberScrollState(0)

  val hourHeight = 60.dp
  val density = LocalDensity.current

  LaunchedEffect(reports) {
    if (reports.any { it.startTime != null }) {
      val reportToEdit = reports.find { it.id == reportId }
      val first = reportToEdit ?: reports.minBy { it.startTime?.hour ?: 24 }

      val offsetPx = with(density) { (first.startTime!!.hour * hourHeight.toPx()).toInt() }

      scrollState.animateScrollTo(
          offsetPx, animationSpec = tween(800, easing = LinearOutSlowInEasing))
    }
  }
  Row(
      modifier =
          Modifier.fillMaxSize()
              .verticalScroll(scrollState)
              .testTag(PlannerScreenTestTags.DAILY_SCHEDULER)) {
        HourScale(hourHeight = hourHeight)
        Box(modifier = Modifier.weight(1f)) {
          DailyTasks(
              reports = reports,
              hourHeight = hourHeight,
              showTimeLine = showTimeLine,
              navigateToReport = navigateToReport)
        }
      }
}

/**
 * Line of hour shown on the left of the screen
 *
 * @param hourHeight how much height used to represent each hours
 */
@Composable
fun HourScale(hourHeight: Dp = 60.dp) {
  Column {
    Box(modifier = Modifier.height(hourHeight / 2))
    for (hour in 1..23) {
      Box(
          modifier = Modifier.height(hourHeight)
          // .fillMaxWidth()
          ,
          contentAlignment = Alignment.Center) {
            Text(text = "$hour:00", modifier = Modifier.padding(4.dp))
          }
    }
    Box(modifier = Modifier.height(hourHeight / 2))
  }
}

/**
 * The Task list part of the Scheduler, shows tasks based on their startTime with height depending
 * on their durations
 *
 * @param reports the reports to show on the scheduler
 * @param hourHeight the height attributed to each hours
 * @param navigateToReport called with report.id when a report is clicked
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DailyTasks(
    reports: List<Report>,
    hourHeight: Dp = 60.dp,
    showTimeLine: Boolean = false,
    navigateToReport: (String) -> Unit = {}
) {
  val minReportDuration = 30f
  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    Log.d("Planner Screen", "maxWidth: $maxWidth, maxHeight $maxHeight")
    // Hour lines
    for (hour in 0..24) {
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .height(2.dp)
                  .offset(y = hourToOffset(hour.toFloat(), hourHeight))
                  .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.TopStart) {}
    }
    val items =
        reports
            .filter { it.startTime != null }
            .map {
              val start = it.startTime!!.toLocalTime()
              val durationMinutes = (it.duration?.toSecondOfDay()?.div(60)) ?: minReportDuration
              val end = start.plusMinutes(durationMinutes.toLong())
              ReportLayoutItem(it, start, end)
            }
            .sortedBy { it.start }

    for (layoutItems in clusterEvents(items)) {
      assignLanes(layoutItems)
    }
    items.forEach { (report, startTime, _, lane, totalLane) ->
      val taskHeight =
          report.duration
              .let {
                if (it == null) 30f
                else {
                  max(localTimeToOffset(it, hourHeight).value, minReportDuration)
                }
              }
              .dp
      val topOffset = localTimeToOffset(startTime, hourHeight)
      val laneWidth = maxWidth / totalLane
      val leftOffset = laneWidth * lane
      ReportCard(report, taskHeight, topOffset, leftOffset, laneWidth, navigateToReport)
    }
    if (showTimeLine) {
      CurrentTimeLine(hourHeight)
    }
  }
}

/**
 * Coded using chatGPT 5.0 data class representing a ReportCard
 *
 * @param report The Report represented by the LayoutItem
 * @param start the startTime of the report
 * @param end the end time of the report found using startTime and duration
 * @param lane the lane associated with the Layout item.
 * @param totalLanes the minimum amount of lanes need for the cluster this report is part of.
 */
data class ReportLayoutItem(
    val report: Report,
    val start: LocalTime,
    val end: LocalTime,
    var lane: Int = 0,
    var totalLanes: Int = 1
)

/**
 * Coded using ChatGPT 5.0 cluster events into group of overlapping clusters. This assures that a
 * cluster has minimum totalLanes after assignLane
 *
 * @param events list of ReportLayoutItem to split into cluster
 * @see ReportLayoutItem
 * @see assignLanes
 */
fun clusterEvents(events: List<ReportLayoutItem>): List<List<ReportLayoutItem>> {
  if (events.isEmpty()) return emptyList()

  val sorted = events.sortedBy { it.start }
  val clusters = mutableListOf<MutableList<ReportLayoutItem>>()

  var currentCluster = mutableListOf(sorted[0])
  var currentEnd = sorted[0].end

  for (i in 1 until sorted.size) {
    val item = sorted[i]

    // If event starts before or exactly at current cluster end → same cluster
    if (item.start <= currentEnd) {
      currentCluster.add(item)
      if (item.end > currentEnd) currentEnd = item.end
    } else {
      // New cluster
      clusters.add(currentCluster)
      currentCluster = mutableListOf(item)
      currentEnd = item.end
    }
  }

  clusters.add(currentCluster)
  return clusters
}

/**
 * Coded using chatGPT 5.0 Split a cluster of overlapping Report to different lane to avoid
 * overlapping
 *
 * @param cluster list of ReportLayout item to attribute to lanes
 */
fun assignLanes(cluster: List<ReportLayoutItem>) {
  val lanes: MutableList<MutableList<ReportLayoutItem>> = mutableListOf()

  fun overlaps(a: ReportLayoutItem, b: ReportLayoutItem) = a.start < b.end && b.start < a.end

  for (event in cluster.sortedBy { it.start }) {
    var laneIndex = -1

    // find earliest lane that is free before this event starts
    for (i in lanes.indices) {
      if (lanes[i].isEmpty() || !overlaps(lanes[i].last(), event)) {
        laneIndex = i
        break
      }
    }

    if (laneIndex == -1) {
      // Need a new lane
      laneIndex = lanes.size
      lanes.add(mutableListOf())
    }

    lanes[laneIndex].add(event)
    event.lane = laneIndex
  }

  // set width data
  val maxLanes = lanes.size
  cluster.forEach { it.totalLanes = maxLanes }
}

/**
 * Composable showing a report with its title and location.
 *
 * @param report the Report this composable represents
 * @param taskHeight the height of the composable.
 * @param topOffset the offset to the top of the master composable
 * @param leftOffset the offset to the left of the master composable
 * @param width the width of the composable.
 * @param navigateToReport function executed with report.id when clicking the composable.
 */
@Composable
fun ReportCard(
    report: Report,
    taskHeight: Dp,
    topOffset: Dp,
    leftOffset: Dp,
    width: Dp,
    navigateToReport: (String) -> Unit
) {
  Column(
      modifier =
          Modifier.width(width)
              .height(taskHeight)
              .offset(x = leftOffset, y = topOffset)
              .padding(horizontal = 0.dp)
              .background(color = statusColor(report.status), shape = MaterialTheme.shapes.medium)
              .clickable { navigateToReport(report.id) }
              .testTag(PlannerScreenTestTags.reportCardTag(report.id)),
      verticalArrangement = Arrangement.Top) {
        Text(
            report.title,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            color = onStatusColor(report.status),
            overflow = TextOverflow.Ellipsis,
            maxLines = if (taskHeight >= 90.dp) 2 else 1)
        if (taskHeight >= 54.dp)
            report.location?.name?.let {
              Text(
                  it,
                  modifier = Modifier.weight(1f, fill = false).padding(horizontal = 8.dp),
                  style = MaterialTheme.typography.bodyMedium,
                  color = onStatusColor(report.status),
                  maxLines = 1)
            }
      }
}

/**
 * Time Line Display on the Scheduler at the current time.
 *
 * @param hourHeight size in Dp of each hour used to calculate the offset
 */
@Composable
fun CurrentTimeLine(hourHeight: Dp) {
  val now = LocalTime.now()
  val offset = localTimeToOffset(now, hourHeight)
  Row(
      modifier = Modifier.fillMaxWidth().offset(y = offset),
      verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier.size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape))
        Box(
            modifier =
                Modifier.fillMaxWidth().height(3.dp).background(MaterialTheme.colorScheme.primary),
        )
      }
}

fun localTimeToOffset(time: LocalTime, hourHeight: Dp): Dp {
  return hourToOffset((time.toSecondOfDay() / (60.0 * 60.0).toFloat()), hourHeight)
}

fun hourToOffset(hour: Float, hourHeight: Dp): Dp {
  return (hour * hourHeight.value).dp
}

/**
 * Box shown at the bottom of the screen when the Planner is called to set the date of a report
 *
 * @param modifier the modifier of the outside Box
 * @param report The report whose date is being set
 * @param selectedDate the date selected using the WeeklyPager
 * @param initialTime the initial time shown on the set startTime button
 * @param initialDuration the initial time shown on the set duration button
 * @param onSetReportDateClick called when the user clicks the + icon, meant to be called once the
 *   user hase chosen a date and duration
 * @param onTimeSelected called when the user is done with the time picker for the startTime with
 *   the time chosen
 * @param onDurationSelected called when the user is done with the time picker for the duration with
 *   the time chosen
 */
@Composable
fun SetReportDateBox(
    modifier: Modifier = Modifier,
    report: Report?,
    selectedDate: LocalDate = LocalDate.now(),
    initialTime: LocalTime,
    initialDuration: LocalTime,
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
              .padding(8.dp)
              .testTag(PlannerScreenTestTags.SET_REPORT_DATE_BOX),
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
            TimePickerBox(initialTime = initialTime, onTimeSelected = onTimeSelected)
            Text("   Duration: ")
            TimePickerBox(initialTime = initialDuration, onTimeSelected = onDurationSelected)
            Spacer(Modifier.weight(2f))
            IconButton(
                onClick = onSetReportDateClick,
                modifier =
                    Modifier.weight(1f).testTag(PlannerScreenTestTags.SET_REPORT_DATE_BUTTON)) {
                  Icon(Icons.Default.Add, contentDescription = "Set Report Date")
                }
          }
        }
      }
}

/**
 * Box represented as a button with the selected time as text, opens a TimePicker Dialog when
 * clicked
 *
 * @param initialTime the time initially shown on the button
 * @param onTimeSelected called when the user selects a time, with the selected time as parameter
 */
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
  val startOfDay = LocalDate.now().atTime(0, 0)
  val previewReport1 =
      Report(
          id = "1",
          title = "Checkup with Farmer John",
          description = "Regular health checkup for the cattle.",
          questionForms = emptyList(),
          photoUri = null,
          farmerId = "farmer1",
          officeId = "vet1",
          status = ReportStatus.IN_PROGRESS,
          answer = null,
          location = Location(latitude = 12.34, longitude = 56.78, name = "Farmhouse A"),
          startTime = startOfDay.plusHours(2).plusMinutes(30),
          duration = LocalTime.of(0, 0))

  val previewReport2 =
      previewReport1.copy(
          id = "2", startTime = startOfDay.plusHours(2), duration = LocalTime.of(1, 0))
  val previewReport3 =
      previewReport1.copy(
          id = "3",
          startTime = startOfDay.plusHours(2),
          duration = LocalTime.of(2, 0),
          status = ReportStatus.RESOLVED)
  val previewReport4 =
      previewReport1.copy(
          id = "4",
          status = ReportStatus.PENDING,
          startTime = startOfDay.plusHours(3).plusMinutes(45),
          duration = LocalTime.of(1, 30))

  val previewReport5 =
      previewReport1.copy(
          id = "5",
          startTime = startOfDay.plusHours(5).plusMinutes(45),
          duration = LocalTime.of(3, 0))

  val user =
      Vet(
          uid = "vet1",
          firstname = "test",
          lastname = "test",
          email = "test",
          address = null,
          validCodes = emptyList(),
          officeId = "vet1",
          isGoogleAccount = false,
          description = "test",
          collected = false)

  val reportRepo =
      object : ReportRepository {
        private val reports: MutableList<Report> =
            mutableListOf(
                previewReport1, previewReport2, previewReport3, previewReport4, previewReport5)

        private var nextId = 0

        override fun getNewReportId(): String {
          return (nextId++).toString()
        }

        override suspend fun getAllReports(userId: String): List<Report> {
          return reports.filter { it.farmerId == userId || it.officeId == userId }
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
  AgriHealthAppTheme { PlannerScreen(user = user, reportId = null, plannerVM = fakePlannerVM) }
}
