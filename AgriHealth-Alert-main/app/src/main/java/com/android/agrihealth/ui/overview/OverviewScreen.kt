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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.android.agrihealth.core.design.theme.statusColor
import com.android.agrihealth.data.model.alert.Alert
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.ui.common.AuthorName
import com.android.agrihealth.ui.common.OfficeName
import com.android.agrihealth.ui.navigation.BottomNavigationMenu
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.navigation.Tab
import kotlinx.coroutines.launch

// -- imports for preview --
/*
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.testutil.FakeOverviewViewModel
*/

object OverviewScreenTestTags {
  const val TOP_APP_BAR_TITLE = NavigationTestTags.TOP_BAR_TITLE
  const val ADD_REPORT_BUTTON = "addReportFab"
  const val LOGOUT_BUTTON = "logoutButton"
  const val SCREEN = "OverviewScreen"
  const val REPORT_ITEM = "reportItem"
  const val PROFILE_BUTTON = "ProfileButton"
  const val STATUS_DROPDOWN = "StatusFilterDropdown"
  const val OFFICE_ID_DROPDOWN = "OfficeIdFilterDropdown"
  const val FARMER_ID_DROPDOWN = "FarmerIdFilterDropdown"

  fun alertItemTag(page: Int) = "ALERT_ITEM_$page"
}

/**
 * Composable screen displaying the Overview UI. Shows latest alerts and a list of past reports.
 * Button for creating a new report will only be displayed for farmer accounts. For the list,
 * farmers can view only reports made by their own; vets can view all the reports.
 *
 * @param reports List of report to display kept only for backward compatibility and shouldn't be
 *   used
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OverviewScreen(
    userRole: UserRole,
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    user: User,
    overviewViewModel: OverviewViewModelContract,
    onAddReport: () -> Unit = {},
    onReportClick: (String) -> Unit = {},
    onAlertClick: (String) -> Unit = {},
    navigationActions: NavigationActions? = null
) {

  val uiState by overviewViewModel.uiState.collectAsState()
  val density = LocalDensity.current
  var lazySpace by remember { mutableStateOf(0.dp) }
  val minLazySpace = remember { 150.dp }

  LaunchedEffect(user) {
    overviewViewModel.loadReports(user)
    overviewViewModel.loadAlerts(user)
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

      // -- Main content area --
      content = { paddingValues ->
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
                    rememberPagerState(initialPage = 0, pageCount = { uiState.filteredAlerts.size })
                val coroutineScope = rememberCoroutineScope()
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                  if (uiState.filteredAlerts.isEmpty()) {
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
                          val alert = uiState.filteredAlerts[page]
                          AlertItem(
                              alert = alert,
                              isCentered = pagerState.currentPage == page,
                              onCenterClick = { onAlertClick(alert.id) },
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()) {
                      // -- Status filter --
                      DropdownMenuWrapper(
                          options = listOf(null) + ReportStatus.entries,
                          selectedOption = uiState.selectedStatus,
                          onOptionSelected = {
                            overviewViewModel.updateFiltersForReports(
                                it, uiState.selectedOffice, uiState.selectedFarmer)
                          },
                          modifier = Modifier.testTag(OverviewScreenTestTags.STATUS_DROPDOWN),
                          placeholder = "Filter by status",
                          labelProvider = { status -> status?.displayString() ?: "-" })
                      Spacer(modifier = Modifier.width(8.dp))
                      if (userRole == UserRole.FARMER) {
                        // -- OfficeId filter (only for farmer) --
                        DropdownMenuWrapper(
                            options = listOf(null) + uiState.officeOptions,
                            selectedOption = uiState.selectedOffice,
                            onOptionSelected = {
                              overviewViewModel.updateFiltersForReports(
                                  status = uiState.selectedStatus,
                                  officeId = it,
                                  farmerId = uiState.selectedFarmer)
                            },
                            modifier = Modifier.testTag(OverviewScreenTestTags.OFFICE_ID_DROPDOWN),
                            placeholder = "Filter by offices")
                      } else if (userRole == UserRole.VET) {
                        // -- FarmerId filter (only for vet) --
                        DropdownMenuWrapper(
                            options = listOf(null) + uiState.farmerOptions,
                            selectedOption = uiState.selectedFarmer,
                            onOptionSelected = {
                              overviewViewModel.updateFiltersForReports(
                                  status = uiState.selectedStatus,
                                  officeId = uiState.selectedOffice,
                                  farmerId = it)
                            },
                            modifier = Modifier.testTag(OverviewScreenTestTags.FARMER_ID_DROPDOWN),
                            placeholder = "Filter by farmers")
                      }
                    }

                LazyColumn(modifier = Modifier.height(maxOf(lazySpace, minLazySpace))) {
                  items(uiState.filteredReports) { report ->
                    ReportItem(
                        userRole = userRole,
                        report = report,
                        onClick = { onReportClick(report.id) },
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                  }
                }
              }
        }
      })
}

/**
 * Card displaying the latest alert information. Currently uses static mock data. Future
 * implementation will fetch alerts.
 */
@Composable
fun AlertItem(
    alert: Alert,
    isCentered: Boolean,
    onCenterClick: () -> Unit,
    onNotCenterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      onClick = {
        if (isCentered) {
          onCenterClick()
        } else {
          onNotCenterClick()
        }
      },
      modifier = modifier.width(350.dp),
      elevation = CardDefaults.cardElevation(4.dp)) {
        Column(
            modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
            }
      }
}

/** Composable displaying a simple dropdown menu for filtering or selecting options. */
@Composable
fun <T> DropdownMenuWrapper(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    labelProvider: (T?) -> String = { it?.toString() ?: "-" }
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
 * description, and status tag.
 */
@Composable
fun ReportItem(report: Report, onClick: () -> Unit, userRole: UserRole) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .testTag(OverviewScreenTestTags.REPORT_ITEM)
              .clickable { onClick() }
              .padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          Text(report.title, style = MaterialTheme.typography.titleSmall)

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Show full name and role, no label
            if (userRole == UserRole.VET) AuthorName(uid = report.farmerId)
            else OfficeName(uid = report.officeId, onClick = { /* TODO("add ViewOffice") */})
          }
          Text(
              text = report.description.let { if (it.length > 50) it.take(50) + "..." else it },
              style = MaterialTheme.typography.bodySmall,
              maxLines = 1)
        }
        StatusTag(report.status)
      }
}

/**
 * Composable displaying the status of a report as a colored tag. Color varies based on the report
 * status.
 */
@Composable
fun StatusTag(status: ReportStatus) {
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
