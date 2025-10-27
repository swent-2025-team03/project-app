package com.android.agrihealth.ui.report

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.UserRole

/**
 * Displays the detailed view of a single report. The UI dynamically adapts depending on the current
 * user's role (Farmer or Vet).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportViewScreen(
    navController: NavController,
    userRole: UserRole,
    viewModel: ReportViewModel,
    reportId: String = ""
) {
  LaunchedEffect(reportId) { viewModel.loadReport(reportId) }

  val uiState by viewModel.uiState.collectAsState()

  // --- Auto-change PENDING -> IN_PROGRESS for vets ---
  LaunchedEffect(userRole, uiState.report.status) {
    if (userRole == UserRole.VET && uiState.report.status == ReportStatus.PENDING) {
      viewModel.onStatusChange(ReportStatus.IN_PROGRESS)
    }
  }

  val report = uiState.report
  val answerText = uiState.answerText
  val selectedStatus = uiState.status
  val context = LocalContext.current

  var isEscalateDialogOpen by remember { mutableStateOf(false) }

  // ---- Helper: Color based on status ----
  @Composable
  fun statusColor(status: ReportStatus): Color {
    return when (status) {
      ReportStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
      ReportStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
      ReportStatus.RESOLVED -> MaterialTheme.colorScheme.secondaryContainer
      ReportStatus.ESCALATED -> MaterialTheme.colorScheme.error
    }
  }

  Scaffold(
      topBar = {
        // Top bar with back arrow and title/status
        TopAppBar(
            title = {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    Box(
                        modifier =
                            Modifier.background(
                                    color = statusColor(selectedStatus),
                                    shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("StatusBadgeBox")) {
                          Text(
                              text = selectedStatus.name.replace("_", " "),
                              style = MaterialTheme.typography.labelLarge,
                              color = MaterialTheme.colorScheme.onSurface,
                              modifier = Modifier.testTag("StatusBadgeText"))
                        }
                  }
            },
            navigationIcon = {
              IconButton(
                  onClick = { navController.popBackStack() },
                  modifier =
                      Modifier.testTag(
                          com.android.agrihealth.ui.navigation.NavigationTestTags.GO_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      }) { padding ->

        // Main scrollable content
        Column(
            modifier =
                Modifier.padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

              // ---- Farmer or Vet info line ----
              Text(
                  text =
                      if (userRole == UserRole.VET) "Farmer ID: ${report.farmerId}"
                      else "Vet ID: ${report.vetId}" ?: "Unassigned",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.testTag("roleInfoLine"))

              // ---- Photo ---- For now, I am skipping this part since I had trouble loading a
              // placeholder image
              /*Image(
                  imageVector = Icons.Filled.Image,
                  contentDescription = "Report photo",
                  modifier = Modifier
                      .fillMaxWidth()
                      .height(200.dp)
                      .background(MaterialTheme.colorScheme.surfaceVariant)
              )*/

              // ---- Description ----
              Text(text = report.description, style = MaterialTheme.typography.bodyLarge)

              // ---- Answer section ----
              Text(
                  text = "Answer:",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold)

              if (userRole == UserRole.FARMER) {
                // Farmer: read-only answer
                Text(
                    text = report.answer ?: "No answer yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              } else if (userRole == UserRole.VET) {
                // Vet: editable TextField
                OutlinedTextField(
                    value = answerText,
                    onValueChange = { viewModel.onAnswerChange(it) },
                    placeholder = { Text("Write your answer here...") },
                    modifier =
                        Modifier.fillMaxWidth().heightIn(min = 100.dp).testTag("AnswerField"))
              }

              // ---- Status dropdown (Vet only) ----
              if (userRole == UserRole.VET) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.testTag("StatusDropdownBox")) {
                      OutlinedTextField(
                          value = selectedStatus.name.replace("_", " "),
                          onValueChange = {},
                          label = { Text("Status") },
                          readOnly = true,
                          trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                          },
                          modifier =
                              Modifier.menuAnchor().fillMaxWidth().testTag("StatusDropdownField"))
                      ExposedDropdownMenu(
                          expanded = expanded,
                          onDismissRequest = { expanded = false },
                          modifier = Modifier.testTag("StatusDropdownMenu")) {
                            listOf(ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED).forEach { status
                              ->
                              DropdownMenuItem(
                                  text = { Text(status.name.replace("_", " ")) },
                                  onClick = {
                                    viewModel.onStatusChange(status)
                                    expanded = false
                                  },
                                  modifier = Modifier.testTag("StatusOption_${status.name}"))
                            }
                          }
                    }
              }

              // ---- Escalate button (Vet only) ----
              if (userRole == UserRole.VET && selectedStatus != ReportStatus.ESCALATED) {
                Button(
                    onClick = { isEscalateDialogOpen = true },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().testTag("EscalateButton")) {
                      Text("Escalate to Authorities")
                    }
              }

              // ---- Escalation confirmation dialog ----
              if (isEscalateDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isEscalateDialogOpen = false },
                    title = { Text("Confirm Escalation") },
                    text = {
                      Text("Are you sure you want to escalate this report to the authorities?")
                    },
                    confirmButton = {
                      TextButton(
                          onClick = {
                            viewModel.onEscalate()
                            isEscalateDialogOpen = false
                          }) {
                            Text("Yes")
                          }
                    },
                    dismissButton = {
                      TextButton(onClick = { isEscalateDialogOpen = false }) { Text("Cancel") }
                    })
              }

              // ---- Bottom row: Map + Save ----
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(
                        onClick = {
                          Toast.makeText(context, "Map not implemented yet", Toast.LENGTH_SHORT)
                              .show()
                        }) {
                          Text("View on Map")
                        }

                    // ---- Save Button (Vet only) ----
                    if (userRole == UserRole.VET) {
                      Button(onClick = { viewModel.onSave() }) { Text("Save") }
                    }
                  }
            }
      }
}
/*  If you want to use the preview, just decoment this block.
/**
 * Preview of the ReportViewScreen for both farmer and vet roles. Allows testing of layout and
 * colors directly in Android Studio.
 */
@Preview(showBackground = true, name = "Farmer View")
@Composable
fun PreviewReportViewFarmer() {
  MaterialTheme {
    val navController = rememberNavController()
    val viewModel = ReportViewModel()
    ReportViewScreen(
        navController = navController,
        userRole = UserRole.FARMER,
        viewModel = viewModel,
        reportId = "RPT001")
  }
}

@Preview(showBackground = true, name = "Vet View")
@Composable
fun PreviewReportViewVet() {
  MaterialTheme {
    val navController = rememberNavController()
    val viewModel = ReportViewModel()
    ReportViewScreen(
        navController = navController,
        userRole = UserRole.VET,
        viewModel = viewModel,
        reportId = "RPT001")
  }
}
*/
