package com.android.agrihealth.ui.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.ui.navigation.BottomNavigationMenu
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.navigation.Tab

// -- imports for preview --
/*
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
*/

object OverviewScreenTestTags {
  const val TOP_APP_BAR_TITLE = NavigationTestTags.TOP_BAR_TITLE
  const val ADD_REPORT_BUTTON = "addReportFab"
  const val LOGOUT_BUTTON = "logoutButton"
  const val SCREEN = "OverviewScreen"
  const val REPORT_ITEM = "reportItem"
  const val PROFILE_BUTTON = "ProfileButton"
  const val STATUS_DROPDOWN = "StatusFilterDropdown"
  const val VET_ID_DROPDOWN = "VetIdFilterDropdown"
  const val FARMER_ID_DROPDOWN = "FarmerIdFilterDropdown"
}

/**
 * Composable screen displaying the Overview UI. Shows latest alerts and a list of past reports.
 * Button for creating a new report will only be displayed for farmer accounts. For the list,
 * farmers can view only reports made by their own; vets can view all the reports.
 *
 * @param reports List of report to display kept only for backward compatibility and shouldn't be
 *   used
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    userRole: UserRole,
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    userId: String,
    overviewViewModel: OverviewViewModelContract,
    onAddReport: () -> Unit = {},
    onReportClick: (String) -> Unit = {},
    navigationActions: NavigationActions? = null
) {

  val uiState by overviewViewModel.uiState.collectAsState()

  LaunchedEffect(Unit) { overviewViewModel.loadReports(userRole, userId) }

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
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .testTag(OverviewScreenTestTags.SCREEN)) {
              // -- Latest alert section --
              Text("Latest News / Alerts", style = MaterialTheme.typography.headlineSmall)

              Spacer(modifier = Modifier.height(12.dp))
              LatestAlertCard()

              Spacer(modifier = Modifier.height(24.dp))

              // -- Create a new report buton --
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
                          overviewViewModel.updateFilters(
                              it, uiState.selectedVet, uiState.selectedFarmer)
                        },
                        modifier = Modifier.testTag(OverviewScreenTestTags.STATUS_DROPDOWN))
                    Spacer(modifier = Modifier.width(8.dp))
                    if (userRole == UserRole.FARMER) {
                      // -- VetId filter (only for farmer) --
                      DropdownMenuWrapper(
                          options = listOf(null) + uiState.vetOptions,
                          selectedOption = uiState.selectedVet,
                          onOptionSelected = {
                            overviewViewModel.updateFilters(
                                status = uiState.selectedStatus,
                                vetId = it,
                                farmerId = uiState.selectedFarmer)
                          },
                          modifier = Modifier.testTag(OverviewScreenTestTags.VET_ID_DROPDOWN))
                    } else if (userRole == UserRole.VET) {
                      // -- FarmerId filter (only for vet) --
                      DropdownMenuWrapper(
                          options = listOf(null) + uiState.farmerOptions,
                          selectedOption = uiState.selectedFarmer,
                          onOptionSelected = {
                            overviewViewModel.updateFilters(
                                status = uiState.selectedStatus,
                                vetId = uiState.selectedVet,
                                farmerId = it)
                          },
                          modifier = Modifier.testTag(OverviewScreenTestTags.FARMER_ID_DROPDOWN))
                    }
                  }

              LazyColumn(modifier = Modifier.weight(1f)) {
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
      })
}

/**
 * Card displaying the latest alert information. Currently uses static mock data. Future
 * implementation will fetch alerts.
 */
@Composable
fun LatestAlertCard() {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Using mock data for now, will implement the logics for LatestAlert later
      Text("Influenza Detected", style = MaterialTheme.typography.titleMedium)
      Text("Outbreak: 08/10/2025", style = MaterialTheme.typography.bodyMedium)
      Text(
          "Symptoms: Sudden drop in egg production, respiratory distress",
          style = MaterialTheme.typography.bodyMedium)
      Text("Region: Vaud, Switzerland", style = MaterialTheme.typography.bodyMedium)
      Spacer(modifier = Modifier.height(8.dp))
      // Will need to put outbreak photo
      /*Image(
      *    painter = painterResource(id = R.drawable.placeholder),
      *    contentDescription = "Outbreak photo",
      *    modifier = Modifier.height(120.dp).fillMaxWidth()
      )*/
    }
  }
}

/** Composable displaying a simple dropdown menu for filtering or selecting options. */
@Composable
fun <T> DropdownMenuWrapper(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  val displayText = selectedOption?.toString() ?: "All"

  Box {
    Button(onClick = { expanded = true }, modifier = modifier) {
      Text(displayText, style = MaterialTheme.typography.bodyMedium)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      options.forEach { option ->
        DropdownMenuItem(
            modifier = Modifier.testTag("OPTION_${option?.toString() ?: "All"}"),
            text = {
              Text(option?.toString() ?: "All", style = MaterialTheme.typography.bodyMedium)
            },
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
          when (userRole) {
            UserRole.FARMER ->
                Text("Vet ID: ${report.vetId}", style = MaterialTheme.typography.bodyMedium)
            UserRole.VET ->
                Text("Farmer ID: ${report.farmerId}", style = MaterialTheme.typography.bodyMedium)
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
  val color =
      when (status) {
        ReportStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
        ReportStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
        ReportStatus.RESOLVED -> MaterialTheme.colorScheme.secondaryContainer
        ReportStatus.SPAM -> MaterialTheme.colorScheme.error
      }
  Surface(
      color = color,
      shape = MaterialTheme.shapes.small,
      modifier = Modifier.padding(start = 8.dp)) {
        Text(
            text = status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall)
      }
}

/** Preview of the OverviewScreen with dummy data. Temporarily commented out */
/*
@Preview(showBackground = true)
@Composable
fun PreviewOverviewScreen() {
    val dummyReports = listOf(
        Report(
            id = "1",
            title = "Cow coughing",
            description = "Coughing and nasal discharge observed",
            photoUri = null,
            farmerId = "farmer_001",
            vetId = "vet_001",
            status = ReportStatus.IN_PROGRESS,
            answer = null,
            location = null),
        Report(
            id = "2",
            title = "Sheep limping",
            description = "Limping observed in the rear leg; mild swelling noted",
            photoUri = null,
            farmerId = "farmer_002",
            vetId = "vet_002",
            status = ReportStatus.PENDING,
            answer = null, location = null)
    )
    val dummyUiState = OverviewUIState(
        reports = dummyReports,
        filteredReports = dummyReports,
        selectedStatus = null,
        selectedVet = null,
        selectedFarmer = null,
        vetOptions = listOf("vet_001", "vet_002"),
        farmerOptions = listOf("farmer_001", "farmer_002")
    )
    val dummyViewModel = object : OverviewViewModelContract {
        override val uiState: StateFlow<OverviewUIState> = MutableStateFlow(dummyUiState)
        override fun loadReports(userRole: UserRole, userId: String) {}
        override fun updateFilters(status: ReportStatus?, vetId: String?, farmerId: String?) {}
        override fun signOut(credentialManager: CredentialManager) {}
    }
    OverviewScreen(
        userRole = UserRole.FARMER,
        userId = "farmer_001",
        overviewViewModel = dummyViewModel,
        onAddReport = {},
        onReportClick = {},
        navigationActions = null
    )
}
*/
