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
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.ui.common.OfficeNameViewModel
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.ui.user.UserViewModelContract
import java.io.File
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

  fun getTestTagForOffice(vetId: String): String = "officeOption_$vetId"
}

/** Texts for the report creation feedback */
object AddReportFeedbackTexts {
  const val SUCCESS = "Report created successfully!"
  const val FAILURE = "Couldn't upload report... Please verify your connection and try again"

  const val INCOMPLETE = "Please fill in all required fields..."
}

/** Constants for testing purposes */
object AddReportConstants {
  val officeOptions = listOf("Best Office Ever!", "Meh Office", "Great Office")
}

/** Texts on the button used to upload/remove a photo */
object AddReportUploadButtonTexts {
  const val UPLOAD_IMAGE = "Upload Image"
  const val REMOVE_IMAGE = "Remove Image"
}

/** Texts of the dialog shown when clicking on uploading photo button */
object AddReportDialogTexts {
  const val GALLERY = "Gallery"
  const val CAMERA = "camera"
  const val CANCEL = "cancel"
}

/** This **MUST** be the same as in: AndroidManifest.xml --> <provider --> android:authorities */
private const val FILE_PROVIDER =
    "com.android.agrihealth.fileprovider" // TODO: Maybe move this into its own object


// Helper function
private fun generateCreateReportErrorMessage(
  e: Throwable?
): String {
//  val baseMessage = AddReportFeedbackTexts.FAILURE
//  val errorMessage = e?.message ?: "Unknown error"
//
//  val fullMessage = """
//        $baseMessage
//
//        For more information, here is the actual error:
//        $errorMessage
//    """.trimIndent()
//
//  return fullMessage
  return e?.message ?: "Unknown error"
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
    userViewModel: UserViewModelContract = viewModel<UserViewModel>(),
    onBack: () -> Unit = {},
    onCreateReport: () -> Unit = {},
    addReportViewModel: AddReportViewModelContract
) {

  // TODO Add this back and make changes so ui state is remembered between recompositions
  //
  //  val factory = remember(userId) {
  //    object : androidx.lifecycle.ViewModelProvider.Factory {
  //      override fun <T : ViewModel> create(modelClass: Class<T>): T {
  //        return AddReportViewModel(userId) as T
  //      }
  //    }
  //  }
  //  val addReportViewModel: AddReportViewModel = viewModel(factory = factory)

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

  // For gallery/camera photo picking
  var showCamera by remember { mutableStateOf(false) }
  var showGallery by remember { mutableStateOf(false) }

  // For the dropdown menu
  var expanded by remember { mutableStateOf(false) } // For menu expanded/collapsed tracking
  var selectedOption by remember { mutableStateOf((user as Farmer).defaultOffice ?: "") }

  // For the confirmation snackbar (i.e alter window)
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // For the dialog when adding a report is successful
  var showSuccessDialog by remember { mutableStateOf(false) }

  // For the error dialog
  var showErrorDialog by remember { mutableStateOf(false) }
  var errorDialogMessage by remember { mutableStateOf<String?>(null) }

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
              MultilineField(
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

              var selectedOfficeName = offices[selectedOption] ?: selectedOption
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
                                .testTag(AddReportScreenTestTags.OFFICE_DROPDOWN))

                    ExposedDropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false }) {
                          offices.forEach { (option, displayName) ->
                            DropdownMenuItem(
                                text = { Text(displayName) },
                                onClick = {
                                  selectedOfficeName = displayName
                                  addReportViewModel.setOffice(option)
                                  expanded = false
                                },
                                modifier =
                                    Modifier.testTag(
                                        AddReportScreenTestTags.getTestTagForOffice(option)))
                          }
                        }
                  }

              UploadedImagePreview(photoUri = uiState.photoUri)

              UploadRemovePhotoButton(
                  photoAlreadyPicked = uiState.photoUri != null,
                  onImagePicked = { addReportViewModel.setPhoto(it) },
                  onImageRemoved = { addReportViewModel.removePhoto() })

              CreateReportButton(
                  addReportViewModel = addReportViewModel,
                  snackbarHostState = snackbarHostState,
                  onSuccess = { showSuccessDialog = true })
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

      if (showErrorDialog && errorDialogMessage != null) {
        AlertDialog(
          onDismissRequest = { showErrorDialog = false },
          confirmButton = {
            TextButton(onClick = { showErrorDialog = false }) {
              Text("OK")
            }
          },
          title = { Text("Report creation failed") },
          text = { Text(errorDialogMessage!!) }
        )
      }
}

/**
 * A text filed used in the addReport screen
 *
 * @param value The text stored on the text field
 * @param onValueChange What happens when the text on the text field changes
 * @param placeholder The placeholder shown when the text field is empty
 * @param testTag The test tag associated with the text field
 */
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

@Composable
private fun MultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String,
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = { Text(placeholder) },
      singleLine = false,
      minLines = 2,
      maxLines = Int.MAX_VALUE,
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(testTag),
  )
}

/**
 * Button used to either upload or remove a photo depending on if one has already been selected
 *
 * @param photoAlreadyPicked True if a photo has already been picked, False otherwise
 * @param onImagePicked What happens when a click was performed when the button shows "Upload image"
 * @param onImageRemoved What happens when a click was performed when the button shows "Remove
 *   image"
 */
@Composable
fun UploadRemovePhotoButton(
    photoAlreadyPicked: Boolean,
    onImagePicked: (Uri?) -> Unit,
    onImageRemoved: () -> Unit
) {
  val context = LocalContext.current

  var showDialog by remember { mutableStateOf(false) }

  // Opens the app's permissions so the user does not need to search for it
  fun openAppPermissionsSettings(context: Context) {
    val intent =
        Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null))
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    context.startActivity(intent)
  }

  var photoUri by remember { mutableStateOf<Uri?>(null) }

  // For picking photo on the gallery
  val galleryLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
          onImagePicked(uri)
        }
      }

  // For taking a photo with the camera
  val cameraLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
          onImagePicked(photoUri)
        }
      }

  // Camera launcher which also asks for permissions
  val cameraLauncherWithPermissionRequest =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
          if (photoUri != null) {
            cameraLauncher.launch(photoUri)
          }
        } else {
          Toast.makeText(
                  context, "Camera permission is required to take a photo", Toast.LENGTH_LONG)
              .show()
          openAppPermissionsSettings(context)
        }
      }

  Button(
      onClick = {
        if (photoAlreadyPicked) {
          onImageRemoved()
        } else {
          showDialog = true
        }
      },
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .testTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON),

      // TODO: Make button change color based on status when the app theme will be more developed
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
        Text(
            text =
                if (photoAlreadyPicked) AddReportUploadButtonTexts.REMOVE_IMAGE
                else AddReportUploadButtonTexts.UPLOAD_IMAGE,
            style = MaterialTheme.typography.titleLarge)
      }

  if (showDialog) {
    AlertDialog(
        modifier = Modifier.testTag(AddReportScreenTestTags.UPLOAD_IMAGE_DIALOG),
        onDismissRequest = { showDialog = false },
        title = { Text("Select Image Source") },
        text = { Text("Choose from gallery or take a new photo.") },
        confirmButton = {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                modifier = Modifier.testTag(AddReportScreenTestTags.DIALOG_GALLERY),
                onClick = {
                  showDialog = false
                  galleryLauncher.launch("image/*")
                },
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface)) {
                  Text(AddReportDialogTexts.GALLERY)
                }
            TextButton(
                modifier = Modifier.testTag(AddReportScreenTestTags.DIALOG_CAMERA),
                onClick = {
                  showDialog = false
                  // Create temporary file in cache
                  val imageFile = File.createTempFile("report_photo_", ".jpg", context.cacheDir)
                  photoUri = FileProvider.getUriForFile(context, FILE_PROVIDER, imageFile)
                  cameraLauncherWithPermissionRequest.launch(android.Manifest.permission.CAMERA)
                },
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface)) {
                  Text(AddReportDialogTexts.CAMERA)
                }
          }
        },
        dismissButton = {
          TextButton(
              modifier = Modifier.testTag(AddReportScreenTestTags.DIALOG_CANCEL),
              onClick = { showDialog = false },
              colors =
                  ButtonDefaults.textButtonColors(
                      contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                Text(AddReportDialogTexts.CANCEL)
              }
        })
  }
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
 * @param addReportViewModel The viewModel associated with this screen
 * @param snackbarHostState Current state of the object managing the snackbar
 * @param onSuccess What happens after the report has been submitted
 */
@Composable
fun CreateReportButton(
    addReportViewModel: AddReportViewModelContract,
    snackbarHostState: SnackbarHostState,
    onSuccess: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  Button(
    onClick = {
        scope.launch {
          when (val result = addReportViewModel.createReport()) {
            is CreateReportResult.Success -> {
              onSuccess()
            }
            is CreateReportResult.ValidationError -> {
              snackbarHostState.showSnackbar(AddReportFeedbackTexts.INCOMPLETE)
            }
            is CreateReportResult.PhotoUploadError -> {
              Toast.makeText(context, generateCreateReportErrorMessage(result.e), Toast.LENGTH_LONG).show()
            }
            is CreateReportResult.RepositoryError -> {
              Toast.makeText(context, generateCreateReportErrorMessage(result.e), Toast.LENGTH_LONG).show()
            }
          }
        }
      },
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
