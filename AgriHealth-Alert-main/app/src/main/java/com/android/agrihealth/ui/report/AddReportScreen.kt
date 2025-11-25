package com.android.agrihealth.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.ui.common.OfficeNameViewModel
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.launch

// -- imports for preview --
/*
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.testutil.FakeAddReportViewModel
 */

/** Tags for the various components. For testing purposes */
object AddReportScreenTestTags {
  const val TITLE_FIELD = "titleField"
  const val DESCRIPTION_FIELD = "descriptionField"
  const val VET_DROPDOWN = "vetDropDown"
  const val CREATE_BUTTON = "createButton"
  const val SCROLL_CONTAINER = "AddReportScrollContainer"

  fun getTestTagForVet(vetId: String): String = "vetOption_$vetId"
}

/** Texts for the report creation feedback. For testing purposes */
object AddReportFeedbackTexts {
  const val SUCCESS = "Report created successfully!"
  const val FAILURE = "Please fill in all required fields..."
}

// Used for testing purposes
object AddReportConstants {
  val vetOptions = listOf("Best Vet Ever!", "Meh Vet", "Great Vet")
}

/**
 * Displays the report creation screen for farmers
 *
 * @param onBack A callback invoked when the back button in the top bar is pressed.
 * @param createReportViewModel The [AddReportViewModel] instance responsible for managing report
 *   creation logic and UI state.
 * @see AddReportViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportScreen(
    userViewModel: UserViewModel = viewModel(),
    onBack: () -> Unit = {},
    userRole: UserRole,
    userId: String,
    onCreateReport: () -> Unit = {},
    addReportViewModel: AddReportViewModelContract
) {

  val uiState by addReportViewModel.uiState.collectAsState()
  val user by userViewModel.user.collectAsState()

  val offices = remember { mutableStateMapOf<String, String>() }

  // For each linked vet, load their name
  (user as Farmer).linkedOffices.forEach { officeId ->
    val vm: OfficeNameViewModel = viewModel(key = officeId)
    val label by vm.uiState.collectAsState()

    LaunchedEffect(officeId) {
      vm.loadOffice(uid = officeId, deletedOffice = "Deleted office", noneOffice = "Unknown office")
    }

    offices[officeId] = label
  }

  // For the dropdown menu
  var expanded by remember { mutableStateOf(false) } // For menu expanded/collapsed tracking
  var selectedOption by remember { mutableStateOf((user as Farmer).defaultOffice ?: "") }

  // For the confirmation snackbar (i.e alter window)
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // For the dialog when adding a report is successful
  var showSuccessDialog by remember { mutableStateOf(false) }

  Scaffold(
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        // Top bar with back arrow and title/status
        TopAppBar(
            title = {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Screen.AddReport.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f).testTag(NavigationTestTags.TOP_BAR_TITLE))
                  }
            },
            navigationIcon = {
              IconButton(
                  onClick = onBack,
                  modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
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
                    .testTag(AddReportScreenTestTags.SCROLL_CONTAINER),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              Field(
                  uiState.title,
                  { addReportViewModel.setTitle(it) },
                  "Title",
                  AddReportScreenTestTags.TITLE_FIELD)
              Field(
                  uiState.description,
                  { addReportViewModel.setDescription(it) },
                  "Description",
                  AddReportScreenTestTags.DESCRIPTION_FIELD)

              // Questions
              uiState.questionForms.forEachIndexed { index, question ->
                when (question) {
                  is OpenQuestion -> {
                    OpenQuestionItem(
                        question = question,
                        onAnswerChange = { updated ->
                          addReportViewModel.updateQuestion(index, updated)
                        },
                        enabled = true,
                        modifier = Modifier.testTag("QUESTION_${index}_OPEN"))
                  }
                  is YesOrNoQuestion -> {
                    YesOrNoQuestionItem(
                        question = question,
                        onAnswerChange = { updated ->
                          addReportViewModel.updateQuestion(index, updated)
                        },
                        enabled = true,
                        modifier = Modifier.testTag("QUESTION_${index}_YESORNO"))
                  }
                  is MCQ -> {
                    MCQItem(
                        question = question,
                        onAnswerChange = { updated ->
                          addReportViewModel.updateQuestion(index, updated)
                        },
                        enabled = true,
                        modifier = Modifier.testTag("QUESTION_${index}_MCQ"))
                  }
                  is MCQO -> {
                    MCQOItem(
                        question = question,
                        onAnswerChange = { updated ->
                          addReportViewModel.updateQuestion(index, updated)
                        },
                        enabled = true,
                        modifier = Modifier.testTag("QUESTION_${index}_MCQO"))
                  }
                }
              }

              val selectedOfficeName = offices[selectedOption] ?: ""
              ExposedDropdownMenuBox(
                  expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedOfficeName,
                        onValueChange = {}, // No direct text editing
                        readOnly = true,
                        label = { Text("Choose an Office") },
                        trailingIcon = {
                          ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier =
                            Modifier.menuAnchor() // Needed for M3 dropdown alignment
                                .fillMaxWidth()
                                .testTag(AddReportScreenTestTags.VET_DROPDOWN))

                    ExposedDropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false }) {
                          offices.keys.forEach { option ->
                            val displayName = offices[option] ?: option
                            DropdownMenuItem(
                                text = { Text(displayName) },
                                onClick = {
                                  selectedOption = displayName
                                  addReportViewModel.setVet(option)
                                  expanded = false
                                },
                                modifier =
                                    Modifier.testTag(
                                        AddReportScreenTestTags.getTestTagForVet(option)))
                          }
                        }
                  }

              Button(
                  onClick = {
                    scope.launch {
                      val created = addReportViewModel.createReport()
                      if (created) {
                        showSuccessDialog = true
                      } else {
                        snackbarHostState.showSnackbar(AddReportFeedbackTexts.FAILURE)
                      }
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(56.dp)
                          .testTag(AddReportScreenTestTags.CREATE_BUTTON)) {
                    Text("Create Report", style = MaterialTheme.typography.titleLarge)
                  }
            }

        // If adding the report was successful
        if (showSuccessDialog) {
          AlertDialog(
              onDismissRequest = {
                showSuccessDialog = false
                onBack()
              },
              confirmButton = {
                TextButton(
                    onClick = {
                      showSuccessDialog = false
                      onCreateReport()
                      onBack()
                    }) {
                      Text("OK")
                    }
              },
              title = { Text("Success!") },
              text = { Text(AddReportFeedbackTexts.SUCCESS) })
        }
      }
}

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = { Text(placeholder) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(testTag),
  )
}

/**
 * Preview of the ReportViewScreen for both farmer and vet roles. Allows testing of layout and
 * colors directly in Android Studio.
 */
/*
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AddReportScreenPreview() {
  AgriHealthAppTheme {
    AddReportScreen(
        userRole = UserRole.FARMER,
        userId = "FARMER_001",
        onCreateReport = {},
        addReportViewModel = FakeAddReportViewModel())
  }
}
*/
