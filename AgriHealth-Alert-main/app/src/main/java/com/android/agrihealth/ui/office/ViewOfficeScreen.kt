package com.android.agrihealth.ui.office

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.ui.common.RemotePhotoDisplay
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.common.resolver.AuthorName
import com.android.agrihealth.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewOfficeScreen(
    viewModel: ViewOfficeViewModel,
    onBack: () -> Unit,
    onOpenUser: (String) -> Unit = {},
    imageViewModel: ImageViewModel = ImageViewModel()
) {
  val uiState by viewModel.uiState

  val context = LocalContext.current

  LaunchedEffect(Unit) { viewModel.load() }

  if (uiState is ViewOfficeUiState.Error) {
    LaunchedEffect(uiState) {
      Toast.makeText(context, (uiState as ViewOfficeUiState.Error).message, Toast.LENGTH_LONG)
          .show()
    }
  }

  BackHandler { onBack() }
  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              if (uiState is ViewOfficeUiState.Success) {
                val office = (uiState as ViewOfficeUiState.Success).office
                Text(office.name)
              } else {
                Text(Screen.ViewOffice("").name)
              }
            },
            navigationIcon = {
              IconButton(
                  onClick = onBack,
                  modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
            },
            modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
      }) { padding ->
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
              ViewOfficeContent(
                  office = success.office,
                  onOpenUser = onOpenUser,
                  photoUrl = success.office.photoUrl,
                  imageViewModel = imageViewModel)
            }
          }
        }
      }
}

@Composable
private fun ViewOfficeContent(
    office: Office,
    onOpenUser: (String) -> Unit,
    photoUrl: String?,
    imageViewModel: ImageViewModel
) {
  val scroll = rememberScrollState()
  val noInteraction = remember { MutableInteractionSource() }

  Column(
      modifier =
          Modifier.fillMaxSize()
              .verticalScroll(scroll)
              .padding(16.dp)
              .testTag(ViewOfficeScreenTestTags.OFFICE_INFO_COLUMN),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))

        RemotePhotoDisplay(
            photoURL = photoUrl,
            imageViewModel = imageViewModel,
            modifier = Modifier.size(120.dp).clip(CircleShape),
            contentDescription = "Office photo",
            showPlaceHolder = true)

        OutlinedTextField(
            value = office.name,
            onValueChange = {},
            label = { Text("Office Name") },
            readOnly = true,
            interactionSource = noInteraction,
            enabled = false,
            modifier = Modifier.fillMaxWidth().testTag(ViewOfficeScreenTestTags.NAME_FIELD))

        office.address?.name?.let {
          OutlinedTextField(
              value = it,
              onValueChange = {},
              label = { Text("Address") },
              readOnly = true,
              interactionSource = noInteraction,
              enabled = false,
              modifier = Modifier.fillMaxWidth().testTag(ViewOfficeScreenTestTags.ADDRESS_FIELD))
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
        }

        if (office.vets.isNotEmpty()) {
          Text("Vets in this office:", style = MaterialTheme.typography.titleMedium)

          LazyColumn(
              modifier =
                  Modifier.fillMaxWidth()
                      .heightIn(max = 300.dp)
                      .testTag(ViewOfficeScreenTestTags.VET_LIST)) {
                items(office.vets) { vetId -> AuthorName(vetId, onClick = { onOpenUser(vetId) }) }
              }
        }
      }
}

object ViewOfficeScreenTestTags {
  const val LOADING_INDICATOR = "ViewOfficeLoadingIndicator"
  const val ERROR_TEXT = "ViewOfficeErrorText"
  const val OFFICE_INFO_COLUMN = "ViewOfficeContentColumn"
  const val NAME_FIELD = "ViewOfficeNameField"
  const val ADDRESS_FIELD = "ViewOfficeAddressField"
  const val DESCRIPTION_FIELD = "ViewOfficeDescriptionField"
  const val VET_LIST = "ViewOfficeVetList"
}
