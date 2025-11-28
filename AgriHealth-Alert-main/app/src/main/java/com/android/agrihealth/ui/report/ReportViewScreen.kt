package com.android.agrihealth.ui.report

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.agrihealth.core.design.theme.StatusColors
import com.android.agrihealth.core.design.theme.statusColor
import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.QuestionForm
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.ui.common.AuthorName
import com.android.agrihealth.ui.common.OfficeName
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.utils.maxTitleCharsForScreen
import java.time.format.DateTimeFormatter

object ReportViewScreenTestTags {
  const val STATUS_BADGE_BOX = "StatusBadgeBox"
  const val STATUS_BADGE_TEXT = "StatusBadgeText"
  const val ROLE_INFO_LINE = "roleInfoLine"
  const val ANSWER_FIELD = "AnswerField"
  const val STATUS_DROPDOWN_BOX = "StatusDropdownBox"
  const val STATUS_DROPDOWN_FIELD = "StatusDropdownField"
  const val STATUS_DROPDOWN_MENU = "StatusDropdownMenu"
  const val SPAM_BUTTON = "SpamButton"
  const val VIEW_ON_MAP = "viewReportOnMap"
  const val SAVE_BUTTON = "SaveButton"
  const val SCROLL_CONTAINER = "ReportViewScrollContainer"
  const val UNSAVED_ALERT_BOX = "UnsavedChangesAlertBox"
  const val UNSAVED_ALERT_BOX_DISCARD = "UnsavedChangesAlertDiscardButton"
  const val UNSAVED_ALERT_BOX_CANCEL = "UnsavedChangesAlertCancelButton"

  fun getTagForStatusOption(statusName: String): String = "StatusOption_$statusName"
}

@Composable
private fun QuestionItem(
    question: QuestionForm,
    onAnswerChange: (QuestionForm) -> Unit = {},
    enabled: Boolean = false
) {
  when (question) {
    is OpenQuestion -> OpenQuestionItem(question, { onAnswerChange(it) }, enabled)
    is YesOrNoQuestion -> YesOrNoQuestionItem(question, { onAnswerChange(it) }, enabled)
    is MCQ -> MCQItem(question, { onAnswerChange(it) }, enabled)
    is MCQO -> MCQOItem(question, { onAnswerChange(it) }, enabled)
  }
}

/**
 * Displays the detailed view of a single report. The UI dynamically adapts depending on the current
 * user's role (Farmer or Vet).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportViewScreen(
    navigationActions: NavigationActions,
    userRole: UserRole,
    viewModel: ReportViewViewModel,
    reportId: String = ""
) {
  LaunchedEffect(reportId) { viewModel.loadReport(reportId) }

  val uiState by viewModel.uiState.collectAsState()
  // Observe save completion to navigate back on success
  val saveCompleted by viewModel.saveCompleted.collectAsState()
  val unsavedChanges by viewModel.unsavedChanges.collectAsState()

  // --- Auto-change PENDING -> IN_PROGRESS for vets ---
  LaunchedEffect(userRole, uiState.report.status) {
    if (userRole == UserRole.VET && uiState.report.status == ReportStatus.PENDING) {
      viewModel.onStatusChange(ReportStatus.IN_PROGRESS)
    }
  }

  // Navigate back when save is completed, then consume the flag to avoid re-trigger
  LaunchedEffect(saveCompleted) {
    if (saveCompleted) {
      navigationActions.goBack()
      viewModel.consumeSaveCompleted()
    }
  }

  val report = uiState.report
  val answerText = uiState.answerText
  val selectedStatus = uiState.status

  var isSpamDialogOpen by remember { mutableStateOf(false) }
  var isUnsavedAlertOpen by remember { mutableStateOf(false) }

  fun handleGoBack(force: Boolean = false) {
    if (unsavedChanges && !force) isUnsavedAlertOpen = true else navigationActions.goBack()
  }

  // Overrides behavior of Android system back button
  BackHandler { handleGoBack() }

  if (isUnsavedAlertOpen)
      UnsavedChangesAlert(
          onStay = { isUnsavedAlertOpen = false },
          onDiscard = {
            viewModel.consumeUnsavedChanges()
            isUnsavedAlertOpen = false
            handleGoBack(force = true)
          })

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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f))
                    Box(
                        modifier =
                            Modifier.background(
                                    color = statusColor(selectedStatus),
                                    shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag(ReportViewScreenTestTags.STATUS_BADGE_BOX)) {
                          Text(
                              text = selectedStatus.name.replace("_", " "),
                              style = MaterialTheme.typography.labelLarge,
                              color = MaterialTheme.colorScheme.onSurface,
                              modifier =
                                  Modifier.testTag(ReportViewScreenTestTags.STATUS_BADGE_TEXT))
                        }
                  }
            },
            navigationIcon = {
              IconButton(
                  onClick = { handleGoBack() },
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
                    .padding(16.dp)
                    .testTag(ReportViewScreenTestTags.SCROLL_CONTAINER),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

              // --- Full title (only if too long) ---
              val maxTitleChars = maxTitleCharsForScreen()
              val showFullTitleInBody = report.title.length > maxTitleChars
              if (showFullTitleInBody) {
                Text(
                    text = "Title: ${report.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
              }
              // ---- Farmer or Office info line ----
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.testTag(ReportViewScreenTestTags.ROLE_INFO_LINE)) {
                    if (userRole == UserRole.VET) {
                      // Vet views farmer
                      AuthorName(
                          uid = report.farmerId,
                          onClick = {
                            navigationActions.navigateTo(Screen.ViewUser(report.farmerId))
                          })
                    } else {
                      // Farmer views office
                      OfficeName(uid = report.officeId, onClick = { /* TODO("add ViewOffice") */})
                    }
                  }

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
              Text(
                  text = "Description: ${report.description}",
                  style = MaterialTheme.typography.bodyLarge)

              // ---- Questions (read-only) ----
              Text(
                  text = "Questions:",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold)

              uiState.report.questionForms.forEach { QuestionItem(it) }

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
                        Modifier.fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .testTag(ReportViewScreenTestTags.ANSWER_FIELD))
              }

              // ---- Status dropdown (Vet only) ----
              if (userRole == UserRole.VET) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.testTag(ReportViewScreenTestTags.STATUS_DROPDOWN_BOX)) {
                      OutlinedTextField(
                          value = selectedStatus.name.replace("_", " "),
                          onValueChange = {},
                          label = { Text("Status") },
                          readOnly = true,
                          trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                          },
                          modifier =
                              Modifier.menuAnchor()
                                  .fillMaxWidth()
                                  .testTag(ReportViewScreenTestTags.STATUS_DROPDOWN_FIELD))
                      ExposedDropdownMenu(
                          expanded = expanded,
                          onDismissRequest = { expanded = false },
                          modifier =
                              Modifier.testTag(ReportViewScreenTestTags.STATUS_DROPDOWN_MENU)) {
                            listOf(ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED).forEach { status
                              ->
                              DropdownMenuItem(
                                  text = { Text(status.name.replace("_", " ")) },
                                  onClick = {
                                    viewModel.onStatusChange(status)
                                    expanded = false
                                  },
                                  modifier =
                                      Modifier.testTag(
                                          ReportViewScreenTestTags.getTagForStatusOption(
                                              status.name)))
                            }
                          }
                    }
              }
              // ---- Set Time section ----
              Column {
                Text(
                    text = "Veterinarian visit: ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                  Text(
                      "    Start time: ",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.SemiBold)
                  Text(
                      text =
                          report.startTime?.format(
                              DateTimeFormatter.ofPattern("HH:mm ' on ' dd MMM yyyy"))
                              ?: "  Not set",
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.primaryContainer,
                      modifier =
                          Modifier.clickable(
                              onClick = {
                                navigationActions.navigateTo(Screen.Planner(reportId = report.id))
                              }))
                }
                Row {
                  Text(
                      "    Duration: ",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.SemiBold)
                  Text(
                      text =
                          report.duration?.format(DateTimeFormatter.ofPattern("HH:mm"))
                              ?: "  Not set",
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.primaryContainer,
                      modifier =
                          Modifier.clickable(
                              onClick = {
                                navigationActions.navigateTo(Screen.Planner(reportId = report.id))
                              }))
                }
              }

              // ---- Bottom section: Map + Spam + Save ----
              Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Row with View on Map (left) + Report Spam (right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                          OutlinedButton(
                              modifier =
                                  Modifier.weight(1f).testTag(ReportViewScreenTestTags.VIEW_ON_MAP),
                              onClick = {
                                navigationActions.navigateTo(
                                    Screen.Map(
                                        report.location?.latitude,
                                        report.location?.longitude,
                                        reportId))
                              }) {
                                Text("View on Map")
                              }

                          if (userRole == UserRole.VET) {
                            val isAlreadySpam = selectedStatus == ReportStatus.SPAM
                            if (!isAlreadySpam) {
                              val color = StatusColors().spam
                              OutlinedButton(
                                  onClick = { isSpamDialogOpen = true },
                                  colors =
                                      ButtonDefaults.outlinedButtonColors(contentColor = color),
                                  border = BorderStroke(1.dp, color),
                                  shape = MaterialTheme.shapes.medium,
                                  modifier =
                                      Modifier.weight(1f)
                                          .testTag(ReportViewScreenTestTags.SPAM_BUTTON)) {
                                    Text("Report as spam")
                                  }
                            } else {
                              Button(
                                  onClick = {},
                                  enabled = false,
                                  colors =
                                      ButtonDefaults.buttonColors(
                                          containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                          contentColor =
                                              MaterialTheme.colorScheme.onSurfaceVariant),
                                  modifier =
                                      Modifier.weight(1f)
                                          .testTag(ReportViewScreenTestTags.SPAM_BUTTON)) {
                                    Text("Reported as spam")
                                  }
                            }
                          }
                        }

                    // Save button (Vet only)
                    if (userRole == UserRole.VET) {
                      Button(
                          onClick = { viewModel.onSave() },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag(ReportViewScreenTestTags.SAVE_BUTTON)) {
                            Text("Save")
                          }
                    }
                  }

              // ---- Spam confirmation dialog ----
              if (isSpamDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isSpamDialogOpen = false },
                    title = { Text("Report as spam?") },
                    text = {
                      Text("This will mark the report as spam and hide it from regular view.")
                    },
                    confirmButton = {
                      TextButton(
                          onClick = {
                            viewModel.onSpam()
                            isSpamDialogOpen = false
                          }) {
                            Text("Confirm", color = MaterialTheme.colorScheme.error)
                          }
                    },
                    dismissButton = {
                      TextButton(onClick = { isSpamDialogOpen = false }) { Text("Cancel") }
                    })
              }
            }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnsavedChangesAlert(onDiscard: () -> Unit, onStay: () -> Unit) {
  AlertDialog(
      modifier = Modifier.testTag(ReportViewScreenTestTags.UNSAVED_ALERT_BOX),
      onDismissRequest = onStay,
      title = { Text("Unsaved changes") },
      text = { Text("You have unsaved changes. Go back anyway?") },
      confirmButton = {
        TextButton(
            modifier = Modifier.testTag(ReportViewScreenTestTags.UNSAVED_ALERT_BOX_DISCARD),
            onClick = onDiscard) {
              Text("Discard changes")
            }
      },
      dismissButton = {
        TextButton(
            modifier = Modifier.testTag(ReportViewScreenTestTags.UNSAVED_ALERT_BOX_CANCEL),
            onClick = onStay) {
              Text("Cancel")
            }
      })
}

/*  If you want to use the preview, just de-comment this block.
@Preview(showBackground = true, name = "Farmer View")
@Composable
fun PreviewReportViewFarmer() {
  MaterialTheme {
    val navController = rememberNavController()
    val viewModel = ReportViewModel()
    ReportViewScreen(
        navigationActions = NavigationActions(navController),
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
    val viewModel = ReportViewViewModel()
    ReportViewScreen(
        navigationActions = NavigationActions(navController),
        userRole = UserRole.VET,
        viewModel = viewModel,
        reportId = "RPT001")
  }
}
*/
