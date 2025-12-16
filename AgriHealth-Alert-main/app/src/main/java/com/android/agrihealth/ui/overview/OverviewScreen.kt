package com.android.agrihealth.ui.overview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.android.agrihealth.core.design.theme.statusColor
import com.android.agrihealth.core.design.theme.zoneColor
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.ui.common.layout.BottomNavigationMenu
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.common.layout.Tab
import com.android.agrihealth.ui.common.resolver.AuthorName
import com.android.agrihealth.ui.common.resolver.OfficeName
import com.android.agrihealth.ui.common.resolver.rememberOfficeName
import com.android.agrihealth.ui.common.resolver.rememberUserName
import com.android.agrihealth.ui.loading.LoadingOverlay
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.overview.OverviewScreenTestTags.ASSIGNED_VET_TAG
import kotlinx.coroutines.launch

// -- imports for preview --
/*
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.testhelpers.fakes.FakeOverviewViewModel
*/

/**
 * Composable screen displaying the Overview UI. Shows latest alerts and a list of past reports.
 * Button for creating a new report will only be displayed for farmer accounts. For the list,
 * farmers can view only reports made by their own; vets can view all the reports sent to them.
 */
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun OverviewScreen(
    userRole: UserRole,
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    user: User,
    overviewViewModel: OverviewViewModelContract,
    onAddReport: () -> Unit = {},
    onReportClick: (String) -> Unit = {},
    onAlertClick: (String) -> Unit = {},
    onLogin: () -> Unit = {},
    navigationActions: NavigationActions? = null
) {

  val uiState by overviewViewModel.uiState.collectAsState()
  val density = LocalDensity.current
  var lazySpace by remember { mutableStateOf(0.dp) }
  val minLazySpace = remember { 150.dp }
  val snackbarHostState = remember { SnackbarHostState() }
  var filtersExpanded by remember { mutableStateOf(false) }
  val isLoading = uiState.isAlertLoading || uiState.isReportLoading

  LaunchedEffect(user) {
    overviewViewModel.loadReports(user)
    overviewViewModel.loadAlerts(user)
    onLogin()
    overviewViewModel.updateFiltersForReports(
        status = FilterArg.Reset,
        officeId = FilterArg.Reset,
        farmerId = FilterArg.Reset,
        assignment = FilterArg.Reset)
  }
  Scaffold(
      // -- Top App Bar with logout icon --
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Overview",
                  style = MaterialTheme.typography.titleLarge,
                  modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions?.navigateTo(Screen.Profile) },
                  modifier = Modifier.testTag("ProfileButton")) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile")
                  }
            },
            actions = {
              IconButton(
                  onClick = {
                    overviewViewModel.signOut(credentialManager)
                    navigationActions?.navigateToAuthAndClear()
                  },
                  modifier = Modifier.testTag(OverviewScreenTestTags.LOGOUT_BUTTON)) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out")
                  }
            })
      },

      // -- Bottom navigation menu --
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Overview,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      snackbarHost = {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.imePadding())
      },

      // -- Main content area --
      content = { paddingValues ->
        LoadingOverlay(isLoading = isLoading) {
          val topPadding = paddingValues.calculateTopPadding()
          val bottomPadding = paddingValues.calculateBottomPadding()
          BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screen = this.maxHeight
            Column(
                modifier =
                    Modifier.padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .onSizeChanged { size ->
                          with(density) {
                            lazySpace =
                                screen - size.height.toDp() - topPadding - bottomPadding +
                                    maxOf(minLazySpace, lazySpace)
                          }
                        }
                        .testTag(OverviewScreenTestTags.SCREEN)
                        .verticalScroll(rememberScrollState())) {
                  // -- Latest alert section --
                  Text("Latest News / Alerts", style = MaterialTheme.typography.headlineSmall)

                  Spacer(modifier = Modifier.height(12.dp))

                  val pagerState =
                      rememberPagerState(initialPage = 0, pageCount = { uiState.sortedAlerts.size })
                  val coroutineScope = rememberCoroutineScope()
                  val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                  Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (uiState.sortedAlerts.isEmpty()) {
                      Text(
                          text = "No alerts available",
                          style = MaterialTheme.typography.bodyMedium,
                          modifier = Modifier.padding(16.dp))
                    } else {
                      HorizontalPager(
                          state = pagerState,
                          modifier = Modifier.fillMaxWidth(),
                          contentPadding = PaddingValues(horizontal = (screenWidth - 350.dp) / 2),
                          pageSpacing = 16.dp) { page ->
                            val alertUiState = uiState.sortedAlerts[page]
                            AlertItem(
                                alertUiState = alertUiState,
                                isCentered = pagerState.currentPage == page,
                                onCenterClick = { onAlertClick(alertUiState.alert.id) },
                                onNotCenterClick = {
                                  coroutineScope.launch { pagerState.animateScrollToPage(page) }
                                },
                                modifier =
                                    Modifier.width(350.dp)
                                        .testTag(OverviewScreenTestTags.alertItemTag(page)))
                          }
                    }
                  }
                  Spacer(modifier = Modifier.height(24.dp))

                  // -- Create a new report button --
                  // Display the button only if the user role is FARMER
                  if (userRole == UserRole.FARMER) {
                    Button(
                        onClick = onAddReport,
                        modifier =
                            Modifier.align(Alignment.CenterHorizontally)
                                .testTag(OverviewScreenTestTags.ADD_REPORT_BUTTON)) {
                          Text("Create a new report")
                        }
                  }

                  Spacer(modifier = Modifier.height(15.dp))
                  // -- Past reports list --
                  Text("Past Reports", style = MaterialTheme.typography.headlineSmall)
                  Spacer(modifier = Modifier.height(12.dp))

                  // Filter Chips + Logic
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.fillMaxWidth()) {
                        AssistChip(
                            onClick = { filtersExpanded = !filtersExpanded },
                            label = {
                              Text(
                                  text = if (filtersExpanded) "Hide filters" else "Filters",
                                  style = MaterialTheme.typography.bodyMedium)
                            },
                            modifier = Modifier.testTag(OverviewScreenTestTags.FILTERS_TOGGLE))

                        Spacer(modifier = Modifier.width(8.dp))

                        // Adds selected filters displayed even we the filters not displayed
                        val appliedSummaries =
                            buildList {
                                  uiState.selectedStatus?.let { add(it.displayString()) }
                                  uiState.selectedOffice?.let { add(rememberOfficeName(it)) }
                                  uiState.selectedFarmer?.let { add(rememberUserName(it)) }
                                  uiState.selectedAssignmentFilter?.let {
                                    add(
                                        when (it) {
                                          AssignmentFilter.ASSIGNED_TO_CURRENT_VET ->
                                              AssigneeFilterTexts.ASSIGNED_TO_ME
                                          AssignmentFilter.UNASSIGNED ->
                                              AssigneeFilterTexts.UNASSIGNED
                                          AssignmentFilter.ASSIGNED_TO_OTHERS ->
                                              AssigneeFilterTexts.ASSIGNED_TO_OTHERS
                                        })
                                  }
                                }
                                .joinToString(separator = " • ")
                                .takeIf { it.isNotEmpty() } ?: "No filters"

                        Text(
                            text = appliedSummaries,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                      }

                  if (filtersExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                          // Status filter (common to both roles)
                          DropdownMenuWrapper(
                              options = listOf(null) + ReportStatus.entries,
                              selectedOption = uiState.selectedStatus,
                              onOptionSelected = { newStatus ->
                                overviewViewModel.updateFiltersForReports(
                                    status = FilterArg.Value(newStatus))
                              },
                              modifier = Modifier.testTag(OverviewScreenTestTags.STATUS_DROPDOWN),
                              placeholder = "Filter by status",
                              labelProvider = { status -> status?.displayString() ?: "-" })

                          Spacer(modifier = Modifier.height(4.dp))

                          if (userRole == UserRole.FARMER) {
                            // OfficeId filter (only for farmer)
                            DropdownMenuWrapper(
                                options = listOf(null) + uiState.officeOptions,
                                selectedOption = uiState.selectedOffice,
                                onOptionSelected = { newOffice ->
                                  overviewViewModel.updateFiltersForReports(
                                      officeId = FilterArg.Value(newOffice))
                                },
                                modifier =
                                    Modifier.testTag(OverviewScreenTestTags.OFFICE_ID_DROPDOWN),
                                placeholder = "Filter by offices",
                                labelProvider = { officeId ->
                                  if (officeId == null) "-" else rememberOfficeName(officeId)
                                })
                          } else if (userRole == UserRole.VET) {
                            // FarmerId filter (only for vet)
                            DropdownMenuWrapper(
                                options = listOf(null) + uiState.farmerOptions,
                                selectedOption = uiState.selectedFarmer,
                                onOptionSelected = { newFarmer ->
                                  overviewViewModel.updateFiltersForReports(
                                      farmerId = FilterArg.Value(newFarmer))
                                },
                                modifier =
                                    Modifier.testTag(OverviewScreenTestTags.FARMER_ID_DROPDOWN),
                                placeholder = "Filter by farmers",
                                labelProvider = { farmerId ->
                                  if (farmerId == null) "-" else rememberUserName(farmerId)
                                })

                            Spacer(modifier = Modifier.height(4.dp))

                            // Assignment filter (only for vets)
                            DropdownMenuWrapper(
                                options =
                                    listOf<AssignmentFilter?>(null) +
                                        AssignmentFilter.entries.toTypedArray(),
                                selectedOption = uiState.selectedAssignmentFilter,
                                onOptionSelected = { newAssignment ->
                                  overviewViewModel.updateFiltersForReports(
                                      assignment = FilterArg.Value(newAssignment))
                                },
                                modifier = Modifier.testTag(OverviewScreenTestTags.ASSIGNEE_FILTER),
                                placeholder = "Filter by Assignee",
                                labelProvider = { assignment ->
                                  when (assignment) {
                                    null -> "-"
                                    AssignmentFilter.ASSIGNED_TO_CURRENT_VET ->
                                        AssigneeFilterTexts.ASSIGNED_TO_ME
                                    AssignmentFilter.UNASSIGNED -> AssigneeFilterTexts.UNASSIGNED
                                    AssignmentFilter.ASSIGNED_TO_OTHERS ->
                                        AssigneeFilterTexts.ASSIGNED_TO_OTHERS
                                  }
                                })
                          }
                        }
                  }

                  LazyColumn(modifier = Modifier.height(maxOf(lazySpace, minLazySpace))) {
                    items(uiState.filteredReports) { report ->
                      ReportItem(
                          userRole = userRole,
                          snackbarHostState = snackbarHostState,
                          report = report,
                          onClick = { onReportClick(report.id) },
                          navigationActions = navigationActions,
                          user = user)
                      HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    }
                  }
                }
          }
        }
      })
}

/** Composable displaying a simple dropdown menu for filtering or selecting options. */
@Composable
fun <T> DropdownMenuWrapper(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    labelProvider: @Composable (T?) -> String = { it?.toString() ?: "-" }
) {
  var expanded by remember { mutableStateOf(false) }
  val displayText = selectedOption?.let { labelProvider(it) } ?: placeholder

  Box {
    Button(onClick = { expanded = true }, modifier = modifier) {
      Text(displayText, style = MaterialTheme.typography.bodyMedium)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      options.forEach { option ->
        DropdownMenuItem(
            modifier = Modifier.testTag("OPTION_${option?.toString() ?: "All"}"),
            text = { Text(labelProvider(option), style = MaterialTheme.typography.bodyMedium) },
            onClick = {
              onOptionSelected(option)
              expanded = false
            })
      }
    }
  }
}

/**
 * Composable displaying a single report item in the list. Shows title, farmer ID, truncated
 * description, and status tag. For Vets, it also shows the assignedVet if there is one.
 */
@Composable
private fun ReportItem(
    report: Report,
    onClick: () -> Unit,
    userRole: UserRole,
    snackbarHostState: SnackbarHostState,
    navigationActions: NavigationActions? = null,
    user: User? = null
) {
  val coroutineScope = rememberCoroutineScope()

  Box(
      modifier =
          Modifier.fillMaxWidth().testTag(OverviewScreenTestTags.REPORT_ITEM).clickable {
            onClick()
          }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Column(modifier = Modifier.weight(1f)) {
                Text(report.title, style = MaterialTheme.typography.titleSmall)

                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (userRole == UserRole.VET) {
                    AuthorName(
                        uid = report.farmerId,
                        onClick = {
                          navigationActions?.navigateTo(Screen.ViewUser(report.farmerId))
                        })
                  } else {
                    OfficeName(
                        uid = report.officeId,
                        onClick = {
                          if (report.officeId.isNotBlank()) {
                            navigationActions?.navigateTo(Screen.ViewOffice(report.officeId))
                          } else {
                            coroutineScope.launch {
                              snackbarHostState.showSnackbar("This office no longer exists.")
                            }
                          }
                        })
                  }
                }

                Text(
                    text =
                        report.description.let { if (it.length > 50) it.take(50) + "..." else it },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1)
              }

              ReportStatusTag(report.status)
            }

        if (userRole == UserRole.VET && report.assignedVet != null) {
          Box(modifier = Modifier.align(Alignment.BottomEnd).offset(y = 4.dp)) {
            AssignedVetTag(
                vetId = report.assignedVet,
                isCurrentVet = report.assignedVet == user?.uid,
                navigationActions = navigationActions,
                snackbarHostState = snackbarHostState)
          }
        }
      }
}

/**
 * Shows the Name of the assignedVet or "Assigned to You" or nothing depending on the situation.
 * Only for Vets.
 */
@Composable
private fun AssignedVetTag(
    vetId: String,
    isCurrentVet: Boolean,
    navigationActions: NavigationActions?,
    snackbarHostState: SnackbarHostState
) {
  val coroutineScope = rememberCoroutineScope()
  val label =
      if (isCurrentVet) AssignedVetTagTexts.ASSIGNED_TO_CURRENT_VET else rememberUserName(vetId)

  val isClickable = !isCurrentVet

  Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color =
          if (isClickable) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.onBackground,
      modifier =
          Modifier.testTag(ASSIGNED_VET_TAG)
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .then(
                  if (isClickable) {
                    Modifier.clickable {
                      if (vetId.isNotBlank()) {
                        navigationActions?.navigateTo(Screen.ViewUser(vetId))
                      } else {
                        coroutineScope.launch {
                          snackbarHostState.showSnackbar("This vet no longer exists.")
                        }
                      }
                    }
                  } else Modifier.testTag(ASSIGNED_VET_TAG)))
}

/**
 * Card displaying the latest alert information. Currently uses static mock data. Future
 * implementation will fetch alerts.
 */
@Composable
fun AlertItem(
    alertUiState: AlertUiState,
    isCentered: Boolean,
    onCenterClick: () -> Unit,
    onNotCenterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  val alert = alertUiState.alert
  val distanceText = alertUiState.distanceToAddress?.let { "${it.toInt()} m" } ?: "Out of Zone"
  Card(
      onClick = {
        if (isCentered) {
          onCenterClick()
        } else {
          onNotCenterClick()
        }
      },
      modifier = modifier.width(350.dp).height(120.dp),
      elevation = CardDefaults.cardElevation(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${alert.region} • ${alert.outbreakDate}",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                  }
              AlertZoneTag(distanceText)
            }
      }
}

/**
 * Composable displaying the status of a report as a colored tag. Color varies based on the report
 * status.
 */
@Composable
fun ReportStatusTag(status: ReportStatus) {
  Surface(
      color = statusColor(status),
      shape = MaterialTheme.shapes.small,
      modifier = Modifier.padding(start = 8.dp)) {
        Text(
            text = status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall)
      }
}

/**
 * Composable displaying the distance information of an alert as a colored tag. Color varies based
 * on whether the user's address is in the zone or not.
 */
@Composable
fun AlertZoneTag(distanceText: String) {
  val isInside = !distanceText.contains("Out", ignoreCase = true)
  Surface(
      color = zoneColor(isInside),
      shape = MaterialTheme.shapes.small,
      modifier = Modifier.padding(start = 8.dp)) {
        Text(
            text = distanceText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White)
      }
}

object AssignedVetTagTexts {
  const val ASSIGNED_TO_CURRENT_VET = "Assigned to You"
}

object AssigneeFilterTexts {
  const val ASSIGNED_TO_ME = "Assigned to Me"
  const val UNASSIGNED = "Unassigned"
  const val ASSIGNED_TO_OTHERS = "Assigned to Others"
}

object OverviewScreenTestTags {
  const val TOP_APP_BAR_TITLE = NavigationTestTags.TOP_BAR_TITLE
  const val ADD_REPORT_BUTTON = "addReportFab"
  const val LOGOUT_BUTTON = "logoutButton"
  const val SCREEN = "OverviewScreen"
  const val REPORT_ITEM = "reportItem"
  const val PROFILE_BUTTON = "ProfileButton"
  const val FILTERS_TOGGLE = "FiltersToggle"
  const val STATUS_DROPDOWN = "StatusFilterDropdown"
  const val OFFICE_ID_DROPDOWN = "OfficeIdFilterDropdown"
  const val FARMER_ID_DROPDOWN = "FarmerIdFilterDropdown"
  const val ASSIGNEE_FILTER = "AssigneeFilter"
  const val ASSIGNED_VET_TAG = "AssignedVetTag"

  fun alertItemTag(page: Int) = "ALERT_ITEM_$page"
}

/*
/** Preview of the OverviewScreen with dummy data. Temporarily commented out */
@Preview(showBackground = true)
@Composable
fun PreviewOverviewScreen() {
    val fakeFarmer = Farmer(
        uid = "farmer_001",
        firstname = "Test",
        lastname = "Farmer",
        email = "test@farmer.com",
        address = Location(46.5191, 6.5668, "EPFL"),
        linkedOffices = listOf("off_001"),
        defaultOffice = "off_001"
    )
    val fakeViewModel = FakeOverviewViewModel(fakeFarmer)

    AgriHealthAppTheme {
        OverviewScreen(
            userRole = UserRole.FARMER,
            user = fakeFarmer,
            overviewViewModel = fakeViewModel,
            onAddReport = {},
            onReportClick = {},
            onAlertClick = {}
        )
    }
}
*/
