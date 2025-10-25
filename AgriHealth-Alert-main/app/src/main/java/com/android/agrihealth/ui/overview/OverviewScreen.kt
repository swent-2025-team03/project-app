package com.android.agrihealth.ui.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.Report
import com.android.agrihealth.data.model.ReportStatus
import com.android.agrihealth.data.model.UserRole
import com.android.agrihealth.ui.navigation.BottomNavigationMenu
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Tab

object OverviewScreenTestTags {

  const val TOP_APP_BAR_TITLE = NavigationTestTags.TOP_BAR_TITLE
  const val ADD_REPORT_BUTTON = "addReportFab"
  const val LOGOUT_BUTTON = "logoutButton"
  const val SCREEN = "OverviewScreen"
  // Nouveau: tag commun pour chaque item de rapport (pour les tests E2E)
  const val REPORT_ITEM = "reportItem"
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
    overviewViewModel: OverviewViewModel = viewModel(),
    onAddReport: () -> Unit = {},
    onReportClick: (String) -> Unit = {},
    navigationActions: NavigationActions? = null,
    reports: List<Report> = overviewViewModel.uiState.collectAsState().value.reports,
) {

  val uiState by overviewViewModel.uiState.collectAsState()
  val reports = uiState.reports

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
            actions = {
              IconButton(
                  onClick = {
                    overviewViewModel.signOut(credentialManager)
                    navigationActions?.navigateToAuthAndClear()
                  },
                  modifier = Modifier.testTag(OverviewScreenTestTags.LOGOUT_BUTTON)) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
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
                    .testTag(OverviewScreenTestTags.SCREEN) // ← tag stable sur le conteneur racine
            ) {
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
              LazyColumn {
                items(reports, key = { it.id }) { report ->
                  // Ajout du testTag pour chaque item cliquable
                  ReportItem(
                      report = report,
                      onClick = { onReportClick(report.id) },
                  )
                  Divider()
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
      Text("Influenza Detected", fontSize = 20.sp, fontWeight = FontWeight.Bold)
      Text("Outbreak: 08/10/2025")
      Text("Symptoms: Sudden drop in egg production, respiratory distress")
      Text("Region: Vaud, Switzerland")
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

/**
 * Composable displaying a single report item in the list. Shows title, farmer ID, truncated
 * description, and status tag.
 */
@Composable
fun ReportItem(report: Report, onClick: () -> Unit) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .testTag(OverviewScreenTestTags.REPORT_ITEM)
              .clickable { onClick() }
              .padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          Text(report.title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
          Text("Farmer ID: ${report.farmerId}")
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
        ReportStatus.ESCALATED -> MaterialTheme.colorScheme.error
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

/**
 * @Preview(showBackground = true)
 * @Composable fun PreviewOverviewScreen() { val dummyNavController = rememberNavController() val
 *   dummyNavigationActions = NavigationActions(dummyNavController)
 *
 * val dummyReports = listOf( Report( id = "1", title = "Cow coughing", description = "Coughing and
 * nasal discharge observed", photoUri = null, farmerId = "farmer_001", vetId = null, status =
 * ReportStatus.IN_PROGRESS, answer = null, location = null), Report( id = "2", title = "Sheep
 * limping", description = "Limping observed in the rear leg; mild swelling noted", photoUri = null,
 * farmerId = "farmer_002", vetId = null, status = ReportStatus.PENDING, answer = null, location =
 * null))
 *
 * OverviewScreen( userRole = UserRole.FARMER, onAddReport = {}, onReportClick = {},
 * navigationActions = dummyNavigationActions, reports = dummyReports, ) }
 */
