package com.android.agrihealth.ui.user

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.agrihealth.data.model.office.OfficeRepositoryFirestore
import com.android.agrihealth.data.model.user.*

object ViewUserScreenTestTags {
  const val TOP_BAR = "ViewUserTopBar"
  const val BACK_BUTTON = "ViewUserBackButton"
  const val LOADING_INDICATOR = "ViewUserLoadingIndicator"
  const val ERROR_TEXT = "ViewUserErrorText"
  const val CONTENT_COLUMN = "ViewUserContentColumn"
  const val PROFILE_ICON = "ViewUserProfileIcon"
  const val NAME_FIELD = "ViewUserNameField"
  const val ROLE_FIELD = "ViewUserRoleField"
  const val OFFICE_FIELD = "ViewUserOfficeField"
  const val ADDRESS_FIELD = "ViewUserAddressField"
  const val DESCRIPTION_FIELD = "ViewUserDescriptionField"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewUserScreen(
    viewModel: ViewUserViewModel,
    onBack: () -> Unit,
    userViewModel: UserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
  val currentUser by userViewModel.user.collectAsState()
  val uiState by viewModel.uiState

  val context = LocalContext.current

  LaunchedEffect(Unit) { viewModel.load(currentUser) }

  // Handle errors with a toast
  if (uiState is ViewUserUiState.Error) {
    LaunchedEffect(uiState) {
      Toast.makeText(context, (uiState as ViewUserUiState.Error).message, Toast.LENGTH_LONG).show()
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              if (uiState is ViewUserUiState.Success) {
                val user = (uiState as ViewUserUiState.Success).user
                Text("${user.firstname} ${user.lastname}")
              } else {
                Text("User")
              }
            },
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.testTag(ViewUserScreenTestTags.BACK_BUTTON))
              }
            },
            modifier = Modifier.testTag(ViewUserScreenTestTags.TOP_BAR))
      }) { padding ->
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Box(modifier = Modifier.padding(padding)) {
          when (uiState) {
            is ViewUserUiState.Loading ->
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                  CircularProgressIndicator(
                      modifier = Modifier.testTag(ViewUserScreenTestTags.LOADING_INDICATOR))
                }
            is ViewUserUiState.Error ->
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                  Text(
                      "Unable to load user.",
                      modifier = Modifier.testTag(ViewUserScreenTestTags.ERROR_TEXT))
                }
            is ViewUserUiState.Success -> {
              val user = (uiState as ViewUserUiState.Success).user
              ViewUserContent(user = user)
            }
          }
        }
      }
}

@Composable
private fun ViewUserContent(user: User) {
  val scroll = rememberScrollState()

  Column(
      modifier =
          Modifier.fillMaxSize()
              .verticalScroll(scroll)
              .padding(16.dp)
              .testTag(ViewUserScreenTestTags.CONTENT_COLUMN),
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Profile icon TODO replace with real profile picture when implemented
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier.size(120.dp).testTag(ViewUserScreenTestTags.PROFILE_ICON))

        Spacer(Modifier.height(24.dp))

        // Name field
        OutlinedTextField(
            value = "${user.firstname} ${user.lastname}",
            onValueChange = {},
            label = { Text("Name") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().testTag(ViewUserScreenTestTags.NAME_FIELD))

        Spacer(Modifier.height(8.dp))

        // Role
        OutlinedTextField(
            value = user.role.displayString(),
            onValueChange = {},
            label = { Text("Role") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().testTag(ViewUserScreenTestTags.ROLE_FIELD))

        Spacer(Modifier.height(8.dp))

        // Office name for vets
        if (user is Vet) {
          val officeRepo = OfficeRepositoryFirestore()
          var officeName by remember { mutableStateOf("Loading…") }

          LaunchedEffect(user.officeId) {
            val id = user.officeId
            if (id == null) {
              officeName = "None"
            } else {
              try {
                val office = officeRepo.getOffice(id).getOrNull()
                officeName = office?.name ?: "None"
              } catch (_: Exception) {
                officeName = "None"
              }
            }
          }

          OutlinedTextField(
              value = officeName,
              onValueChange = {},
              label = { Text("Office") },
              readOnly = true,
              modifier = Modifier.fillMaxWidth().testTag(ViewUserScreenTestTags.OFFICE_FIELD))

          Spacer(Modifier.height(8.dp))
        }

        // Address
        // TODO display real address once implemented.
        user.address?.let {
          OutlinedTextField(
              value = "Not implemented yet",
              onValueChange = {},
              label = { Text("Address") },
              readOnly = true,
              modifier = Modifier.fillMaxWidth().testTag(ViewUserScreenTestTags.ADDRESS_FIELD))

          Spacer(Modifier.height(8.dp))
        }

        // Description
        if (!user.description.isNullOrBlank()) {
          OutlinedTextField(
              value = user.description!!,
              onValueChange = {},
              label = { Text("Description") },
              readOnly = true,
              modifier = Modifier.fillMaxWidth().testTag(ViewUserScreenTestTags.DESCRIPTION_FIELD))
        }

        Spacer(Modifier.height(32.dp))
      }
}
