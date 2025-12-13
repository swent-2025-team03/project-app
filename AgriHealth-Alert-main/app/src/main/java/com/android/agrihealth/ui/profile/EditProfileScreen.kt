package com.android.agrihealth.ui.profile

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.connection.ConnectionRepository
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.OfficeRepositoryProvider
import com.android.agrihealth.data.model.user.*
import com.android.agrihealth.ui.common.OfficeNameViewModel
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.NavigationTestTags.GO_BACK_BUTTON
import com.android.agrihealth.ui.office.ManageOfficeViewModel
import com.android.agrihealth.ui.profile.EditProfileScreenTestTags.PASSWORD_BUTTON
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.PROFILE_IMAGE
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import com.android.agrihealth.ui.report.CollectedSwitch
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.ui.user.UserViewModelContract
import com.android.agrihealth.ui.utils.ImagePickerDialog
import com.mr0xf00.easycrop.AspectRatio
import com.mr0xf00.easycrop.CircleCropShape
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.CropState
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.ImageCropper
import com.mr0xf00.easycrop.LocalCropperStyle
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.flipHorizontal
import com.mr0xf00.easycrop.flipVertical
import com.mr0xf00.easycrop.images.ImageSrc
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rotLeft
import com.mr0xf00.easycrop.rotRight
import com.mr0xf00.easycrop.ui.AspectSelectionMenu
import com.mr0xf00.easycrop.ui.ButtonsBar
import com.mr0xf00.easycrop.ui.CropperControls
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import com.mr0xf00.easycrop.ui.LocalVerticalControls
import com.mr0xf00.easycrop.ui.ShapeSelectionMenu
import kotlinx.coroutines.launch

enum class CodeType {
  FARMER,
  VET;

  fun displayName(): String =
      when (this) {
        FARMER -> "Farmer"
        VET -> "Vet"
      }
}

object EditProfileScreenTestTags {
  const val FIRSTNAME_FIELD = "FirstNameField"
  const val LASTNAME_FIELD = "LastNameField"
  const val DESCRIPTION_FIELD = "Description"
  const val PASSWORD_FIELD = "PasswordField"
  const val ADDRESS_FIELD = "EditAddressField"
  const val LOCATION_BUTTON = "LocationButton"
  const val DEFAULT_VET_DROPDOWN = "DefaultVetDropdown"
  const val ADD_CODE_BUTTON = "AddVetButton"
  const val SAVE_BUTTON = "SaveButton"
  const val PASSWORD_BUTTON = "PasswordButton"
  const val COPY_CODE_BUTTON = "CopyActiveCodeListElementButton"
  const val EDIT_PROFILE_PICTURE_BUTTON = "EditProfilePictureButton"

  fun dropdownTag(type: String) = "ACTIVE_CODES_DROPDOWN_$type"

  fun dropdownElementTag(type: String) = "ACTIVE_CODE_ELEMENT_$type"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userViewModel: UserViewModelContract = viewModel<UserViewModel>(),
    pickedLocation: Location? = null,
    onChangeLocation: () -> Unit = {},
    onGoBack: () -> Unit = {},
    onSave: (User) -> Unit = { userViewModel.updateUser(it) },
    onPasswordChange: () -> Unit = {}
) {

  val connectionRepository = remember { ConnectionRepository(connectionType = "") }
  val codesViewModel = remember { CodesViewModel(userViewModel, connectionRepository) }

  val user by userViewModel.user.collectAsState()
  val userRole = user.role
  val currentUser = user

  LaunchedEffect(user) {
    if (currentUser is Vet) {
      codesViewModel.loadActiveCodesForVet(currentUser)
    }
  }

  val farmerCodes by codesViewModel.farmerCodes.collectAsState()
  val vetCodes by codesViewModel.vetCodes.collectAsState()

  val createManageOfficeViewModel =
      object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return ManageOfficeViewModel(
              userViewModel = userViewModel, officeRepository = OfficeRepositoryProvider.get())
              as T
        }
      }

  val manageOfficeVm: ManageOfficeViewModel = viewModel(factory = createManageOfficeViewModel)

  val isOwner = manageOfficeVm.uiState.collectAsState().value.office?.ownerId == user.uid

  val snackbarHostState = remember { SnackbarHostState() }

  // Local mutable states
  var firstname by remember { mutableStateOf(user.firstname) }
  var lastname by remember { mutableStateOf(user.lastname) }
  var description by remember { mutableStateOf(user.description ?: "") }
  var address by remember { mutableStateOf(pickedLocation?.name ?: "") }

  // Farmer-specific states
  var selectedDefaultOffice by remember { mutableStateOf((user as? Farmer)?.defaultOffice) }
  var expandedVetDropdown by remember { mutableStateOf(false) }
  var collected by remember { mutableStateOf(user.collected) }

  var showPhotoPickerDialog by remember { mutableStateOf(false) }
  var showPhotoCropper by remember { mutableStateOf(false) }
  var chosenUri : Uri? by remember {mutableStateOf(null)}
  val imageCropper = rememberImageCropper()
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Edit Profile",
                  style = MaterialTheme.typography.titleLarge,
                  modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
            },
            navigationIcon = {
              IconButton(onClick = onGoBack, modifier = Modifier.testTag(GO_BACK_BUTTON)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
              }
            },
            modifier = Modifier.testTag(TOP_BAR))
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        Column(
            modifier =
                Modifier
                  .padding(innerPadding)
                  .padding(16.dp)
                  .fillMaxSize()
                  .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

          Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = "Profile Picture",
              modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .testTag(PROFILE_IMAGE),
              tint = MaterialTheme.colorScheme.primary
            )

            // Camera icon overlay
            FloatingActionButton(
              onClick = { showPhotoPickerDialog = true },
              modifier = Modifier
                .size(40.dp)
                .align(Alignment.BottomEnd)
                .testTag(EditProfileScreenTestTags.EDIT_PROFILE_PICTURE_BUTTON),
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Edit profile picture",
                modifier = Modifier.size(20.dp)
              )
            }
          }

              Spacer(modifier = Modifier.height(24.dp))

              // First name
              OutlinedTextField(
                  value = firstname,
                  onValueChange = { firstname = it },
                  label = { Text("First Name") },
                  modifier =
                      Modifier
                        .fillMaxWidth()
                        .testTag(EditProfileScreenTestTags.FIRSTNAME_FIELD))

              Spacer(modifier = Modifier.height(12.dp))

              // Last name
              OutlinedTextField(
                  value = lastname,
                  onValueChange = { lastname = it },
                  label = { Text("Last Name") },
                  modifier =
                      Modifier
                        .fillMaxWidth()
                        .testTag(EditProfileScreenTestTags.LASTNAME_FIELD))

              Spacer(modifier = Modifier.height(12.dp))

              // Description
              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text("Description") },
                  modifier =
                      Modifier
                        .fillMaxWidth()
                        .testTag(EditProfileScreenTestTags.DESCRIPTION_FIELD))

              if (!user.isGoogleAccount) {
                Spacer(modifier = Modifier.height(12.dp))

                // Password
                OutlinedTextField(
                    value = "********",
                    onValueChange = {},
                    label = { Text("Password") },
                    enabled = true,
                    readOnly = true,
                    modifier =
                        Modifier
                          .fillMaxWidth()
                          .testTag(EditProfileScreenTestTags.PASSWORD_FIELD),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                      IconButton(
                          onClick = { onPasswordChange() },
                          modifier = Modifier.testTag(PASSWORD_BUTTON)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Password")
                          }
                    })
              }

              Spacer(modifier = Modifier.height(12.dp))
              if (user is Vet && !isOwner) {
                Text(
                    text = "Only office owners can change their address.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp))
              }
              // Address
              OutlinedTextField(
                  value = address,
                  onValueChange = { address = it },
                  readOnly = true,
                  singleLine = true,
                  label = {
                    when (userRole) {
                      UserRole.FARMER -> Text("Farm Address")
                      UserRole.VET -> Text("Clinic Address")
                    }
                  },
                  modifier =
                      Modifier
                        .fillMaxWidth()
                        .testTag(EditProfileScreenTestTags.ADDRESS_FIELD))
              Button(
                  onClick = onChangeLocation,
                  enabled = user !is Vet || isOwner,
                  modifier =
                      Modifier
                        .fillMaxWidth()
                        .testTag(EditProfileScreenTestTags.LOCATION_BUTTON)) {
                    Text("Change Location")
                  }

              // Default Vet Selection and Code Input (Farmers only)
              if (user is Farmer) {
                Spacer(modifier = Modifier.height(12.dp))

                if ((user as Farmer).linkedOffices.isEmpty()) {
                  Text(
                      text = "You need to add offices before choosing your default one.",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(vertical = 4.dp))
                }

                val officeNames = remember { mutableStateMapOf<String, String>() }

                // For each linked office, load their name
                (user as Farmer).linkedOffices.forEach { officeId ->
                  val vm: OfficeNameViewModel = viewModel(key = officeId)
                  val uiState by vm.uiState.collectAsState()

                  LaunchedEffect(officeId) {
                    vm.loadOffice(
                        uid = officeId, deletedOffice = "Deleted office", noneOffice = "Unassigned")
                  }

                  officeNames[officeId] = uiState
                }

                val selectedOfficeName = officeNames[selectedDefaultOffice] ?: "Unassigned"

                ExposedDropdownMenuBox(
                    expanded = expandedVetDropdown,
                    onExpandedChange = { expandedVetDropdown = !expandedVetDropdown }) {
                      OutlinedTextField(
                          value = selectedOfficeName,
                          onValueChange = {},
                          readOnly = true,
                          label = { Text("Default Office") },
                          trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVetDropdown)
                          },
                          modifier =
                              Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag(EditProfileScreenTestTags.DEFAULT_VET_DROPDOWN))

                      ExposedDropdownMenu(
                          expanded = expandedVetDropdown,
                          onDismissRequest = { expandedVetDropdown = false }) {
                            (user as Farmer).linkedOffices.forEach { officeId ->
                              val displayName = officeNames[officeId] ?: officeId

                              DropdownMenuItem(
                                  text = { Text(displayName) },
                                  onClick = {
                                    selectedDefaultOffice = officeId
                                    expandedVetDropdown = false
                                  })
                            }
                          }
                    }
                CollectedSwitch(collected, { collected = !collected }, true)
              }

              // Active Codes (Vets only)
              if (user is Vet) {
                Spacer(modifier = Modifier.height(16.dp))
                if (farmerCodes.isNotEmpty())
                    ActiveCodeList(CodeType.FARMER, farmerCodes, snackbarHostState)
                if (vetCodes.isNotEmpty()) ActiveCodeList(CodeType.VET, vetCodes, snackbarHostState)
              }

              Spacer(modifier = Modifier.weight(1f))

              // Save Changes Button
              Button(
                  onClick = {
                    val updatedDescription = description.ifBlank { null }
                    // Construct updated user object
                    val updatedUser =
                        when (userRole) {
                          UserRole.FARMER ->
                              (user as? Farmer)?.copy(
                                  firstname = firstname,
                                  lastname = lastname,
                                  address = pickedLocation,
                                  defaultOffice = selectedDefaultOffice,
                                  description = updatedDescription,
                                  collected = collected)
                          UserRole.VET -> {
                            manageOfficeVm.updateOffice(newAddress = pickedLocation)
                            (user as? Vet)?.copy(
                                firstname = firstname,
                                lastname = lastname,
                                address = pickedLocation,
                                description = updatedDescription,
                                collected = collected)
                          }
                        }
                    updatedUser?.let { onSave(it) }
                  },
                  modifier =
                      Modifier
                        .fillMaxWidth()
                        .testTag(EditProfileScreenTestTags.SAVE_BUTTON)) {
                    Text("Save Changes")
                  }
            }
      }

  if (showPhotoPickerDialog) {
    ImagePickerDialog(
      onDismiss = { showPhotoPickerDialog = false },
      onImageSelected = { uri ->
        chosenUri = uri
        scope.launch {
          val bitmap = uri.toBitmap(context).asImageBitmap()
          val result = imageCropper.crop(bmp = bitmap)
          when (result) {
            is CropResult.Cancelled -> {showPhotoPickerDialog = true}
            is CropError -> { TODO("Handle error") }
            is CropResult.Success -> { TODO("Upload photo") }
          }
        }
      }
    )
  }

  val cropState = imageCropper.cropState
  if(cropState != null) {
    // Setting this once
    LaunchedEffect(cropState) {
      // Force the region to be square by triggering an update
      val currentRegion = cropState.region
      val size = minOf(currentRegion.width, currentRegion.height)
      val centerX = currentRegion.center.x
      val centerY = currentRegion.center.y

      cropState.region = Rect(
        left = centerX - size / 2f,
        top = centerY - size / 2f,
        right = centerX + size / 2f,
        bottom = centerY + size / 2f
      )

      cropState.aspectLock = true
      cropState.shape = CircleCropShape


    }

    ImageCropperDialog(
      state = cropState,
      topBar = { ImageCropperDialogControls(cropState) },
      style = CropperStyle(
        autoZoom = true,
        guidelines = null,
        shapes = listOf(CircleCropShape),
        aspects = listOf(AspectRatio(1, 1))
    ))
  }



}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropperDialogControls(state: CropState) {
  TopAppBar(title = {},
    navigationIcon = {
      IconButton(onClick = { state.done(accept = false) }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel cropping")
      }
    },
    actions = {
      IconButton(onClick = {
        state.reset()
        // Force the region to be square by triggering an update
        val currentRegion = state.region
        val size = minOf(currentRegion.width, currentRegion.height)
        val centerX = currentRegion.center.x
        val centerY = currentRegion.center.y

        state.region = Rect(
          left = centerX - size / 2f,
          top = centerY - size / 2f,
          right = centerX + size / 2f,
          bottom = centerY + size / 2f
        )

        state.aspectLock = true
        state.shape = CircleCropShape
      }) {
        Icon(Icons.Default.Restore, contentDescription = "Restore")
      }
      IconButton(onClick = { state.done(accept = true) }, enabled = !state.accepted) {
        Icon(Icons.Default.Done, contentDescription = "Submit")
      }
    }
  )
}

@Composable
private fun BoxScope.DefaultControls(state: CropState) {
  val verticalControls =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
  CropperControls(
    isVertical = verticalControls,
    state = state,
    modifier = Modifier
      .align(if (!verticalControls) Alignment.BottomCenter else Alignment.CenterEnd)
      .padding(12.dp),
  )
}



// Created with the help of an LLM
fun Uri.toBitmap(context: Context): Bitmap {
  val source = ImageDecoder.createSource(context.contentResolver, this)
  return ImageDecoder.decodeBitmap(source)
}

/*
@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreviewFarmer() {
  val fakeViewModel =
      object : UserViewModel() {
        init {
          userRole = UserRole.FARMER
          user =
              Farmer(
                  uid = "1",
                  firstname = "Alice",
                  lastname = "Johnson",
                  email = "alice@farmmail.com",
                  address = Location(0.0, 0.0, "Farm"),
                  linkedVets = listOf("vet123", "vet456"),
                  defaultVet = "vet123")
        }
      }

  EditProfileScreen(userViewModel = fakeViewModel, onGoBack = {}, onSave = {}, onAddVetCode = {})
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreviewVet() {
  val fakeViewModel =
      object : UserViewModel() {
        init {
          userRole = UserRole.VET
          user =
              Vet(
                  uid = "2",
                  firstname = "Bob",
                  lastname = "Smith",
                  email = "bob@vetcare.com",
                  address = Location(0.0, 0.0, "Clinic")
        }
      }

  EditProfileScreen(userViewModel = fakeViewModel, onGoBack = {}, onSave = {}, onAddVetCode = {})
}
*/

@Composable
/** Creates an expandable list of every given code, along a "copy to clipboard" button */
fun ActiveCodeList(type: CodeType, codes: List<String>, snackbarHostState: SnackbarHostState) {
  var expanded by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    // Title bar
    Row(
        modifier =
            Modifier
              .fillMaxWidth()
              .clickable { expanded = !expanded }
              .padding(horizontal = 12.dp, vertical = 10.dp)
              .testTag(EditProfileScreenTestTags.dropdownTag(type.name)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Text(text = type.displayName())
          Icon(
              imageVector =
                  if (expanded) Icons.Default.KeyboardArrowDown
                  else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
              contentDescription = if (expanded) "Collapse" else "Expand")
        }

    // Codes
    if (expanded) {
      Column(modifier = Modifier
        .padding(vertical = 4.dp)
        .fillMaxWidth()) {
        codes.forEach { code ->
          Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier =
                        Modifier.testTag(EditProfileScreenTestTags.dropdownElementTag(type.name)))
                CopyToClipboardButton(code, snackbarHostState)
              }
        }
      }
    }
  }
}

@Composable
/** Creates a button that copies toCopy to the device clipboard */
fun CopyToClipboardButton(toCopy: String, snackbarHostState: SnackbarHostState) {
  val clipboardManager = LocalClipboardManager.current

  var copied by remember { mutableStateOf(false) }

  LaunchedEffect(copied) {
    if (copied) {
      snackbarHostState.showSnackbar("Copied to clipboard")
      copied = false
    }
  }

  IconButton(
      onClick = {
        clipboardManager.setText(AnnotatedString(toCopy))
        copied = true
      },
      modifier = Modifier
        .size(32.dp)
        .testTag(EditProfileScreenTestTags.COPY_CODE_BUTTON)) {
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy to clipboard",
            modifier = Modifier.size(16.dp))
      }
}

@Preview
@Composable
fun ActiveCodeListPreview() {
  AgriHealthAppTheme { EditProfileScreen() }
}
