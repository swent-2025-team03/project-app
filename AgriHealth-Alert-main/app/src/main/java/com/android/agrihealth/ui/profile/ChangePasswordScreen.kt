package com.android.agrihealth.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.agrihealth.testutil.FakeChangePasswordViewModel
import com.android.agrihealth.ui.loading.LoadingOverlay
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen

/** Tags for the various components. For testing purposes */
object ChangePasswordScreenTestTags {
  const val OLD_PASSWORD = "oldPassword"
  const val NEW_PASSWORD = "newPassword"
  const val SAVE_BUTTON = "saveButton"
}

/** Texts for the password change feedback. For testing purposes */
object ChangePasswordFeedbackTexts {
  const val OLD_WRONG = "Password does not match."
  const val NEW_WEAK = "Password is too weak or common to use."
}

/** Change password screen for email and password users */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit = {},
    userEmail: String,
    onUpdatePassword: () -> Unit = {},
    changePasswordViewModel: ChangePasswordViewModelContract = FakeChangePasswordViewModel()
) {

  val uiState by changePasswordViewModel.uiState.collectAsState()
  LaunchedEffect(Unit) { changePasswordViewModel.setEmail(userEmail) }

  LaunchedEffect(uiState.success) { if (uiState.success) onUpdatePassword.invoke() }

    LoadingOverlay(isLoading = uiState.isLoading) {
    Scaffold(
      topBar = {
        // Top bar with back arrow and title/status
        TopAppBar(
            title = {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Screen.ChangePassword.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).testTag(NavigationTestTags.TOP_BAR_TITLE))
                  }
            },
            navigationIcon = {
              IconButton(
                  onClick = onBack,
                  modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      }) { padding ->

        // Main scrollable content
        Column(
            modifier =
                Modifier.padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

              // Old password field
              Field(
                  value = uiState.oldPassword,
                  onValueChange = { changePasswordViewModel.setOldPassword(it) },
                  testTag = ChangePasswordScreenTestTags.OLD_PASSWORD,
                  errorText = ChangePasswordFeedbackTexts.OLD_WRONG,
                  label = "Current Password",
                  error = uiState.oldWrong)

              // New password field
              Field(
                  value = uiState.newPassword,
                  onValueChange = { changePasswordViewModel.setNewPassword(it) },
                  testTag = ChangePasswordScreenTestTags.NEW_PASSWORD,
                  errorText = ChangePasswordFeedbackTexts.NEW_WEAK,
                  label = "New Password",
                  error = uiState.newWeak)

              // Save button

            }
      }
    }
}

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String,
    errorText: String,
    label: String,
    error: Boolean = false
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      isError = error,
      supportingText = { if (error) Text(errorText) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(testTag))
}

/**
 * Preview of the ReportViewScreen for both farmer and vet roles. Allows testing of layout and
 * colors directly in Android Studio.
 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ChangePasswordScreenPreview() {
  MaterialTheme {
    ChangePasswordScreen(
        userEmail = "notan@email.no",
        onBack = {},
        onUpdatePassword = {},
        changePasswordViewModel = FakeChangePasswordViewModel("password"))
  }
}
