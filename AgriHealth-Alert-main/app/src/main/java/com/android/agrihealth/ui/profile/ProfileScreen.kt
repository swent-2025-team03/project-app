package com.android.agrihealth.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.connection.ConnectionRepositoryProvider
import com.android.agrihealth.data.model.user.*
import com.android.agrihealth.ui.authentification.SignInScreenTestTags.EMAIL_FIELD
import com.android.agrihealth.ui.authentification.SignInScreenTestTags.PASSWORD_FIELD
import com.android.agrihealth.ui.common.OfficeNameViewModel
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.NavigationTestTags.GO_BACK_BUTTON
import com.android.agrihealth.ui.overview.OverviewScreenTestTags.LOGOUT_BUTTON
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.ADDRESS_FIELD
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.CODE_BUTTON_FARMER
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.DEFAULT_OFFICE_FIELD
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.DESCRIPTION_FIELD
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.EDIT_BUTTON
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.MANAGE_OFFICE_BUTTON
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.NAME_TEXT
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.PROFILE_IMAGE
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import com.android.agrihealth.ui.report.CollectedSwitch
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.ui.user.UserViewModelContract

object ProfileScreenTestTags {

  const val TOP_BAR = "TopBar"
  const val PROFILE_IMAGE = "ProfileImage"
  const val NAME_TEXT = "NameText"
  const val EDIT_BUTTON = "EditButton"
  const val DESCRIPTION_FIELD = "DescriptionField"
  const val ADDRESS_FIELD = "AddressField"
  const val DEFAULT_OFFICE_FIELD = "DefaultOfficeField"
  const val CODE_BUTTON_FARMER = "CodeButtonFarmer"
  const val MANAGE_OFFICE_BUTTON = "ManageOfficeButton"
  const val GENERATE_CODE_BUTTON = "GenerateCodeButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModelContract = viewModel<UserViewModel>(),
    onGoBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onCodeFarmer: () -> Unit = {},
    onManageOffice: () -> Unit = {},
) {

  val user by userViewModel.user.collectAsState()
  val userRole = user.role

  val factory = remember {
    object : androidx.lifecycle.ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CodesViewModel(userViewModel, ConnectionRepositoryProvider.farmerToOfficeRepository)
            as T
      }
    }
  }
  val codesViewModel: CodesViewModel = viewModel(factory = factory)

  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Profile - ${userRole.displayString()}",
                  style = MaterialTheme.typography.titleLarge,
                  modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
            },
            navigationIcon = {
              IconButton(onClick = onGoBack, modifier = Modifier.testTag(GO_BACK_BUTTON)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
              }
            },
            actions = {
              IconButton(onClick = onLogout, modifier = Modifier.testTag(LOGOUT_BUTTON)) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
              }
            },
            modifier = Modifier.testTag(TOP_BAR))
      },
      snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier =
                Modifier.padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))

              // Profile Image Placeholder
              Icon(
                  imageVector =
                      Icons.Default.AccountCircle, // To change to actual image when available
                  contentDescription = "Profile Picture",
                  modifier = Modifier.size(120.dp).clip(CircleShape).testTag(PROFILE_IMAGE),
                  tint = MaterialTheme.colorScheme.primary)

              Spacer(modifier = Modifier.height(8.dp))

              // Name + Edit Icon
              Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(
                    modifier = Modifier.width(32.dp)) // Solution to center the name with edit icon
                Text(
                    text = "${user.firstname} ${user.lastname}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag(NAME_TEXT))
                IconButton(onClick = onEditProfile, modifier = Modifier.testTag(EDIT_BUTTON)) {
                  Icon(Icons.Default.Edit, contentDescription = "Edit profile")
                }
              }

              // Description
              Spacer(modifier = Modifier.height(12.dp))
              OutlinedTextField(
                  value = user.description ?: "",
                  onValueChange = {},
                  label = { Text("Description") },
                  enabled = false,
                  singleLine = false,
                  modifier = Modifier.fillMaxWidth().testTag(DESCRIPTION_FIELD))

              if (!user.isGoogleAccount) {
                Spacer(modifier = Modifier.height(12.dp))

                // Email
                OutlinedTextField(
                    value = user.email,
                    onValueChange = {},
                    label = { Text("Email address") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().testTag(EMAIL_FIELD))

                Spacer(modifier = Modifier.height(12.dp))

                // Password
                OutlinedTextField(
                    value =
                        "********", // For now the password is not in the user model, so we use a
                    // placeholder
                    onValueChange = {},
                    label = { Text("Password") },
                    enabled = false,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().testTag(PASSWORD_FIELD))
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Address (Location)
              OutlinedTextField(
                  value = user.address?.name ?: "",
                  singleLine = true,
                  onValueChange = {},
                  label = {
                    when (userRole) {
                      UserRole.FARMER -> Text("Farm Address")
                      UserRole.VET -> Text("Clinic Address")
                    }
                  },
                  enabled = false,
                  modifier = Modifier.fillMaxWidth().testTag(ADDRESS_FIELD))

              // Default Vet (Farmers only)
              if (user is Farmer) {
                val officeNameVm: OfficeNameViewModel =
                    viewModel(key = (user as Farmer).defaultOffice)
                val officeName by officeNameVm.uiState.collectAsState()
                LaunchedEffect(user) {
                  officeNameVm.loadOffice(
                      uid = (user as Farmer).defaultOffice,
                      deletedOffice = "Deleted office",
                      noneOffice = "Unassigned")
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = officeName,
                    onValueChange = {},
                    label = { Text("Default Office") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().testTag(DEFAULT_OFFICE_FIELD))
              }

              Spacer(modifier = Modifier.height(24.dp))
              CollectedSwitch(user.collected)
              Spacer(modifier = Modifier.height(12.dp))

              if (user is Farmer) {
                Button(
                    onClick = onCodeFarmer,
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally).testTag(CODE_BUTTON_FARMER)) {
                      Text("Add new Office with Code")
                    }
              }

              if (user is Vet) {
                GenerateCode(
                    codesViewModel,
                    snackbarHostState,
                    Modifier.align(Alignment.CenterHorizontally)
                        .testTag(ProfileScreenTestTags.GENERATE_CODE_BUTTON))

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onManageOffice,
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally)
                            .testTag(MANAGE_OFFICE_BUTTON)) {
                      Text("Manage My Office")
                    }
              }
            }
      }
}

/* If you want to use the preview, just de-comment this block.
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreviewFarmer() {
    // Create a fake UserViewModel that matches the real one (no mutableState)
    val fakeViewModel = object : UserViewModel() {
        init {
            userRole = UserRole.FARMER
            userId = "FARMER_001"
            user = Farmer(
                uid = "1",
                firstname = "Alice",
                lastname = "Johnson",
                email = "alice@farmmail.com",
                address = Location(0.0, 0.0, "Farm"),
                linkedVets = listOf("vet123", "vet456"),
                defaultVet = "vet123"
            )
        }
    }

    ProfileScreen(
        userViewModel = fakeViewModel,
        onGoBack = {},
        onLogout = {},
        onEditProfile = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreviewVet() {
    val fakeViewModel = object : UserViewModel() {
        init {
            userRole = UserRole.VET
            userId = "VET_001"
            user = Vet(
                uid = "2",
                firstname = "Bob",
                lastname = "Smith",
                email = "bob@vetcare.com",
                address = Location(0.0, 0.0, "Clinic"),
                linkedFarmers = listOf("farmer123", "farmer456")
            )
        }
    }

    ProfileScreen(
        userViewModel = fakeViewModel,
        onGoBack = {},
        onLogout = {},
        onEditProfile = {}
    )
}
*/
