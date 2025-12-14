package com.android.agrihealth.ui.authentification

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.R
import com.android.agrihealth.ui.loading.LoadingOverlay

// Imports for preview
/*
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.Credential
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.user.User
import com.google.firebase.auth.FirebaseUser
*/

object SignInScreenTestTags {
  const val SCREEN = "SignInScreen"
  const val LOGIN_BUTTON = "loginButton"
  const val SIGN_UP_BUTTON = "signUpButton"
  const val LOGIN_TITLE = "loginTitle"
  const val EMAIL_FIELD = "emailField"
  const val PASSWORD_FIELD = "passwordField"
  const val FORGOT_PASSWORD = "forgotPassword"
  const val LOGIN_DIVIDER = "loginDivider"
  const val GOOGLE_LOGIN_BUTTON = "googleLoginButton"
  const val SNACKBAR = "snackbar"
}

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onForgotPasswordClick: () -> Unit = {},
    onSignedIn: () -> Unit = {},
    onNewGoogle: () -> Unit = {},
    onNotVerified: () -> Unit = {},
    goToSignUp: () -> Unit = {},
    signInViewModel: SignInViewModel = viewModel()
) {

  val signInUIState by signInViewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val errorMsg = signInUIState.errorMsg

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      snackbarHostState.showSnackbar(errorMsg)
      signInViewModel.clearErrorMsg()
    }
  }

  val context = LocalContext.current
  LaunchedEffect(signInUIState.verified) {
    signInUIState.verified?.let {
      if (signInUIState.isNewGoogle) onNewGoogle()
      else if (signInUIState.verified!!) onSignedIn() else onNotVerified()
    }
  }

  LoadingOverlay(isLoading = signInUIState.isLoading) {
    Scaffold(
        modifier = modifier.testTag(SignInScreenTestTags.SCREEN),
        snackbarHost = {
          SnackbarHost(
              hostState = snackbarHostState,
              modifier =
                  Modifier.padding(bottom = 16.dp)
                      .testTag(SignInScreenTestTags.SNACKBAR)
                      .imePadding())
        }) { padding ->
          Column(
              modifier =
                  Modifier.padding(padding)
                      .padding(horizontal = 32.dp)
                      .verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(96.dp))
                Text(
                    text = "AgriHealth",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.testTag(SignInScreenTestTags.LOGIN_TITLE))

                Spacer(Modifier.height(56.dp))

                TextField(
                    value = signInUIState.email,
                    onValueChange = { signInViewModel.setEmail(it) },
                    placeholder = { Text("Email") },
                    singleLine = true,
                    isError = signInUIState.emailIsInvalid && signInUIState.email.isEmpty(),
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(56.dp)
                            .testTag(SignInScreenTestTags.EMAIL_FIELD))

                Spacer(Modifier.height(16.dp))

                TextField(
                    value = signInUIState.password,
                    onValueChange = { signInViewModel.setPassword(it) },
                    placeholder = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = signInUIState.passwordIsInvalid && signInUIState.password.isEmpty(),
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(56.dp)
                            .testTag(SignInScreenTestTags.PASSWORD_FIELD))

                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth()) {
                  Spacer(Modifier.weight(1f))
                  Text(
                      text = "Forgot password",
                      style = MaterialTheme.typography.bodyMedium,
                      textAlign = TextAlign.End,
                      modifier =
                          Modifier.padding(top = 4.dp)
                              .clickable { onForgotPasswordClick() }
                              .testTag(SignInScreenTestTags.FORGOT_PASSWORD))
                }

                Spacer(Modifier.height(16.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    modifier =
                        Modifier.fillMaxWidth(0.8f).testTag(SignInScreenTestTags.LOGIN_DIVIDER))
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { signInViewModel.signInWithEmailAndPassword() },
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(56.dp)
                            .testTag(SignInScreenTestTags.LOGIN_BUTTON)) {
                      Text("Log In", color = MaterialTheme.colorScheme.onPrimary)
                    }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = goToSignUp,
                    shape = RoundedCornerShape(28.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(56.dp)
                            .testTag(SignInScreenTestTags.SIGN_UP_BUTTON)) {
                      Text("Create an account")
                    }
                Spacer(Modifier.height(32.dp))
                GoogleSignInButton {
                  signInViewModel.signInWithGoogle(
                      context = context, credentialManager = credentialManager)
                }
              }
        }
  }
}

// function from bootcamp-25-B3-Solution
@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Button(
      onClick = onSignInClick,
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.tertiaryContainer,
              contentColor = MaterialTheme.colorScheme.onTertiaryContainer), // Button color
      modifier =
          Modifier.padding(8.dp)
              .height(48.dp) // Adjust height as needed
              .testTag(SignInScreenTestTags.GOOGLE_LOGIN_BUTTON)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              // Load the Google logo from resources
              Image(
                  painter =
                      painterResource(id = R.drawable.google_logo), // Ensure this drawable exists
                  contentDescription = "Google Logo",
                  modifier =
                      Modifier.size(30.dp) // Size of the Google logo
                          .padding(end = 8.dp))

              // Text for the button
              Text(text = "Sign in with Google")
            }
      }
}

/*
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignInScreenPreview() {
    val authRepo = object : AuthRepository {
        override suspend fun signInWithEmailAndPassword(
            email: String,
            password: String
        ): Result<FirebaseUser> {TODO()}

        override suspend fun reAuthenticate(
            email: String,
            password: String
        ): Result<Unit> {TODO()}

        override suspend fun changePassword(password: String): Result<Unit> {TODO()}

        override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> {TODO()}

        override suspend fun signUpWithEmailAndPassword(
            email: String,
            password: String,
            userData: User
        ): Result<FirebaseUser> {TODO()}

        override fun signOut(): Result<Unit> {TODO()}

        override suspend fun deleteAccount(): Result<Unit> {TODO()}

    }
    val userRepo =object : UserRepository{
        override suspend fun addUser(user: User) {TODO()}

        override suspend fun updateUser(user: User) {TODO()}

        override suspend fun deleteUser(uid: String) {TODO()}

        override suspend fun getUserFromId(uid: String): Result<User> {TODO()}
    }
    val vm = object : SignInViewModel(authRepo, userRepo){
    }
    AgriHealthAppTheme { SignInScreen(
          credentialManager = CredentialManager.create(LocalContext.current),
        signInViewModel = vm) }
}
*/
