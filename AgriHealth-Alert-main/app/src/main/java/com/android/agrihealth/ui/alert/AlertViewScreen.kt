package com.android.agrihealth.ui.alert

// -- imports for preview --

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.utils.maxTitleCharsForScreen

object AlertViewScreenTestTags {
  const val ALERT_FULL_TITLE = "alertFullTitle"
  const val ALERT_DESCRIPTION = "alertDescription"
  const val ALERT_DATE = "alertDate"
  const val ALERT_REGION = "alertRegion"
  const val PREVIOUS_ALERT_ARROW = "previousAlertArrow"
  const val NEXT_ALERT_ARROW = "nextAlertArrow"
  const val VIEW_ON_MAP = "viewOnMapButton"

  fun containerTag(index: Int) = "SCREEN_CONTAINER_$index"
}

/**
 * Displays detailed information for a single alert.
 *
 * Loads the alert for the given [alertId], shows its main fields, and provides navigation to the
 * previous/next alert. Also includes a placeholder button for future "View on Map" functionality.
 *
 * @param navigationActions Navigation handler for back navigation.
 * @param viewModel ViewModel that provides alert data and navigation logic.
 * @param alertId ID of the alert to load and display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertViewScreen(
    navigationActions: NavigationActions,
    viewModel: AlertViewModel,
    alertId: String = ""
) {
  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(alertId) { viewModel.loadAlert(alertId) }

  val uiState by viewModel.uiState.collectAsState()
  val alert = uiState.alert ?: return

  LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let { message -> snackbarHostState.showSnackbar(message) }
  }

  Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = alert.title,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis)
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
          val alertIndex by viewModel.currentAlertIndex.collectAsState()
          Column(
              modifier =
                  Modifier.verticalScroll(rememberScrollState())
                      .padding(16.dp)
                      .fillMaxSize()
                      .testTag(AlertViewScreenTestTags.containerTag(alertIndex))
                      .padding(bottom = 64.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // --- Full title (only if too long) ---
                val maxTitleChars = maxTitleCharsForScreen()
                val showFullTitleInBody = alert.title.length > maxTitleChars
                if (showFullTitleInBody) {
                  Text(
                      text = "Title: ${alert.title}",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.testTag(AlertViewScreenTestTags.ALERT_FULL_TITLE))
                }
                Text(
                    text = "Description: ${alert.description}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag(AlertViewScreenTestTags.ALERT_DESCRIPTION))
                Text(
                    text = "Date: ${alert.outbreakDate}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag(AlertViewScreenTestTags.ALERT_DATE))
                Text(
                    text = "Region: ${alert.region}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag(AlertViewScreenTestTags.ALERT_REGION))

                Spacer(modifier = Modifier.height(32.dp))
              }
          // --- View on Map Button ---
          OutlinedButton(
              modifier =
                  Modifier.fillMaxWidth()
                      .align(Alignment.BottomCenter)
                      .padding(bottom = 16.dp)
                      .testTag(AlertViewScreenTestTags.VIEW_ON_MAP),
              onClick = { /*TODO: implement View on Map using polygon when available */}) {
                Text("View on Map")
              }
          // --- Navigation arrows (previous / next) ---
          Row(
              modifier =
                  Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.loadPreviousAlert(alert.id) },
                    enabled = viewModel.hasPrevious(alert.id),
                    modifier = Modifier.testTag(AlertViewScreenTestTags.PREVIOUS_ALERT_ARROW)) {
                      Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Alert")
                    }
                IconButton(
                    onClick = { viewModel.loadNextAlert(alert.id) },
                    enabled = viewModel.hasNext(alert.id),
                    modifier = Modifier.testTag(AlertViewScreenTestTags.NEXT_ALERT_ARROW)) {
                      Icon(Icons.Default.ChevronRight, contentDescription = "Next Alert")
                    }
              }
        }
      }
}

/*
@Preview(showBackground = true)
@Composable
fun AlertViewScreenPreview() {
    val fakeRepo = com.android.agrihealth.testutil.FakeAlertRepository()
    val viewModel = AlertViewModel(fakeRepo)

    val navController = androidx.navigation.compose.rememberNavController()
    LaunchedEffect(Unit) {
        viewModel.loadAlert("1")
    }

    AlertViewScreen(
        navigationActions = NavigationActions(navController),
        viewModel = viewModel,
        alertId = "1"
    )
}
*/
