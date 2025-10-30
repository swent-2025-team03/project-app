package com.android.agrihealth.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.user.*
import com.android.agrihealth.ui.user.UserViewModel

object ProfileScreenTestTags {

  const val GO_BACK_BUTTON = "GoBackButton"
  const val LOGOUT_BUTTON = "LogoutButton"
  const val TOP_BAR = "TopBar"
  const val PROFILE_IMAGE = "ProfileImage"
  const val NAME_TEXT = "NameText"
  const val EDIT_BUTTON = "EditButton"
  const val EMAIL_FIELD = "EmailField"
  const val PASSWORD_FIELD = "PasswordField"
  const val ADDRESS_FIELD = "AddressField"
  const val DEFAULT_VET_FIELD = "DefaultVetField"
  const val CODE_BUTTON = "CodeButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onCode: () -> Unit = {}
) {
  val user = userViewModel.user
  val userRole = userViewModel.userRole

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Profile - ${userRole.displayString()}",
                  style = MaterialTheme.typography.titleMedium)
            },
            navigationIcon = {
              IconButton(onClick = onGoBack, modifier = Modifier.testTag("GoBackButton")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
              }
            },
            actions = {
              IconButton(onClick = onLogout, modifier = Modifier.testTag("LogoutButton")) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
              }
            },
            modifier = Modifier.testTag("TopBar"))
      }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))

              // Profile Image Placeholder
              Icon(
                  imageVector =
                      Icons.Default.AccountCircle, // To change to actual image when available
                  contentDescription = "Profile Picture",
                  modifier = Modifier.size(120.dp).clip(CircleShape).testTag("ProfileImage"),
                  tint = MaterialTheme.colorScheme.primary)

              Spacer(modifier = Modifier.height(8.dp))

              // Name + Edit Icon
              Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(
                    modifier = Modifier.width(32.dp)) // Solution to center the name with edit icon
                Text(
                    text = "${user?.firstname ?: "Unknown"} ${user?.lastname ?: ""}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag("NameText"))
                IconButton(onClick = onEditProfile, modifier = Modifier.testTag("EditButton")) {
                  Icon(Icons.Default.Edit, contentDescription = "Edit profile")
                }
              }

              Spacer(modifier = Modifier.height(24.dp))

              // Email
              OutlinedTextField(
                  value = user?.email ?: "",
                  onValueChange = {},
                  label = { Text("Email address") },
                  enabled = false,
                  modifier = Modifier.fillMaxWidth().testTag("EmailField"))

              Spacer(modifier = Modifier.height(12.dp))

              // Password
              OutlinedTextField(
                  value = "********", // For now the password is not in the user model, so we use a
                  // placeholder
                  onValueChange = {},
                  label = { Text("Password") },
                  enabled = false,
                  visualTransformation = PasswordVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                  modifier = Modifier.fillMaxWidth().testTag("PasswordField"))

              Spacer(modifier = Modifier.height(12.dp))

              // Address (Location)
              OutlinedTextField(
                  value = user?.address?.toString() ?: "",
                  onValueChange = {},
                  label = {
                    when (userRole) {
                      UserRole.FARMER -> Text("Farm Address")
                      UserRole.VET -> Text("Clinic Address")
                    }
                  },
                  enabled = false,
                  modifier = Modifier.fillMaxWidth().testTag("AddressField"))

              // Default Vet (Farmers only)
              if (user is Farmer) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = user.defaultVet ?: "",
                    onValueChange = {},
                    label = { Text("Default Vet") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().testTag("DefaultVetField"))
              }

              Spacer(modifier = Modifier.height(24.dp))

              Button(
                  onClick = onCode,
                  modifier = Modifier.align(Alignment.CenterHorizontally).testTag("CodeButton")) {
                    when (userRole) {
                      UserRole.FARMER -> Text("Add new Vet with Code")
                      UserRole.VET -> Text("Generate new Farmer's Code")
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
