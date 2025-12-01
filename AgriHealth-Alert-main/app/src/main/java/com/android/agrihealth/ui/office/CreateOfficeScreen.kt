package com.android.agrihealth.ui.office

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.ui.office.CreateOfficeScreenTestTags.CREATE_BUTTON
import com.android.agrihealth.ui.office.CreateOfficeScreenTestTags.DESCRIPTION_FIELD
import com.android.agrihealth.ui.office.CreateOfficeScreenTestTags.NAME_FIELD
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import com.android.agrihealth.ui.user.UserViewModel

object CreateOfficeScreenTestTags {
  const val NAME_FIELD = "CreateOfficeNameField"
  const val DESCRIPTION_FIELD = "CreateOfficeDescriptionField"
  const val CREATE_BUTTON = "CreateOfficeButtonConfirm"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfficeScreen(
    userViewModel: UserViewModel = viewModel(),
    onGoBack: () -> Unit,
    onCreated: () -> Unit,
) {
  val vm: CreateOfficeViewModel =
      viewModel(
          factory =
              object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                  return CreateOfficeViewModel(userViewModel) as T
                }
              })

  val uiState by vm.uiState.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Create Office") },
            navigationIcon = {
              IconButton(onClick = onGoBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            },
            modifier = Modifier.testTag(TOP_BAR))
      }) { padding ->
        Column(
            modifier =
                Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

              // Input: Office Name
              OutlinedTextField(
                  value = uiState.name,
                  onValueChange = vm::onNameChange,
                  label = { Text("Office Name") },
                  modifier = Modifier.fillMaxWidth().testTag(NAME_FIELD))

              // Input: Office Description
              OutlinedTextField(
                  value = uiState.description,
                  onValueChange = vm::onDescriptionChange,
                  label = { Text("Description (optional)") },
                  modifier = Modifier.fillMaxWidth().testTag(DESCRIPTION_FIELD))

              // Error text (only shown when not null)
              if (uiState.error != null) {
                Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
              }

              Button(
                  onClick = { vm.createOffice(onCreated) },
                  enabled = !uiState.loading,
                  modifier = Modifier.fillMaxWidth().testTag(CREATE_BUTTON)) {
                    if (uiState.loading) {
                      CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                      Spacer(Modifier.width(8.dp))
                    }

                    Text("Create Office")
                  }
            }
      }
}
