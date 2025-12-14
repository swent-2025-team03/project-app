package com.android.agrihealth.ui.office

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.core.design.theme.StatusColors
import com.android.agrihealth.ui.common.AuthorName
import com.android.agrihealth.ui.loading.LoadingOverlay
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags.GO_BACK_BUTTON
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.CONFIRM_LEAVE
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.CREATE_OFFICE_BUTTON
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.JOIN_OFFICE_BUTTON
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.LEAVE_OFFICE_BUTTON
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.OFFICE_ADDRESS
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.OFFICE_DESCRIPTION
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.OFFICE_NAME
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.OFFICE_VET_LIST
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.SAVE_BUTTON
import com.android.agrihealth.ui.profile.CodesViewModel
import com.android.agrihealth.ui.profile.GenerateCode
import com.android.agrihealth.ui.profile.ProfileScreenTestTags.TOP_BAR
import com.android.agrihealth.ui.user.UserViewModel

object ManageOfficeScreenTestTags {
  const val CREATE_OFFICE_BUTTON = "CreateOfficeButton"
  const val JOIN_OFFICE_BUTTON = "JoinOfficeButton"
  const val OFFICE_NAME = "OfficeName"
  const val OFFICE_ADDRESS = "OfficeAddress"
  const val OFFICE_DESCRIPTION = "OfficeDescription"
  const val OFFICE_VET_LIST = "OfficeVetList"
  const val SAVE_BUTTON = "SaveButton"
  const val LEAVE_OFFICE_BUTTON = "LeaveOfficeButton"
  const val CONFIRM_LEAVE = "ConfirmLeaveOffice"
}

@SuppressLint("StateFlowValueCalledInComposition", "SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOfficeScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onCreateOffice: () -> Unit = {},
    onJoinOffice: () -> Unit = {},
    manageOfficeVmFactory: () -> ViewModelProvider.Factory,
    codesVmFactory: () -> ViewModelProvider.Factory,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val manageOfficeVm: ManageOfficeViewModel = viewModel(factory = manageOfficeVmFactory())

  val connectionVm: CodesViewModel = viewModel(factory = codesVmFactory())

  val uiState by manageOfficeVm.uiState.collectAsState()

  val currentUser = userViewModel.uiState.collectAsState().value.user

  var showLeaveDialog by remember { mutableStateOf(false) }

  val isOwner = uiState.office?.ownerId == currentUser.uid

  LaunchedEffect(uiState.error) {
    uiState.error?.let { snackbarHostState.showSnackbar(uiState.error ?: "") }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("My Office") },
            navigationIcon = {
              IconButton(onClick = onGoBack, modifier = Modifier.testTag(GO_BACK_BUTTON)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            modifier = Modifier.testTag(TOP_BAR))
      },
      snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.imePadding()) }) {
          innerPadding ->
        LoadingOverlay(isLoading = uiState.isLoading) {
          Column(
              modifier =
                  Modifier.padding(innerPadding)
                      .padding(16.dp)
                      .verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Top) {
                HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))
                if (uiState.error == null) {
                  if (uiState.office == null) {
                    Button(
                        onClick = onCreateOffice,
                        modifier = Modifier.fillMaxWidth().testTag(CREATE_OFFICE_BUTTON)) {
                          Text("Create My Office")
                        }

                    Button(
                        onClick = onJoinOffice,
                        modifier = Modifier.fillMaxWidth().testTag(JOIN_OFFICE_BUTTON)) {
                          Text("Join an Office")
                        }
                  } else {
                    OutlinedTextField(
                        value = if (isOwner) uiState.editableName else uiState.office!!.name,
                        onValueChange = { if (isOwner) manageOfficeVm.onNameChange(it) },
                        label = { Text("Office Name") },
                        enabled = isOwner,
                        modifier = Modifier.fillMaxWidth().testTag(OFFICE_NAME))

                    OutlinedTextField(
                        value =
                            if (isOwner) uiState.editableDescription
                            else (uiState.office!!.description ?: ""),
                        onValueChange = { if (isOwner) manageOfficeVm.onDescriptionChange(it) },
                        label = { Text("Description") },
                        enabled = isOwner,
                        modifier = Modifier.fillMaxWidth().testTag(OFFICE_DESCRIPTION))

                    OutlinedTextField(
                        value = uiState.office!!.address?.name ?: "",
                        onValueChange = { if (isOwner) manageOfficeVm.onAddressChange(it) },
                        singleLine = true,
                        readOnly = true,
                        label = { Text("Address") },
                        enabled = isOwner,
                        modifier = Modifier.fillMaxWidth().testTag(OFFICE_ADDRESS))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Vets in this office:", style = MaterialTheme.typography.titleMedium)

                    LazyColumn(
                        modifier =
                            Modifier.fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .testTag(OFFICE_VET_LIST)) {
                          items(uiState.office!!.vets) { vetId ->
                            AuthorName(
                                vetId,
                                onClick = { navigationActions.navigateTo(Screen.ViewUser(vetId)) })
                          }
                        }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isOwner) {
                      GenerateCode(
                          codesViewModel = connectionVm,
                          snackbarHostState = snackbarHostState,
                          Modifier.align(Alignment.CenterHorizontally))

                      Spacer(modifier = Modifier.height(8.dp))

                      Button(
                          onClick = { manageOfficeVm.updateOffice() },
                          modifier = Modifier.fillMaxWidth().testTag(SAVE_BUTTON),
                      ) {
                        Text("Save Changes")
                      }
                    }

                    OutlinedButton(
                        onClick = { showLeaveDialog = true },
                        colors =
                            ButtonDefaults.outlinedButtonColors(contentColor = StatusColors().spam),
                        border = BorderStroke(1.dp, StatusColors().spam),
                        modifier = Modifier.fillMaxWidth().testTag(LEAVE_OFFICE_BUTTON)) {
                          Text("Leave My Office")
                        }
                    // TODO: improve leave button permissions; currently only owners can trigger
                    // dialog

                    if (showLeaveDialog) {
                      AlertDialog(
                          onDismissRequest = { showLeaveDialog = false },
                          title = { Text("Leave Office?") },
                          text = { Text("Are you sure you want to leave this office?") },
                          confirmButton = {
                            TextButton(
                                onClick = {
                                  showLeaveDialog = false
                                  manageOfficeVm.leaveOffice(onSuccess = onGoBack)
                                },
                                modifier = Modifier.testTag(CONFIRM_LEAVE)) {
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
      }
}
