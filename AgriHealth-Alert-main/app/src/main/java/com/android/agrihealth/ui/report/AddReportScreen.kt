package com.android.agrihealth.ui.report

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.QuestionForm
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.ui.common.OfficeNameViewModel
import com.android.agrihealth.ui.common.rememberImagePickerLauncher
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.ui.user.UserViewModelContract
import java.io.File
import kotlin.collections.forEachIndexed
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
  const val OFFICE_DROPDOWN = "officeDropDown"
  const val CREATE_BUTTON = "createButton"
  const val UPLOAD_IMAGE_BUTTON = "uploadImageButton"
  const val UPLOAD_IMAGE_DIALOG = "uploadImageDialog"
  const val DIALOG_GALLERY = "uploadImageDialogGallery"
  const val DIALOG_CAMERA = "uploadImageDialogCamera"
  const val DIALOG_CANCEL = "uploadImageDialogCancel"
  const val IMAGE_PREVIEW = "imageDisplay"
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

/** Texts on the button used to upload/remove a photo */
object AddReportUploadButtonTexts {
  const val UPLOAD_IMAGE = "Upload Image"
  const val REMOVE_IMAGE = "Remove Image"
}

/** Texts of the dialog shown when clicking on uploading photo button */
object AddReportDialogTexts {
  const val GALLERY = "Gallery"
  const val CAMERA = "Camera"
  const val CANCEL = "Cancel"
  const val OK = "Ok"
  const val TITLE_SUCCESS = "Success!"
  const val TITLE_FAILURE = "Error!"
}

// This **MUST** be the same as in: AndroidManifest.xml --> <provider --> android:authorities
private fun getFileProviderAuthority(context: Context): String =
  context.packageName + ".fileprovider" // TODO: Maybe move this into its own object

// Helper function to format the error message shown in the error dialog when creating a report
// failed
private fun generateCreateReportErrorMessage(e: Throwable?): String {
  val errorMessage = e?.message ?: AddReportFeedbackTexts.UNKNOWN

  val fullMessage = "${AddReportFeedbackTexts.FAILURE}\n\nDetails:\n${errorMessage}"

  return fullMessage
}

private fun openAppPermissionsSettings(context: Context) {
  val intent = Intent(
    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
    Uri.fromParts("package", context.packageName, null),
  ).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  context.startActivity(intent)
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
    addReportViewModel: AddReportViewModelContract
) {

  val uiState by addReportViewModel.uiState.collectAsState()
  val user by userViewModel.user.collectAsState()

  val offices = remember { mutableStateMapOf<String, String>() }

  // For each linked office, load their name
  (user as Farmer).linkedOffices.forEach { officeId ->
    val vm: OfficeNameViewModel = viewModel(key = officeId)
    val label by vm.uiState.collectAsState()

    LaunchedEffect(officeId) {
      vm.loadOffice(uid = officeId, deletedOffice = "Deleted office", noneOffice = "Unknown office")
    }

    offices[officeId] = label
  }

  // For the dropdown menu
  var selectedOption by remember { mutableStateOf((user as Farmer).defaultOffice ?: "") }

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
              TitleField(uiState.title, { addReportViewModel.setTitle(it) })

              DescriptionField(uiState.description, { addReportViewModel.setDescription(it) })

              QuestionList(
                  questions = uiState.questionForms,
                  onQuestionChange = { index, updated ->
                    addReportViewModel.updateQuestion(index, updated)
                  })

              OfficeDropdown(
                  offices = offices,
                  selectedOfficeId = selectedOption,
                  onOfficeSelected = { officeId ->
                    selectedOption = officeId
                    addReportViewModel.setOffice(officeId)
                  })

              UploadedImagePreview(photoUri = uiState.photoUri)

            UploadRemovePhotoSection(
              photoAlreadyPicked = uiState.photoUri != null,
              onPhotoPicked = { addReportViewModel.setPhoto(it) },
              onPhotoRemoved = { addReportViewModel.removePhoto() },
            )

              CreateReportButton(onClick = onCreateReportClick)
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
    onOfficeSelected: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  var selectedOfficeName by remember {
    mutableStateOf(offices[selectedOfficeId] ?: selectedOfficeId)
  }

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
    onQuestionChange: (index: Int, updated: QuestionForm) -> Unit
) {
  questions.forEachIndexed { index, question ->
    when (question) {
      is OpenQuestion -> {
        OpenQuestionItem(
            question = question,
            onAnswerChange = { updated -> onQuestionChange(index, updated) },
            enabled = true,
            modifier = Modifier.testTag("QUESTION_${index}_OPEN"))
      }
      is YesOrNoQuestion -> {
        YesOrNoQuestionItem(
            question = question,
            onAnswerChange = { updated -> onQuestionChange(index, updated) },
            enabled = true,
            modifier = Modifier.testTag("QUESTION_${index}_YESORNO"))
      }
      is MCQ -> {
        MCQItem(
            question = question,
            onAnswerChange = { updated -> onQuestionChange(index, updated) },
            enabled = true,
            modifier = Modifier.testTag("QUESTION_${index}_MCQ"))
      }
      is MCQO -> {
        MCQOItem(
            question = question,
            onAnswerChange = { updated -> onQuestionChange(index, updated) },
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
 *  The section of the UI that handles adding or removing a photo from the report
 *
 *  @param photoAlreadyPicked True if a photo has already ben added to the report, False otherwise
 *  @param onPhotoPicked Called when a photo has been picked from the report
 *  @param onPhotoRemoved Called when a photo has been removed from the report
 */
@Composable
fun UploadRemovePhotoSection(
  photoAlreadyPicked: Boolean,
  onPhotoPicked: (Uri?) -> Unit,
  onPhotoRemoved: () -> Unit,
) {
  var showDialog by remember { mutableStateOf(false) }

  val imagePickerState = rememberImagePickerLauncher(
    onImageSelected = onPhotoPicked
  )

  UploadRemovePhotoButton(
    photoAlreadyPicked = photoAlreadyPicked,
    onClickUpload = { showDialog = true },
    onClickRemove = onPhotoRemoved,
  )

  if (showDialog) {
    PhotoSourceDialog(
      onDismiss = { showDialog = false },
      onGalleryClick = {
        showDialog = false
        imagePickerState.launchGallery()
      },
      onCameraClick = {
        showDialog = false
        imagePickerState.launchCamera()
      },
    )
  }
}

/**
 *  The button that allows user to either add a photo to the report or remove a photo from the report
 *
 *  @param photoAlreadyPicked True if a photo has already been picked by the user, False otherwise
 *  @param onClickUpload Called when the user clicks to add a photo to the report
 *  @param onClickRemove Called when the user clicks to remove a photo from the report
 */
@Composable
fun UploadRemovePhotoButton(
  photoAlreadyPicked: Boolean,
  onClickUpload: () -> Unit,
  onClickRemove: () -> Unit,
) {
  Button(
    onClick = {
      if (photoAlreadyPicked) onClickRemove() else onClickUpload()
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
      .testTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON),
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.primary
    ),
  ) {
    Text(
      text = if (photoAlreadyPicked)
        AddReportUploadButtonTexts.REMOVE_IMAGE
      else
        AddReportUploadButtonTexts.UPLOAD_IMAGE,
      style = MaterialTheme.typography.titleLarge,
    )
  }
}

/**
 *  Dialog that asks the user whether they want to upload a photo from gallery or from camera
 *
 *  @param onDismiss Called when the user dismisses the dialog (i.e clicks on "Cancel")
 *  @param onGalleryClick Called when the user clicks on the "Gallery" button
 *  @param onCameraClick Called when the user clicks on the "Camera" button
 */
@Composable
fun PhotoSourceDialog(
  onDismiss: () -> Unit,
  onGalleryClick: () -> Unit,
  onCameraClick: () -> Unit,
) {
  AlertDialog(
    modifier = Modifier.testTag(AddReportScreenTestTags.UPLOAD_IMAGE_DIALOG),
    onDismissRequest = onDismiss,
    title = { Text("Select Image Source") },
    text = { Text("Choose from gallery or take a new photo.") },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(
          modifier = Modifier.testTag(AddReportScreenTestTags.DIALOG_GALLERY),
          onClick = onGalleryClick,
          colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
          ),
        ) {
          Text(AddReportDialogTexts.GALLERY)
        }
        TextButton(
          modifier = Modifier.testTag(AddReportScreenTestTags.DIALOG_CAMERA),
          onClick = onCameraClick,
          colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
          ),
        ) {
          Text(AddReportDialogTexts.CAMERA)
        }
      }
    },
    dismissButton = {
      TextButton(
        modifier = Modifier.testTag(AddReportScreenTestTags.DIALOG_CANCEL),
        onClick = onDismiss,
        colors = ButtonDefaults.textButtonColors(
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
      ) {
        Text(AddReportDialogTexts.CANCEL)
      }
    },
  )
}

/**
 * Displays the photo that was picked by the user before being uploaded and possible compressed by
 * the image repository
 *
 * TODO: Display the photo stored on the image repository to avoid discrepancy
 */
@Composable
fun UploadedImagePreview(photoUri: Uri?, modifier: Modifier = Modifier) {
  if (photoUri != null) {
    AsyncImage(
        model = photoUri,
        contentDescription = "Uploaded image",
        modifier =
            modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(bottom = 8.dp)
                .testTag(AddReportScreenTestTags.IMAGE_PREVIEW),
        contentScale = ContentScale.Fit)
  }
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
      modifier =
          Modifier.fillMaxWidth().height(56.dp).testTag(AddReportScreenTestTags.CREATE_BUTTON)) {
        Text("Create Report", style = MaterialTheme.typography.titleLarge)
      }
}

// TODO: (OPTIONAL) Make this work again
/// **
// * Preview of the ReportViewScreen for both farmer and vet roles. Allows testing of layout and
// * colors directly in Android Studio.
// */
// @Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
// @Composable
// fun AddReportScreenPreview() {
//  AgriHealthAppTheme {
//    AddReportScreen(
//        userRole = UserRole.FARMER,
//        userId = "FARMER_001",
//        onCreateReport = {},
//        addReportViewModel = FakeAddReportViewModel())
//  }
// }
