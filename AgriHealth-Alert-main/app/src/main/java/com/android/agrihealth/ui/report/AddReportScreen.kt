package com.android.agrihealth.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.form.MCQ
import com.android.agrihealth.data.model.report.form.MCQO
import com.android.agrihealth.data.model.report.form.OpenQuestion
import com.android.agrihealth.data.model.report.form.QuestionForm
import com.android.agrihealth.data.model.report.form.YesOrNoQuestion
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.UserViewModel
import com.android.agrihealth.data.model.user.UserViewModelContract
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.common.resolver.OfficeNameViewModel
import com.android.agrihealth.ui.loading.LoadingOverlay
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.profile.LocalPhotoDisplay
import com.android.agrihealth.ui.profile.UploadRemovePhotoButton
import kotlin.collections.forEachIndexed
import kotlinx.coroutines.launch

// -- imports for preview --
/*
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.testhelpers.fakes.FakeAddReportViewModel
 */

/** Tags for the various components. For testing purposes */
object AddReportScreenTestTags {
  const val TITLE_FIELD = "titleField"
  const val DESCRIPTION_FIELD = "descriptionField"
  const val OFFICE_DROPDOWN = "officeDropDown"
  const val ADDRESS_FIELD = "addressField"
  const val LOCATION_BUTTON = "locationButton"
  const val CREATE_BUTTON = "createButton"
  const val SCROLL_CONTAINER = "scrollContainer"
  const val DIALOG_SUCCESS = "dialogSuccess"
  const val DIALOG_FAILURE = "dialogFailure"
  const val DIALOG_SUCCESS_OK = "dialogSuccessOk"
  const val DIALOG_FAILURE_OK = "dialogFailureOk"

  fun getTestTagForOffice(vetId: String): String = "officeOption_$vetId"
}

/** Texts for the report creation feedback */
object AddReportFeedbackTexts {
  const val SUCCESS = "Report created successfully!"
  const val FAILURE = "Couldn't upload report... Please verify your connection and try again..."
  const val INCOMPLETE = "Please fill in all required fields..."
  const val UNKNOWN = "Unknown error..."
}

/** Texts of the dialog shown when clicking on uploading photo button */
object AddReportDialogTexts {
  const val OK = "Ok"
  const val TITLE_SUCCESS = "Success!"
  const val TITLE_FAILURE = "Error!"
}

// Helper function to format the error message shown in the error dialog when creating a report
// failed
private fun generateCreateReportErrorMessage(e: Throwable?): String {
  val errorMessage = e?.message ?: AddReportFeedbackTexts.UNKNOWN

  val fullMessage = "${AddReportFeedbackTexts.FAILURE}\n\nDetails:\n${errorMessage}"

  return fullMessage
}

/**
 * Displays the report creation screen for farmers
 *
 * @param onBack A callback invoked when the back button in the top bar is pressed.
 * @param addReportViewModel The [AddReportViewModel] instance responsible for managing report
 *   creation logic and UI state.
 * @param onCreateReport Executed when the report has successfully been created
 * @param userViewModel ViewModel for managing user-related data and operations.
 * @see AddReportViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportScreen(
    userViewModel: UserViewModelContract = viewModel<UserViewModel>(),
    onBack: () -> Unit = {},
    onCreateReport: () -> Unit = {},
    pickedLocation: Location? = null,
    onChangeLocation: () -> Unit = {},
    addReportViewModel: AddReportViewModelContract
) {

  val reportUi by addReportViewModel.uiState.collectAsState()
  val userUi by userViewModel.uiState.collectAsState()
  val user = userUi.user

  val offices = remember { mutableStateMapOf<String, String>() }

  val focusManager = LocalFocusManager.current

  // For each linked office, load their name
  (user as Farmer).linkedOffices.forEach { officeId ->
    val vm: OfficeNameViewModel = viewModel(key = officeId)
    val label by vm.uiState.collectAsState()

    LaunchedEffect(officeId) {
      vm.loadOffice(uid = officeId, deletedOffice = "Deleted office", noneOffice = "Unknown office")
    }

    offices[officeId] = label
  }

  LaunchedEffect(pickedLocation) { addReportViewModel.setAddress(pickedLocation) }
  LaunchedEffect(Unit) {
    if (user.collected != reportUi.collected) addReportViewModel.switchCollected()
  }

  // For the dropdown menu
  var selectedOption by remember { mutableStateOf(user.defaultOffice ?: "") }

  // For the confirmation snackbar (i.e alter window)
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // For the dialog when adding a report is successful
  var showSuccessDialog by remember { mutableStateOf(false) }

  // For the error dialog
  var showErrorDialog by remember { mutableStateOf(false) }
  var errorDialogMessage by remember { mutableStateOf<String?>(null) }

  // For the create report button
  val onCreateReportClick: () -> Unit = {
    scope.launch {
      when (val result = addReportViewModel.createReport()) {
        is CreateReportResult.Success -> {
          showSuccessDialog = true
        }
        is CreateReportResult.ValidationError -> {
          snackbarHostState.showSnackbar(AddReportFeedbackTexts.INCOMPLETE)
        }
        is CreateReportResult.UploadError -> {
          errorDialogMessage = generateCreateReportErrorMessage(result.e)
          showErrorDialog = true
        }
      }
    }
  }

  LaunchedEffect(Unit) { addReportViewModel.setOffice(selectedOption) }
  Scaffold(
      snackbarHost = {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.imePadding())
      },
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
        LoadingOverlay(isLoading = reportUi.isLoading) {
          Column(
              modifier =
                  Modifier.padding(padding)
                      .fillMaxSize()
                      .verticalScroll(rememberScrollState())
                      .padding(16.dp)
                      .testTag(AddReportScreenTestTags.SCROLL_CONTAINER),
              verticalArrangement = Arrangement.Top) {
                HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))
                TitleField(reportUi.title, { addReportViewModel.setTitle(it) })

                DescriptionField(reportUi.description, { addReportViewModel.setDescription(it) })

                QuestionList(
                    questions = reportUi.questionForms,
                    focusManager = focusManager,
                    onQuestionChange = { index, updated ->
                      addReportViewModel.updateQuestion(index, updated)
                    })

                OutlinedTextField(
                    value = reportUi.address?.name ?: "Select a Location",
                    placeholder = { Text("Select a Location") },
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Selected Location") },
                    modifier =
                        Modifier.fillMaxWidth().testTag(AddReportScreenTestTags.ADDRESS_FIELD))
                Button(
                    onClick = {
                      focusManager.clearFocus()
                      onChangeLocation()
                    },
                    modifier =
                        Modifier.fillMaxWidth().testTag(AddReportScreenTestTags.LOCATION_BUTTON)) {
                      Text("Select Location")
                    }

                OfficeDropdown(
                    offices = offices,
                    selectedOfficeId = selectedOption,
                    focusManager = focusManager,
                    onOfficeSelected = { officeId ->
                      selectedOption = officeId
                      addReportViewModel.setOffice(officeId)
                    })

                LocalPhotoDisplay(
                    photoURI = reportUi.photoUri,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp))

                UploadRemovePhotoButton(
                    photoAlreadyPicked = reportUi.photoUri != null,
                    onPhotoPicked = { addReportViewModel.setPhoto(it) },
                    onPhotoRemoved = { addReportViewModel.removePhoto() })

                CollectedSwitch(reportUi.collected, { addReportViewModel.switchCollected() }, true)

                CreateReportButton(
                    onClick = {
                      focusManager.clearFocus()
                      onCreateReportClick()
                    })
              }

          CreateReportSuccessDialog(
              visible = showSuccessDialog,
              onDismiss = {
                showSuccessDialog = false
                onBack()
                onCreateReport()
              })

          CreateReportErrorDialog(
              visible = showErrorDialog,
              errorMessage = errorDialogMessage,
              onDismiss = { showErrorDialog = false })
        }
      }
}

/**
 * A dialog shown when the report has successfully been created
 *
 * @param visible True if the dialog should be shown
 * @param onDismiss Executed when the user dismisses the dialog
 */
@Composable
fun CreateReportSuccessDialog(visible: Boolean, onDismiss: () -> Unit) {
  if (!visible) return

  AlertDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(AddReportScreenTestTags.DIALOG_SUCCESS_OK),
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface)) {
              Text(AddReportDialogTexts.OK)
            }
      },
      title = { Text(AddReportDialogTexts.TITLE_SUCCESS) },
      text = { Text(AddReportFeedbackTexts.SUCCESS) },
      modifier = Modifier.testTag(AddReportScreenTestTags.DIALOG_SUCCESS),
  )
}

/**
 * A dialog shown when an error happened and a report couldn't be created
 *
 * @param visible True if the dialog should be shown
 * @param errorMessage The error message received when attempting to create a report
 * @param onDismiss Executed when the user dismisses the dialog
 */
@Composable
fun CreateReportErrorDialog(visible: Boolean, errorMessage: String?, onDismiss: () -> Unit) {
  if (!visible) return

  AlertDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(AddReportScreenTestTags.DIALOG_FAILURE_OK),
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface)) {
              Text(AddReportDialogTexts.OK)
            }
      },
      title = { Text(AddReportDialogTexts.TITLE_FAILURE) },
      text = { Text(errorMessage ?: AddReportFeedbackTexts.UNKNOWN) },
      modifier = Modifier.testTag(AddReportScreenTestTags.DIALOG_FAILURE))
}

/**
 * A dropdown menu to choose an office
 *
 * @param offices Offices and their associated label (i.e name)
 * @param selectedOfficeId The id of the currently selected office
 * @param onOfficeSelected Executed when an office is selected
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeDropdown(
    offices: Map<String, String>,
    selectedOfficeId: String,
    onOfficeSelected: (String) -> Unit,
    focusManager: FocusManager
) {
  var expanded by remember { mutableStateOf(false) }
  var selectedOfficeName = offices[selectedOfficeId] ?: selectedOfficeId

  ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
    OutlinedTextField(
        value = selectedOfficeName,
        onValueChange = {}, // No direct text editing
        readOnly = true,
        label = { Text("Choose an Office") },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        modifier =
            Modifier.menuAnchor().fillMaxWidth().testTag(AddReportScreenTestTags.OFFICE_DROPDOWN))

    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      offices.forEach { (option, displayName) ->
        DropdownMenuItem(
            text = { Text(displayName) },
            onClick = {
              focusManager.clearFocus()
              selectedOfficeName = displayName
              onOfficeSelected(option)
              expanded = false
            },
            modifier = Modifier.testTag(AddReportScreenTestTags.getTestTagForOffice(option)))
      }
    }
  }
}

/**
 * A long list of questions that must be answered to create a report
 *
 * @param questions A list containing all the questions
 * @param onQuestionChange Called when the user changes the selected answer of a question
 */
@Composable
fun QuestionList(
    questions: List<QuestionForm>,
    onQuestionChange: (index: Int, updated: QuestionForm) -> Unit,
    focusManager: FocusManager
) {
  questions.forEachIndexed { index, question ->
    when (question) {
      is OpenQuestion -> {
        OpenQuestionItem(
            question = question,
            onAnswerChange = { updated ->
              focusManager.clearFocus()
              onQuestionChange(index, updated)
            },
            enabled = true,
            modifier = Modifier.testTag("QUESTION_${index}_OPEN"))
      }
      is YesOrNoQuestion -> {
        YesOrNoQuestionItem(
            question = question,
            onAnswerChange = { updated ->
              focusManager.clearFocus()
              onQuestionChange(index, updated)
            },
            enabled = true,
            modifier = Modifier.testTag("QUESTION_${index}_YESORNO"))
      }
      is MCQ -> {
        MCQItem(
            question = question,
            onAnswerChange = { updated ->
              focusManager.clearFocus()
              onQuestionChange(index, updated)
            },
            enabled = true,
            modifier = Modifier.testTag("QUESTION_${index}_MCQ"))
      }
      is MCQO -> {
        MCQOItem(
            question = question,
            onAnswerChange = { updated ->
              focusManager.clearFocus()
              onQuestionChange(index, updated)
            },
            enabled = true,
            modifier = Modifier.testTag("QUESTION_${index}_MCQO"))
      }
    }
  }
}

/**
 * A text field used to input the title o the report. It is kept single line to encourage users to
 * write short titles
 *
 * @param value The text stored on the text field
 * @param onValueChange What happens when the text on the text field changes
 */
@Composable
private fun TitleField(
    value: String,
    onValueChange: (String) -> Unit,
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = { Text("Title") },
      singleLine = true,
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .testTag(AddReportScreenTestTags.TITLE_FIELD),
  )
}

/**
 * A multi-line text field used to input the description of the report. It is 3 lines high so that
 * the user can see that it can and should hold more text than the title
 *
 * @param value The text stored on the text field
 * @param onValueChange What happens when the text on the text field changes
 */
@Composable
private fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = { Text("Description") },
      singleLine = false,
      minLines = 3,
      maxLines = Int.MAX_VALUE,
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .testTag(AddReportScreenTestTags.DESCRIPTION_FIELD),
  )
}

/**
 * Buttons that allows creating a report and uploading it on the repository
 *
 * @param onSuccess What happens after the report has been submitted
 */
@Composable
fun CreateReportButton(
    onClick: () -> Unit,
) {
  Button(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().testTag(AddReportScreenTestTags.CREATE_BUTTON)) {
        Text("Create Report")
      }
}

/*
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AddReportScreenPreview() {
  AgriHealthAppTheme {
    AddReportScreen(
        userViewModel = FakeUserViewModel(), addReportViewModel = FakeAddReportViewModel())
  }
}
*/
