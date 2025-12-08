package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.testutil.FakeAuthRepository
import com.android.agrihealth.testutil.FakeUserViewModel
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.user.UserViewModelContract
import com.android.agrihealth.ui.user.defaultUser

object VerifyEmailScreenTestTags {
    const val WELCOME = "Welcome"
    const val FARMER = "FarmerButton"
    const val VET = "VetButton"
}

/**
 * Minimalist composable function to display a screen to new google users, this should only display
 * when the user logs in with a new google account for the first time. The point of this screen is
 * to let the user choose a role as the other user information will be provided through the profile
 * screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    vm: VerifyEmailViewModel = viewModel(),
    onBack: () -> Unit = {},
    onVerified: () -> Unit = {},
    userViewModel: UserViewModelContract
) {
    val uiState = vm.uiState.collectAsState()
    val userGreeting = remember {
        "One last step! Confirm your email address to have full access to our app"
    }

    LaunchedEffect(Unit) {
        vm.sendVerifyEmail()
        vm.pollingRefresh()
    }

    LaunchedEffect(uiState.value.verified) {
        if (uiState.value.verified) {
            onVerified.invoke()
        }
    }

    val user = userViewModel.user.collectAsState()

    Scaffold(
        topBar = {
            // Top bar with back arrow and title/status
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            vm.signOut(credentialManager)
                            onBack.invoke()
                        },
                        modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                })
        }) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = userGreeting,
                style = MaterialTheme.typography.headlineLarge,
                overflow = TextOverflow.Visible,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .testTag(VerifyEmailScreenTestTags.WELCOME)
                    .fillMaxWidth()
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "email was successfully sent to ${user.value.email}",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { vm.sendVerifyEmail() },
                    modifier = Modifier.testTag(VerifyEmailScreenTestTags.FARMER)
                ) {
                    Text("Send new email")
                }
                Text("Didn't receive the email?", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview
@Composable
fun VerifyEmailScreenPreview() {
    AgriHealthAppTheme {
        VerifyEmailScreen(
            vm = VerifyEmailViewModel(FakeAuthRepository()),
            userViewModel = FakeUserViewModel(defaultUser.copy(email = "ma@email.coom"))
        )
    }
}
