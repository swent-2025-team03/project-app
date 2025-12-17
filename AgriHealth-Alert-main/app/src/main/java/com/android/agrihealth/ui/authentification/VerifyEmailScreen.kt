package com.android.agrihealth.ui.authentification

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.ui.navigation.NavigationTestTags

// imports for debug button
/*
import androidx.compose.material.icons.filled.Preview
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking
import com.android.agrihealth.data.model.authentification.verifyUser
 */

// imports for preview
/*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.testutil.FakeAuthRepository
import com.android.agrihealth.testutil.FakeUserViewModel
import com.android.agrihealth.ui.user.defaultUser
 */
object VerifyEmailScreenTestTags {
  const val WELCOME = "Welcome"
  const val SEND_EMAIL = "SendEmail"
  const val SNACKBAR = "Snackbar"
}

object VerifyEmailScreenTexts {
  const val GREETING = "One last step! Confirm your email address to have full access to our app"
  const val SEND_BUTTON = "Send new email"
  const val NO_EMAIL_QUESTIONMARK = "Didn't receive the email?"

  fun textForCountdown(cd: Int): String {
    return if (cd > 0) "Can resend email in $cd seconds" else ""
  }
}

/**
 * Screen used to keep unverified users away from the main app, must be present for unverified users
 * right after authentication.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    vm: VerifyEmailViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onBack: () -> Unit = {},
    onVerified: () -> Unit = {}
) {
  val uiState = vm.uiState.collectAsState()

  LaunchedEffect(Unit) {
    vm.sendVerifyEmail()
    vm.pollingRefresh()
  }

  LaunchedEffect(uiState.value.verified) {
    if (uiState.value.verified) {
      onVerified()
    }
  }

  val errorMsg = uiState.value.errorMsg
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      snackbarHostState.showSnackbar(errorMsg)
      vm.clearError()
    }
  }

  BackHandler {
    vm.signOut(credentialManager)
    onBack()
  }

  Scaffold(
      snackbarHost = {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.testTag(VerifyEmailScreenTestTags.SNACKBAR))
      },
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(
                  onClick = {
                    vm.signOut(credentialManager)
                    onBack()
                  },
                  modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
            })
      }) { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)) {
              Text(
                  text = VerifyEmailScreenTexts.GREETING,
                  style = MaterialTheme.typography.headlineLarge,
                  overflow = TextOverflow.Visible,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag(VerifyEmailScreenTestTags.WELCOME).fillMaxWidth())
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    VerifyEmailScreenTexts.NO_EMAIL_QUESTIONMARK,
                    style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = { vm.sendVerifyEmail() },
                    enabled = uiState.value.enabled,
                    modifier = Modifier.testTag(VerifyEmailScreenTestTags.SEND_EMAIL)) {
                      Text(VerifyEmailScreenTexts.SEND_BUTTON)
                    }
                Text(VerifyEmailScreenTexts.textForCountdown(uiState.value.countdown))

                // Debug button to enable any account
                /*
                Button(onClick = { runBlocking { verifyUser(Firebase.auth.uid ?: "") } }) {
                  Text("Haxchi")
                }
                 */
              }
            }
      }
}

/*
@Preview
@Composable
fun VerifyEmailScreenPreview() {
  AgriHealthAppTheme {
    VerifyEmailScreen(
        vm = VerifyEmailViewModel(FakeAuthRepository()),
        userViewModel = FakeUserViewModel(defaultUser.copy(email = "ma@email.coom")))
  }
}
*/
