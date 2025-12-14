package com.android.agrihealth.ui.user

import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun ViewUserScreen(viewModel: ViewUserViewModel, onBack: () -> Unit) {
  val uiState by viewModel.uiState

  val context = LocalContext.current

  LaunchedEffect(Unit) { viewModel.load() }

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
              IconButton(
                  onClick = onBack,
                  modifier = Modifier.testTag(ViewUserScreenTestTags.BACK_BUTTON)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                      (uiState as ViewUserUiState.Error).message,
                      modifier = Modifier.testTag(ViewUserScreenTestTags.ERROR_TEXT))
                }
            is ViewUserUiState.Success -> {
              val success = uiState as ViewUserUiState.Success
              ViewUserContent(user = success.user, officeName = success.officeName)
            }
          }
        }
      }
}

@Composable
private fun ViewUserContent(user: User, officeName: String?) {
  val scroll = rememberScrollState()
  val noInteraction = remember { MutableInteractionSource() }

  Column(
      modifier =
          Modifier.fillMaxSize()
              .verticalScroll(scroll)
              .padding(16.dp)
              .testTag(ViewUserScreenTestTags.CONTENT_COLUMN),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier.size(120.dp).testTag(ViewUserScreenTestTags.PROFILE_ICON))

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = "${user.firstname} ${user.lastname}",
            onValueChange = {},
            label = { Text("Name") },
            readOnly = true,
            interactionSource = noInteraction,
            enabled = false,
            modifier = Modifier.fillMaxWidth().testTag(ViewUserScreenTestTags.NAME_FIELD))

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = user.role.displayString(),
            onValueChange = {},
            label = { Text("Role") },
            readOnly = true,
            interactionSource = noInteraction,
            enabled = false,
            modifier = Modifier.fillMaxWidth().testTag(ViewUserScreenTestTags.ROLE_FIELD))

        Spacer(Modifier.height(8.dp))

        if (user is Vet) {
          OutlinedTextField(
              value = officeName ?: "None",
              onValueChange = {},
              label = { Text("Office") },
              readOnly = true,
              interactionSource = noInteraction,
              enabled = false,
              modifier = Modifier.fillMaxWidth().testTag(ViewUserScreenTestTags.OFFICE_FIELD))

          Spacer(Modifier.height(8.dp))
        }

        user.address?.let {
          OutlinedTextField(
              value = "Not implemented yet",
              onValueChange = {},
              label = { Text("Address") },
              readOnly = true,
              interactionSource = noInteraction,
              enabled = false,
              modifier = Modifier.fillMaxWidth().testTag(ViewUserScreenTestTags.ADDRESS_FIELD))

          Spacer(Modifier.height(8.dp))
        }

        if (!user.description.isNullOrBlank()) {
          OutlinedTextField(
              value = user.description!!,
              onValueChange = {},
              label = { Text("Description") },
              readOnly = true,
              interactionSource = noInteraction,
              enabled = false,
              modifier = Modifier.fillMaxWidth().testTag(ViewUserScreenTestTags.DESCRIPTION_FIELD))
        }

        Spacer(Modifier.height(32.dp))
      }
}
