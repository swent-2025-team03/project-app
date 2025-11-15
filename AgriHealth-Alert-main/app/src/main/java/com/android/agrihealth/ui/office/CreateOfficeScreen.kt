package com.android.agrihealth.ui.office

import androidx.compose.foundation.layout.*
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
import com.android.agrihealth.data.model.office.OfficeRepositoryFirestore
import com.android.agrihealth.ui.office.CreateOfficeScreenTestTags.ADDRESS_FIELD
import com.android.agrihealth.ui.office.CreateOfficeScreenTestTags.CREATE_BUTTON
import com.android.agrihealth.ui.office.CreateOfficeScreenTestTags.DESCRIPTION_FIELD
import com.android.agrihealth.ui.office.CreateOfficeScreenTestTags.NAME_FIELD
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import com.android.agrihealth.ui.user.UserViewModel

object CreateOfficeScreenTestTags {
  const val NAME_FIELD = "CreateOfficeNameField"
  const val DESCRIPTION_FIELD = "CreateOfficeDescriptionField"
  const val ADDRESS_FIELD = "CreateOfficeAddressField"
  const val CREATE_BUTTON = "CreateOfficeButtonConfirm"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfficeScreen(
    userViewModel: UserViewModel = viewModel(),
    onGoBack: () -> Unit,
    onCreated: () -> Unit,
    viewModelFactory: ViewModelProvider.Factory? = null
) {
  val factory = remember {
    viewModelFactory
        ?: object : ViewModelProvider.Factory {
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateOfficeViewModel(
                userViewModel = userViewModel, officeRepository = OfficeRepositoryFirestore())
                as T
          }
        }
  }

  val vm: CreateOfficeViewModel = viewModel(factory = factory)

  val name by vm.name.collectAsState()
  val description by vm.description.collectAsState()
  val address by vm.address.collectAsState()
  val loading by vm.loading.collectAsState()
  val error by vm.error.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Create Office") },
            navigationIcon = {
              IconButton(onClick = onGoBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            },
            modifier = Modifier.testTag(TOP_BAR))
      }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
          OutlinedTextField(
              value = name,
              onValueChange = vm::onNameChange,
              label = { Text("Office Name") },
              modifier = Modifier.fillMaxWidth().testTag(NAME_FIELD))

          Spacer(Modifier.height(16.dp))

          OutlinedTextField(
              value = description,
              onValueChange = vm::onDescriptionChange,
              label = { Text("Description (optional)") },
              modifier = Modifier.fillMaxWidth().testTag(DESCRIPTION_FIELD))

          Spacer(Modifier.height(16.dp))

          OutlinedTextField(
              value = address,
              onValueChange = vm::onAddressChange,
              label = { Text("Address (optional)") },
              modifier = Modifier.fillMaxWidth().testTag(ADDRESS_FIELD))

          Spacer(Modifier.height(24.dp))

          if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
          }

          Button(
              onClick = { vm.createOffice(onCreated) },
              enabled = !loading,
              modifier = Modifier.fillMaxWidth().testTag(CREATE_BUTTON)) {
                Text(if (loading) "Creating..." else "Create Office")
              }
        }
      }
}
