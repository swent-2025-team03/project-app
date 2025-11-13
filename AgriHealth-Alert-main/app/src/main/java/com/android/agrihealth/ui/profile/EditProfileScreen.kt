package com.android.agrihealth.ui.profile

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.user.*
import com.android.agrihealth.ui.navigation.NavigationTestTags.GO_BACK_BUTTON
import com.android.agrihealth.ui.profile.EditProfileScreenTestTags.PASSWORD_BUTTON
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.PROFILE_IMAGE
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.launch

object EditProfileScreenTestTags {
  const val FIRSTNAME_FIELD = "FirstNameField"
  const val LASTNAME_FIELD = "LastNameField"
  const val PASSWORD_FIELD = "PasswordField"
  const val ADDRESS_FIELD = "EditAddressField"
  const val DEFAULT_VET_DROPDOWN = "DefaultVetDropdown"
  const val CODE_FIELD = "VetCodeField"
  const val ADD_CODE_BUTTON = "AddVetButton"
  const val SAVE_BUTTON = "SaveButton"
  const val PASSWORD_BUTTON = "PasswordButton"
  const val ACTIVE_CODES_DROPDOWN = "ActiveCodesDropdown"
  const val ACTIVE_CODE_ELEMENT = "ActiveCodeListElement"
  const val COPY_CODE_BUTTON = "CopyActiveCodeListElementButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userViewModel: UserViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onSave: (User) -> Unit = { userViewModel.updateUser(it) },
    onPasswordChange: () -> Unit = {},
    showOnlyVetField: Boolean = false
) {
  val user by userViewModel.user.collectAsState()
  val userRole = user.role

  val factory = remember {
    object : androidx.lifecycle.ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(userViewModel) as T
      }
    }
  }
  val profileViewModel: ProfileViewModel = viewModel(factory = factory)

  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val vetClaimMessage by profileViewModel.vetClaimMessage.collectAsState()
  LaunchedEffect(vetClaimMessage) { vetClaimMessage?.let { snackbarHostState.showSnackbar(it) } }

  // Local mutable states
  var firstname by remember { mutableStateOf(user?.firstname ?: "") }
  var lastname by remember { mutableStateOf(user?.lastname ?: "") }
  var address by remember { mutableStateOf(user?.address?.toString() ?: "") }

  // Farmer-specific states
  var selectedDefaultVet by remember { mutableStateOf((user as? Farmer)?.defaultVet ?: "") }
  var expandedVetDropdown by remember { mutableStateOf(false) }
  var vetCode by remember { mutableStateOf("") }

  // Focus requester for vet code input
  val codeFocusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current

  // Vet-specific states
  var expandedCodesDropdown by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Edit Profile", style = MaterialTheme.typography.titleMedium) },
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
                Modifier.padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

              if (!showOnlyVetField) {

                // Profile Image Placeholder
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(120.dp).clip(CircleShape).testTag(PROFILE_IMAGE),
                    tint = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(24.dp))

                // First name
                OutlinedTextField(
                    value = firstname,
                    onValueChange = { firstname = it },
                    label = { Text("First Name") },
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditProfileScreenTestTags.FIRSTNAME_FIELD))

                Spacer(modifier = Modifier.height(12.dp))

                // Last name
                OutlinedTextField(
                    value = lastname,
                    onValueChange = { lastname = it },
                    label = { Text("Last Name") },
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditProfileScreenTestTags.LASTNAME_FIELD))

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
                          Modifier.fillMaxWidth().testTag(EditProfileScreenTestTags.PASSWORD_FIELD),
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

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = {
                      when (userRole) {
                        UserRole.FARMER -> Text("Farm Address")
                        UserRole.VET -> Text("Clinic Address")
                      }
                    },
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditProfileScreenTestTags.ADDRESS_FIELD))
                // TODO: right now addresses are displayed as Location(...), I think we will change
                // this once we work on the implementation of Location in more details.

                // Default Vet Selection and Code Input (Farmers only)
                if (user is Farmer) {
                  Spacer(modifier = Modifier.height(12.dp))

                  if ((user as Farmer).linkedVets.isEmpty()) {
                    Text(
                        text = "You need to add vets before choosing your default one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp))
                  }

                  ExposedDropdownMenuBox(
                      expanded = expandedVetDropdown,
                      onExpandedChange = { expandedVetDropdown = !expandedVetDropdown }) {
                        OutlinedTextField(
                            value = selectedDefaultVet,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Default Vet") },
                            trailingIcon = {
                              ExposedDropdownMenuDefaults.TrailingIcon(
                                  expanded = expandedVetDropdown)
                            },
                            modifier =
                                Modifier.menuAnchor()
                                    .fillMaxWidth()
                                    .testTag(EditProfileScreenTestTags.DEFAULT_VET_DROPDOWN))
                        ExposedDropdownMenu(
                            expanded = expandedVetDropdown,
                            onDismissRequest = { expandedVetDropdown = false }) {
                              (user as Farmer).linkedVets.forEach { vetId ->
                                DropdownMenuItem(
                                    text = { Text("Vet $vetId") }, // Placeholder name display
                                    onClick = {
                                      selectedDefaultVet = vetId
                                      expandedVetDropdown = false
                                    })
                              }
                            }
                      }
                }
              }

              if (user is Farmer) {

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = vetCode,
                    onValueChange = { vetCode = it },
                    label = { Text("Enter Vet Code") },
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditProfileScreenTestTags.CODE_FIELD))

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                      if (vetCode.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter a code.") }
                      } else {
                        profileViewModel.claimVetCode(vetCode)
                      }
                    },
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally)
                            .testTag(EditProfileScreenTestTags.ADD_CODE_BUTTON)) {
                      Text("Add Vet")
                    }
              }

              // Active Codes (Vets only)
              if (user is Vet) {
                Spacer(modifier = Modifier.height(16.dp))
                val codes = (user as Vet).validCodes
                ActiveCodeList(codes, snackbarHostState)
              }

              Spacer(modifier = Modifier.weight(1f))

              // Save Changes Button
              Button(
                  onClick = {
                    // Construct updated user object
                    val updatedUser =
                        when (userRole) {
                          UserRole.FARMER ->
                              (user as? Farmer)?.copy(
                                  firstname = firstname,
                                  lastname = lastname,
                                  address = user.address?.copy(name = address),
                                  defaultVet = selectedDefaultVet)
                          UserRole.VET ->
                              (user as? Vet)?.copy(
                                  firstname = firstname,
                                  lastname = lastname,
                                  address = user.address?.copy(name = address))
                        }
                    updatedUser?.let { onSave(it) }
                  },
                  modifier =
                      Modifier.fillMaxWidth().testTag(EditProfileScreenTestTags.SAVE_BUTTON)) {
                    Text("Save Changes")
                  }
            }
      }
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
                  address = Location(0.0, 0.0, "Clinic"),
                  linkedFarmers = listOf("farmer123", "farmer456"))
        }
      }

  EditProfileScreen(userViewModel = fakeViewModel, onGoBack = {}, onSave = {}, onAddVetCode = {})
}
*/

@Composable
fun ActiveCodeList(codes: List<String>, snackbarHostState: SnackbarHostState) {
  var expanded by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    // Title bar
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag(EditProfileScreenTestTags.ACTIVE_CODES_DROPDOWN),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Text(text = "Active codes")
          Icon(
              imageVector =
                  if (expanded) Icons.Default.KeyboardArrowDown
                  else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
              contentDescription = if (expanded) "Collapse" else "Expand")
        }

    // Codes
    if (expanded) {
      Column(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        codes.forEach { code ->
          Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag(EditProfileScreenTestTags.ACTIVE_CODE_ELEMENT))
                CopyToClipboardButton(code, snackbarHostState)
              }
        }
      }
    }
  }
}

@Composable
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
      modifier = Modifier.size(32.dp).testTag(EditProfileScreenTestTags.COPY_CODE_BUTTON)) {
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
