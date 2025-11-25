package com.android.agrihealth.ui.office

import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.ui.common.AuthorName
import com.android.agrihealth.ui.user.UserViewModel

object ViewOfficeScreenTestTags {
  const val TOP_BAR = "ViewOfficeTopBar"
  const val BACK_BUTTON = "ViewOfficeBackButton"
  const val LOADING_INDICATOR = "ViewOfficeLoadingIndicator"
  const val ERROR_TEXT = "ViewOfficeErrorText"
  const val CONTENT_COLUMN = "ViewOfficeContentColumn"
  const val OFFICE_ICON = "ViewOfficeIcon"
  const val NAME_FIELD = "ViewOfficeNameField"
  const val ADDRESS_FIELD = "ViewOfficeAddressField"
  const val DESCRIPTION_FIELD = "ViewOfficeDescriptionField"
  const val VET_LIST = "ViewOfficeVetList"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewOfficeScreen(
    viewModel: ViewOfficeViewModel,
    onBack: () -> Unit,
    onOpenUser: (String) -> Unit = {},
    userViewModel: UserViewModel = viewModel()
) {
  val currentUser by userViewModel.user.collectAsState()
  val uiState by viewModel.uiState

  val context = LocalContext.current

  LaunchedEffect(Unit) { viewModel.load() }

  if (uiState is ViewOfficeUiState.Error) {
    LaunchedEffect(uiState) {
      Toast.makeText(context, (uiState as ViewOfficeUiState.Error).message, Toast.LENGTH_LONG)
          .show()
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              if (uiState is ViewOfficeUiState.Success) {
                val office = (uiState as ViewOfficeUiState.Success).office
                Text(office.name)
              } else {
                Text("Office")
              }
            },
            navigationIcon = {
              IconButton(
                  onClick = onBack,
                  modifier = Modifier.testTag(ViewOfficeScreenTestTags.BACK_BUTTON)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
            },
            modifier = Modifier.testTag(ViewOfficeScreenTestTags.TOP_BAR))
      }) { padding ->
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Box(modifier = Modifier.padding(padding)) {
          when (uiState) {
            is ViewOfficeUiState.Loading ->
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                  CircularProgressIndicator(
                      modifier = Modifier.testTag(ViewOfficeScreenTestTags.LOADING_INDICATOR))
                }
            is ViewOfficeUiState.Error ->
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                  Text(
                      (uiState as ViewOfficeUiState.Error).message,
                      modifier = Modifier.testTag(ViewOfficeScreenTestTags.ERROR_TEXT))
                }
            is ViewOfficeUiState.Success -> {
              val success = uiState as ViewOfficeUiState.Success
              ViewOfficeContent(office = success.office, onOpenUser = onOpenUser)
            }
          }
        }
      }
}

@Composable
private fun ViewOfficeContent(office: Office, onOpenUser: (String) -> Unit) {
  val scroll = rememberScrollState()
  val noInteraction = remember { MutableInteractionSource() }

  Column(
      modifier =
          Modifier.fillMaxSize()
              .verticalScroll(scroll)
              .padding(16.dp)
              .testTag(ViewOfficeScreenTestTags.CONTENT_COLUMN),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Office",
            modifier = Modifier.size(120.dp).testTag(ViewOfficeScreenTestTags.OFFICE_ICON))

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = office.name,
            onValueChange = {},
            label = { Text("Office Name") },
            readOnly = true,
            interactionSource = noInteraction,
            enabled = false,
            modifier = Modifier.fillMaxWidth().testTag(ViewOfficeScreenTestTags.NAME_FIELD))

        Spacer(Modifier.height(8.dp))

        // TODO: add once location is implemented in users and offices
        office.address?.let {
          OutlinedTextField(
              value = "Not implemented yet",
              onValueChange = {},
              label = { Text("Address") },
              readOnly = true,
              interactionSource = noInteraction,
              enabled = false,
              modifier = Modifier.fillMaxWidth().testTag(ViewOfficeScreenTestTags.ADDRESS_FIELD))

          Spacer(Modifier.height(8.dp))
        }

        if (!office.description.isNullOrBlank()) {
          OutlinedTextField(
              value = office.description,
              onValueChange = {},
              label = { Text("Description") },
              readOnly = true,
              interactionSource = noInteraction,
              enabled = false,
              modifier =
                  Modifier.fillMaxWidth().testTag(ViewOfficeScreenTestTags.DESCRIPTION_FIELD))

          Spacer(Modifier.height(8.dp))
        }

        if (office.vets.isNotEmpty()) {
          Text("Vets in this office:", style = MaterialTheme.typography.titleMedium)
          Spacer(Modifier.height(8.dp))

          LazyColumn(
              modifier =
                  Modifier.fillMaxWidth()
                      .heightIn(max = 300.dp)
                      .testTag(ViewOfficeScreenTestTags.VET_LIST)) {
                items(office.vets) { vetId -> AuthorName(vetId, onClick = { onOpenUser(vetId) }) }
              }

          Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(32.dp))
      }
}
