package com.android.agrihealth.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.agrihealth.testutil.FakeChangePasswordViewModel
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

private val unfocusedFieldColor = Color(0xFFF0F7F1)
private val focusedFieldColor = Color(0xFFF0F7F1)
private val createReportButtonColor = Color(0xFF96B7B1)

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
  changePasswordViewModel.setEmail(userEmail)

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
              Field(
                  uiState.oldPassword,
                  { changePasswordViewModel.setOldPassword(it) },
                  ChangePasswordScreenTestTags.OLD_PASSWORD,
                  errorText = ChangePasswordFeedbackTexts.OLD_WRONG,
                  placeholder = "Current Password",
                  error = uiState.oldWrong)
              Field(
                  uiState.newPassword,
                  { changePasswordViewModel.setNewPassword(it) },
                  ChangePasswordScreenTestTags.NEW_PASSWORD,
                  errorText = ChangePasswordFeedbackTexts.NEW_WEAK,
                  placeholder = "New Password",
                  error = uiState.newWeak)
              Button(
                  onClick = {
                    if (changePasswordViewModel.changePassword()) {
                      onUpdatePassword.invoke()
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(56.dp)
                          .testTag(ChangePasswordScreenTestTags.SAVE_BUTTON),
                  shape = RoundedCornerShape(20.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = createReportButtonColor)) {
                    Text("Save Changes", fontSize = 24.sp)
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
    placeholder: String,
    error: Boolean = false
) {
  OutlinedTextField(
      value = value,
      placeholder = { Text(placeholder) },
      onValueChange = { it -> onValueChange(it) },
      singleLine = true,
      shape = RoundedCornerShape(28.dp),
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(testTag),
      supportingText = { if (error) Text(errorText) },
      isError = error,
      colors =
          OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = unfocusedFieldColor,
              focusedContainerColor = focusedFieldColor,
              unfocusedBorderColor = Color.Transparent,
              focusedBorderColor = Color.Transparent))
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
