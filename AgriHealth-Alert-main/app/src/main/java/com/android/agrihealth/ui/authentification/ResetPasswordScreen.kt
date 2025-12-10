package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.core.design.theme.successColor
import com.android.agrihealth.testutil.FakeAuthRepository
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen

object ResetPasswordScreenTestTags {
  const val INSTRUCTION_TEXT = "resetPasswordInstructionTest"
  const val EMAIL = "resetPasswordEmail"
  const val SEND_RESET_EMAIL_BUTTON = "resetPasswordResetEmailButton"
  const val SUCCESS_FEEDBACK = "resetPasswordSuccessFeedBack"
  const val FAIL_FEEDBACK = "resetPasswordFailFeedBack"
  const val WAITING_FEEDBACK = "resetPasswordWaitingFeedBack"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(onBack: () -> Unit = {}, vm: ResetPasswordViewModel = viewModel()) {

  val uiState by vm.uiState.collectAsState()

  val scrollState = rememberScrollState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = Screen.ResetPassword.name,
                  style = MaterialTheme.typography.titleLarge,
                  modifier = Modifier.testTag(NavigationTestTags.TOP_BAR_TITLE))
            },
            navigationIcon = {
              IconButton(
                  onClick = onBack,
                  modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
            })
      },
  ) { pd ->
    Column(
        modifier =
            Modifier.padding(pd)
                .padding(horizontal = 16.dp, vertical = 48.dp)
                .verticalScroll(
                    state = scrollState,
                    enabled = true,
                ),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
          Text(
              "Enter your email and you will receive a form to reset your password.",
              modifier = Modifier.testTag(ResetPasswordScreenTestTags.INSTRUCTION_TEXT))

          OutlinedTextField(
              value = uiState.email,
              placeholder = { Text("Email") },
              onValueChange = { vm.setEmail(it) },
              singleLine = true,
              label = { Text("Email") },
              isError = uiState.emailIsMalformed,
              modifier = Modifier.fillMaxWidth().testTag(ResetPasswordScreenTestTags.EMAIL),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))

          Button(
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(ResetPasswordScreenTestTags.SEND_RESET_EMAIL_BUTTON),
              onClick = vm::sendFormButtonClicked) {
                Text("Send reset form")
              }

          SendingResetFormFeedbackText(status = uiState.emailSendStatus)
        }
  }
}

@Composable
fun SendingResetFormFeedbackText(
    modifier: Modifier = Modifier,
    status: EmailSendStatus = EmailSendStatus.None
) {

  Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    when (status) {
      is EmailSendStatus.Success -> {
        Text(
            "The form was sent successfully !",
            color = successColor(),
            modifier = Modifier.testTag(ResetPasswordScreenTestTags.SUCCESS_FEEDBACK))
        Text("Check your Inbox !")
      }
      is EmailSendStatus.Fail -> {
        Text(
            "Something went wrong !",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag(ResetPasswordScreenTestTags.FAIL_FEEDBACK))
      }
      is EmailSendStatus.Waiting -> {
        Text(
            "Sending reset form !",
            modifier = Modifier.testTag(ResetPasswordScreenTestTags.WAITING_FEEDBACK))
      }
      is EmailSendStatus.None -> {}
    }
  }
}

@Preview
@Composable
fun ResetPasswordScreenPreview() {
  val vm = ResetPasswordViewModel(FakeAuthRepository())
  AgriHealthAppTheme { ResetPasswordScreen(vm = vm) }
}
