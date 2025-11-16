package com.android.agrihealth.ui.office

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.android.agrihealth.core.design.theme.StatusColors
import com.android.agrihealth.data.model.office.OfficeRepositoryFirestore
import com.android.agrihealth.ui.common.AuthorName
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.CREATE_OFFICE_BUTTON
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.JOIN_OFFICE_BUTTON
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.LEAVE_OFFICE_BUTTON
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.OFFICE_ADDRESS
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.OFFICE_DESCRIPTION
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.OFFICE_NAME
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.OFFICE_VET_LIST
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import com.android.agrihealth.ui.user.UserViewModel

object ManageOfficeScreenTestTags {
  const val CREATE_OFFICE_BUTTON = "CreateOfficeButton"
  const val JOIN_OFFICE_BUTTON = "JoinOfficeButton"
  const val OFFICE_NAME = "OfficeName"
  const val OFFICE_ADDRESS = "OfficeAddress"
  const val OFFICE_DESCRIPTION = "OfficeDescription"
  const val OFFICE_VET_LIST = "OfficeVetList"
  const val LEAVE_OFFICE_BUTTON = "LeaveOfficeButton"
}

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOfficeScreen(
    userViewModel: UserViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onCreateOffice: () -> Unit = {},
    onJoinOffice: () -> Unit = {},
    viewModelFactory: ViewModelProvider.Factory? = null
) {
  val factory = remember {
    viewModelFactory
        ?: object : ViewModelProvider.Factory {
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ManageOfficeViewModel(userViewModel, OfficeRepositoryFirestore()) as T
          }
        }
  }

  // TODO: reload office data when returning to this screen

  val vm: ManageOfficeViewModel = viewModel(factory = factory)
  val office by vm.office.collectAsState()
  val isLoading by vm.isLoading.collectAsState()

  var showLeaveDialog by remember { mutableStateOf(false) }

  val isOwner = office?.ownerId == userViewModel.user.value.uid

  var editableName by remember { mutableStateOf(office?.name ?: "") }
  var editableDescription by remember { mutableStateOf(office?.description ?: "") }
  var editableAddress by remember { mutableStateOf(office?.address?.toString() ?: "") }

  LaunchedEffect(office) {
    if (office != null) {
      editableName = office!!.name
      editableDescription = office!!.description ?: ""
      editableAddress = office!!.address?.toString() ?: ""
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("My Office") },
            navigationIcon = {
              IconButton(onClick = onGoBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            modifier = Modifier.testTag(TOP_BAR))
      }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
          if (office == null) {
            Button(
                onClick = onCreateOffice,
                modifier = Modifier.fillMaxWidth().testTag(CREATE_OFFICE_BUTTON)) {
                  Text("Create My Office")
                }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onJoinOffice,
                modifier = Modifier.fillMaxWidth().testTag(JOIN_OFFICE_BUTTON)) {
                  Text("Join an Office")
                }
          } else {
            OutlinedTextField(
                value = if (isOwner) editableName else office!!.name,
                onValueChange = { if (isOwner) editableName = it },
                label = { Text("Office Name") },
                enabled = isOwner,
                modifier = Modifier.fillMaxWidth().testTag(OFFICE_NAME))

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = if (isOwner) editableDescription else (office!!.description ?: ""),
                onValueChange = { if (isOwner) editableDescription = it },
                label = { Text("Description") },
                enabled = isOwner,
                modifier = Modifier.fillMaxWidth().testTag(OFFICE_DESCRIPTION))

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = if (isOwner) editableAddress else office!!.address.toString(),
                onValueChange = { if (isOwner) editableAddress = it },
                label = { Text("Address") },
                enabled = isOwner,
                modifier = Modifier.fillMaxWidth().testTag(OFFICE_ADDRESS))

            Spacer(Modifier.height(24.dp))

            Text("Vets in this office:", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.fillMaxWidth().testTag(OFFICE_VET_LIST)) {
              items(office!!.vets) { vetId -> AuthorName(vetId) }
            }

            if (isOwner) {
              Spacer(Modifier.height(24.dp))

              Button(
                  onClick = {
                    vm.updateOffice(
                        office!!.copy(
                            name = editableName,
                            description = editableDescription,
                            // TODO: convert editableAddress back to Location when ready
                        ))
                  },
                  modifier = Modifier.fillMaxWidth()) {
                    Text("Save Changes")
                  }
            }

            Spacer(Modifier.height(32.dp))

            val color = StatusColors().spam
            OutlinedButton(
                onClick = { showLeaveDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
                border = BorderStroke(1.dp, color),
                modifier = Modifier.fillMaxWidth().testTag(LEAVE_OFFICE_BUTTON)) {
                  Text("Leave My Office")
                }

            if (showLeaveDialog) {
              AlertDialog(
                  onDismissRequest = { showLeaveDialog = false },
                  title = { Text("Leave Office?") },
                  text = { Text("Are you sure you want to leave this office?") },
                  confirmButton = {
                    TextButton(
                        onClick = {
                          showLeaveDialog = false
                          vm.leaveOffice()
                          onGoBack()
                        }) {
                          Text("Leave")
                        }
                  },
                  dismissButton = {
                    TextButton(onClick = { showLeaveDialog = false }) { Text("Cancel") }
                  })
            }
          }
        }
      }
}
