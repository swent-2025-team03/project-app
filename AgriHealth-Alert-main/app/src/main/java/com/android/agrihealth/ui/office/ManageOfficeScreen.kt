package com.android.agrihealth.ui.office

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
import com.android.agrihealth.data.model.office.OfficeRepositoryFirestore
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.CREATE_OFFICE_BUTTON
import com.android.agrihealth.ui.office.ManageOfficeScreenTestTags.JOIN_OFFICE_BUTTON
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
}

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

  val vm: ManageOfficeViewModel = viewModel(factory = factory)
  val office by vm.office.collectAsState()
  val isLoading by vm.isLoading.collectAsState()

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
            Text("Office: ${office!!.name}", Modifier.testTag(OFFICE_NAME))
            Spacer(Modifier.height(8.dp))
            Text("Address: ${office!!.address}", Modifier.testTag(OFFICE_ADDRESS))
            Spacer(Modifier.height(8.dp))
            Text("Description: ${office!!.description}", Modifier.testTag(OFFICE_DESCRIPTION))

            Spacer(Modifier.height(16.dp))
            Text("Vets in this office:")

            LazyColumn(modifier = Modifier.fillMaxWidth().testTag(OFFICE_VET_LIST)) {
              items(office!!.vets) { vetId -> Text("- $vetId") }
            }
          }
        }
      }
}
