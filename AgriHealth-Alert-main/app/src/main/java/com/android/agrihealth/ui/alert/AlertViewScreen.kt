package com.android.agrihealth.ui.alert

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.agrihealth.ui.navigation.NavigationActions

object AlertViewScreenTestTags {
    const val SCREEN_CONTAINER = "AlertViewScreenContainer"
}

/**
 * Screen to display a single alert in detail.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertViewScreen(
    navigationActions: NavigationActions,
    viewModel: AlertViewModel,
    alertId: String = ""
) {
    LaunchedEffect(alertId) { viewModel.loadAlert(alertId) }

    val uiState by viewModel.uiState.collectAsState()
    val alert = uiState.alert ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(alert.title) },
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .fillMaxSize()
                    .testTag(AlertViewScreenTestTags.SCREEN_CONTAINER),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Description: ${alert.description}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Date: ${alert.outbreakDate}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Region: ${alert.region}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.loadPreviousAlert(alert.id) },
                        enabled = viewModel.hasPrevious(alert.id)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Alert")
                    }
                    IconButton(
                        onClick = { viewModel.loadNextAlert(alert.id) },
                        enabled = viewModel.hasNext(alert.id)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Alert")
                    }
                }
            }
        }
}