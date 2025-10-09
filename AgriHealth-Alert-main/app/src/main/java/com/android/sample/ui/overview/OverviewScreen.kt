package com.android.sample.ui.overview

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sample.data.model.UserRole
import com.android.sample.ui.navigation.BottomNavigationMenu
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.navigation.NavigationTestTags
import com.android.sample.data.model.Report
import com.android.sample.data.model.ReportStatus
import com.android.sample.ui.navigation.Tab

object OverviewScreenTestTags {
    const val ADD_REPORT_BUTTON = "addReportFab"
    const val LOGOUT_BUTTON = "logoutButton"
}

/**
 * Composable screen displaying the Overview UI.
 * Shows latest alerts and a list of past reports.
 * Button for creating a new report will only be displayed for farmer accounts.
 * For the list, farmers can view only reports made by their own;
 * vets can view all the reports.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    userRole: UserRole,
    reports: List<Report>,
    onAddReport: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null
) {
    Scaffold(
        // -- Top App Bar with logout icon --
        topBar = {
            TopAppBar(
                title = { Text("Overview", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = { onSignedOut() },
                        modifier = Modifier.testTag(OverviewScreenTestTags.LOGOUT_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },

        // -- Bottom navigation menu --
        bottomBar = {
            BottomNavigationMenu(
                selectedTab = Tab.Overview,
                onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
                modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
            )
        },

        // -- Main content area --
        content = { paddingValues ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
            ) {
                // -- Latest alert section --
                Text("Latest News / Alerts", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(12.dp))
                LatestAlertCard()

                Spacer(modifier = Modifier.height(24.dp))

                // -- Create a new report buton --
                //Display the button only if the user role is FARMER
                if (userRole == UserRole.FARMER) {
                    Button(
                        onClick = onAddReport,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .testTag(OverviewScreenTestTags.ADD_REPORT_BUTTON)
                    ) {
                        Text("Create a new report")
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                // -- Past reports list --
                Text("Past Reports", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn {
                    items(reports) { report ->
                        ReportItem(
                            report = report,
                            onClick = { onReportClick() }
                        )
                        Divider()
                    }
                }
            }
        }
    )
}

/**
 * Card displaying the latest alert information.
 * Currently uses static mock data. Future implementation will fetch alerts.
 */
@Composable
fun LatestAlertCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            //Using mock data for now, will implement the logics for LatestAlert later
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
 * Composable displaying a single report item in the list.
 * Shows title, farmer ID, truncated description, and status tag.
 */
@Composable
fun ReportItem(report: Report, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(report.title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text("Farmer ID: ${report.farmerId}")
            Text(
                text = report.description?.let {
                    if (it.length > 50) it.take(50) + "..." else it
                }?:"",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
        StatusTag(report.status)
    }
}

/**
 * Composable displaying the status of a report as a colored tag.
 * Color varies based on the report status.
 */
@Composable
fun StatusTag(status: ReportStatus) {
    val color = when (status) {
        ReportStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
        ReportStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
        ReportStatus.RESOLVED -> MaterialTheme.colorScheme.secondaryContainer
        ReportStatus.ESCALATED -> MaterialTheme.colorScheme.error
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

