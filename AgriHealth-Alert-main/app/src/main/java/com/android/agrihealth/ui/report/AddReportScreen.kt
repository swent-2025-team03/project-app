package com.android.agrihealth.ui.report

import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.user.Farmer
import coil.compose.AsyncImage
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.testutil.FakeAddReportViewModel
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.launch

/** Tags for the various components. For testing purposes */
object AddReportScreenTestTags {
  const val TITLE_FIELD = "titleField"
  const val DESCRIPTION_FIELD = "descriptionField"
  const val VET_DROPDOWN = "vetDropDown"
  const val CREATE_BUTTON = "createButton"
  const val UPLOAD_IMAGE_BUTTON = "uploadImageButton"
  const val IMAGE_PREVIEW = "imageDisplay"

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

object AddReport_UploadButtonTexts {
  const val UPLOAD_IMAGE = "Upload Image"
  const val REMOVE_IMAGE = "Remove Image"
}

// TODO: Replace these with the theme colors (in a global theme file or similar)
private val unfocusedFieldColor = Color(0xFFF0F7F1)
private val focusedFieldColor = Color(0xFFF0F7F1)
private val createReportButtonColor = Color(0xFF96B7B1)
private val imageUploadButton_UploadColor = Color(0xFF43b593)
private val imageUploadButton_RemoveColor = Color(0xFFd45d5d)

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
  val vets = (user as Farmer).linkedVets

  // For the dropdown menu
  var expanded by remember { mutableStateOf(false) } // For menu expanded/collapsed tracking
  var selectedOption by remember { mutableStateOf((user as Farmer).defaultVet ?: "") }
  addReportViewModel.setVet(selectedOption)

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
                        fontWeight = FontWeight.Bold,
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
                    .padding(16.dp),
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

              ExposedDropdownMenuBox(
                  expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedOption,
                        onValueChange = {}, // No direct text editing
                        readOnly = true,
                        label = { Text("Choose a Vet") },
                        trailingIcon = {
                          ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier =
                            Modifier.menuAnchor() // Needed for M3 dropdown alignment
                                .fillMaxWidth()
                                .testTag(AddReportScreenTestTags.VET_DROPDOWN))

                    ExposedDropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false }) {
                          vets.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                  selectedOption = option
                                  addReportViewModel.setVet(option)
                                  expanded = false
                                },
                                modifier =
                                    Modifier.testTag(
                                        AddReportScreenTestTags.getTestTagForVet(option)))
                          }
                        }
                  }

              UploadedImagePreview(photoUri = uiState.photoUri)

              ImageUploadButton(
                  photoUri = uiState.photoUri,
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
      shape = RoundedCornerShape(28.dp),
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(testTag),
      colors =
          OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = unfocusedFieldColor,
              focusedContainerColor = focusedFieldColor,
              unfocusedBorderColor = Color.Transparent,
              focusedBorderColor = Color.Transparent))
}

@Composable
fun ImageUploadButton(
    photoUri: Uri?,
    onImagePicked: (Uri?) -> Unit,
    onImageRemoved: () -> Unit,
) {
  val imagePickerLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri?
        ->
        onImagePicked(uri)
      }
  val imageAlreadyUploaded = photoUri != null
  val buttonColor =
      if (imageAlreadyUploaded) imageUploadButton_RemoveColor else imageUploadButton_UploadColor

  Button(
      onClick = {
        if (imageAlreadyUploaded) {
          // Remove existing image
          onImageRemoved()
        } else {
          // Pick/upload new image
          imagePickerLauncher.launch("image/*")
        }
      },
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .testTag(AddReportScreenTestTags.UPLOAD_IMAGE_BUTTON),
      shape = RoundedCornerShape(20.dp),
      colors = ButtonDefaults.buttonColors(containerColor = buttonColor)) {
        Text(text = if (imageAlreadyUploaded) "Remove Image" else "Upload Image", fontSize = 18.sp)
      }
}

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

@Composable
fun CreateReportButton(
    addReportViewModel: AddReportViewModelContract,
    snackbarHostState: SnackbarHostState,
    onSuccess: () -> Unit
) {
  val scope = rememberCoroutineScope()
  Button(
      onClick = {
        scope.launch {
          val created = addReportViewModel.createReport()
          if (created) {
            onSuccess()
          } else {
            snackbarHostState.showSnackbar(AddReportFeedbackTexts.FAILURE)
          }
        }
      },
      modifier =
          Modifier.fillMaxWidth().height(56.dp).testTag(AddReportScreenTestTags.CREATE_BUTTON),
      shape = RoundedCornerShape(20.dp),
      colors = ButtonDefaults.buttonColors(containerColor = createReportButtonColor)) {
        Text("Create Report", fontSize = 24.sp)
      }
}

/**
 * Preview of the ReportViewScreen for both farmer and vet roles. Allows testing of layout and
 * colors directly in Android Studio.
 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AddReportScreenPreview() {
  MaterialTheme {
    AddReportScreen(
        userRole = UserRole.FARMER,
        userId = "FARMER_001",
        onCreateReport = {},
        addReportViewModel = FakeAddReportViewModel())
  }
}
